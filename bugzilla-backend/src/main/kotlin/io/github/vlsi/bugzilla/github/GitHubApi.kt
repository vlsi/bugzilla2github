package io.github.vlsi.bugzilla.github

import io.ktor.client.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.coroutines.delay
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.slf4j.LoggerFactory

class GitHubApi(
    private val token: String,
    val httpClient: HttpClient,
) {
    companion object {
        val log = LoggerFactory.getLogger(GitHubApi::class.java)
    }

    private val jsonConfig = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
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

        val results = httpClient.post("https://api.github.com/repos") {
            url {
                appendPathSegments(username, repository, "import", "issues")
                requestHeaders()
            }
            setBody(str)
        }
        val resp = results.bodyAsText()
        if (!waitCompletion) {
            delay(500)
            return
        }
        log.debug("Issue import status: {}", resp)
        val status = jsonConfig.decodeFromString<IssueImportStatusResponse>(resp)
        val getStatusUrl = status.url
        if (status.status == "pending") {
            while (true) {
                val statusResp = httpClient.get(getStatusUrl) {
                    requestHeaders()
                }
                val statusRespStr = statusResp.bodyAsText()
                log.debug("Issue import status: {}", statusRespStr)
                val statusRespJson = jsonConfig.decodeFromString<IssueImportStatusResponse>(statusRespStr)
                val statusRespStatus = statusRespJson.status
                if (statusRespStatus != "pending") {
                    break
                }
                delay(1000)
            }
        }
    }

    private fun HttpRequestBuilder.authorization() {
        headers {
            append("Authorization", "token $token")
        }
    }

    private fun HttpRequestBuilder.requestHeaders() {
        authorization()
        headers {
            append("Accept", "application/vnd.github.golden-comet-preview+json")
        }
    }


    @Serializable
    enum class RenderMarkdownMode {
        markdown, gfm;
    }

    @Serializable
    class RenderMarkdownRequest(
        val text: String,
        val mode: RenderMarkdownMode,
        val context: String,
    )

    suspend fun render(markdown: String, organization: String, repository: String): String {
        val str = jsonConfig.encodeToString(
            RenderMarkdownRequest(text = markdown, mode = RenderMarkdownMode.gfm, context = "$organization/$repository")
        )
        log.debug("Sending request to render markdown: {}", str)
        val results = httpClient.post("https://api.github.com/markdown") {
            authorization()
            headers {
                append("Accept", "application/vnd.github+json")
            }

            setBody(str)
        }
        return results.bodyAsText().also {
            log.debug("Markdown render results: {}", it)
        }
    }

    @Serializable
    class IssueInfo(
        val number: Int
    )

    suspend fun getLastIssueNumber(username: String, repository: String): IssueNumber {
        log.debug("Retrieving the last issue number from {}/{}", username, repository)
        httpClient.get("https://api.github.com/repos") {
            url {
                appendPathSegments(username, repository, "issues")
                parameter("per_page", 1)
            }
            requestHeaders()
        }.let {
            val resp = it.bodyAsText()
            log.debug("Last issue number response: {}", resp)
            val issues = jsonConfig.decodeFromString<List<IssueInfo>>(resp)
            log.info("Last issue number in {}/{} is {}", username, repository, issues.firstOrNull()?.number)
            return IssueNumber(issues.firstOrNull()?.number ?: 0)
        }
    }

}
