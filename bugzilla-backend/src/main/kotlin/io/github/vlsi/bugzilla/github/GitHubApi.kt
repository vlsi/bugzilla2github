package io.github.vlsi.bugzilla.github

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory
import kotlin.time.Duration
import kotlin.time.Duration.Companion.hours

class GitHubApi(
    private val delayBetweenApiCalls: Duration,
    private val httpClient: HttpClient,
) {
    companion object {
        val log = LoggerFactory.getLogger(GitHubApi::class.java)
    }

    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
    }

    private fun HttpRequestBuilder.acceptGitHubJson() {
        headers {
            append("Accept", "application/vnd.github+json")
        }
    }

    var lastRequestCompleted = Clock.System.now() - 24.hours

    private suspend fun <T> throttle(body: suspend () -> T): T {
        val elapsedDelay = Clock.System.now() - lastRequestCompleted - delayBetweenApiCalls
        if (elapsedDelay < Duration.ZERO) {
            delay(-elapsedDelay)
        }
        try {
            return body()
        } finally {
            lastRequestCompleted = Clock.System.now()
        }
    }

    suspend fun startImport(
        username: String,
        repository: String,
        issue: ImportIssueRequest,
        waitCompletion: Boolean = false
    ) {
        // https://gist.github.com/jonmagic/5282384165e0f86ef105
        val str = jsonConfig.encodeToString(issue)
        log.debug("Starting issue import: {}", str)
        val results = throttle {
            httpClient.post {
                url {
                    appendPathSegments("repos", username, repository, "import", "issues")
                }
                bulkIssueImportApiHeaders()
                setBody(str)
            }
        }
        val responseText = results.bodyAsText()
        if (!waitCompletion) {
            return
        }
        log.debug("Issue import status: {}", responseText)
        val status = try {
            jsonConfig.decodeFromString<IssueImportStatusResponse>(responseText)
        } catch (e: SerializationException) {
            throw IllegalStateException("Unexpected response: $responseText")
        }
        val getStatusUrl = status.url
        if (status.status == "pending") {
            while (true) {
                val statusResp = throttle {
                    httpClient.get(getStatusUrl) {
                        bulkIssueImportApiHeaders()
                    }
                }
                val statusRespStr = statusResp.bodyAsText()
                log.debug("Issue import status: {}", statusRespStr)
                val statusRespJson = try {
                    jsonConfig.decodeFromString<IssueImportStatusResponse>(statusRespStr)
                } catch (e: SerializationException) {
                    throw IllegalStateException("Unexpected response: $statusRespStr")
                }
                val statusRespStatus = statusRespJson.status
                if (statusRespStatus != "pending") {
                    break
                }
            }
        }
    }

    private fun HttpRequestBuilder.bulkIssueImportApiHeaders() {
        headers {
            append("Accept", "application/vnd.github.golden-comet-preview+json")
        }
    }

    suspend fun render(markdown: String, organization: String, repository: String): String {
        val str = jsonConfig.encodeToString(
            RenderMarkdownRequest(text = markdown, mode = RenderMarkdownMode.gfm, context = "$organization/$repository")
        )
        log.debug("Sending request to render markdown: {}", str)
        val results = throttle {
            httpClient.post {
                url {
                    path("markdown")
                }
                acceptGitHubJson()

                setBody(str)
            }
        }
        return results.bodyAsText().also {
            log.debug("Markdown render results: {}", it)
        }
    }

    @Serializable
    class IssueInfo(
        val number: IssueNumber
    )

    suspend fun getLastIssueNumber(username: String, repository: String): IssueNumber {
        log.debug("Retrieving the last issue number from {}/{}", username, repository)
        val response = throttle {
            httpClient.get {
                url {
                    appendPathSegments("repos", username, repository, "issues")
                    parameter("per_page", 1)
                }
                bulkIssueImportApiHeaders()
            }
        }
        val responseText = response.bodyAsText()
        log.debug("Last issue number response: {}", responseText)
        val issues = jsonConfig.decodeFromString<List<IssueInfo>>(responseText)
        log.info("Last issue number in {}/{} is {}", username, repository, issues.firstOrNull()?.number)
        return issues.firstOrNull()?.number ?: IssueNumber(0)
    }

    suspend fun listMilestones(
        username: String,
        repository: String,
        state: MilestoneListState = MilestoneListState.open
    ): List<Milestone> {
        log.debug("Retrieving the list of milestones for {}/{}", username, repository)
        val response = throttle {
            httpClient.get {
                url {
                    path("repos", username, repository, "milestones")
                    parameter("state", state.name)
                }
                acceptGitHubJson()
            }
        }
        if (response.status == HttpStatusCode.NotFound) {
            return listOf()
        }
        val responseText = response.bodyAsText()
        log.debug("List milestones response: {}", responseText)
        val milestones = try {
            jsonConfig.decodeFromString<List<Milestone>>(responseText)
        } catch (e: SerializationException) {
            throw IllegalStateException("Can't get the list of milestones for $username/$repository: $responseText")
        }
        return milestones
    }

    suspend fun createMilestone(
        username: String, repository: String,
        request: MilestoneCreateRequest
    ): Milestone {
        val str = jsonConfig.encodeToString(request)
        log.debug("Creating milestone {}/{}: {}", username, repository, str)

        val response = throttle {
            httpClient.post {
                url {
                    path("repos", username, repository, "milestones")
                }
                acceptGitHubJson()
                setBody(str)
            }
        }
        val responseText = response.bodyAsText()

        val milestone = try {
            jsonConfig.decodeFromString<Milestone>(responseText)
        } catch (e: SerializationException) {
            throw IllegalStateException("Can't create milestone $str for $username/$repository: $responseText")
        }
        return milestone
    }
}
