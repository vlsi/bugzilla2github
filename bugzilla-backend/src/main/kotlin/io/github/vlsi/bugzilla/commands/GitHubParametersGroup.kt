package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.github.vlsi.bugzilla.dbexport.BugzillaLinkGenerator
import io.github.vlsi.bugzilla.dbexport.GitHubIssueLinkGenerator
import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.GitHubApi
import io.github.vlsi.bugzilla.github.IssueNumber
import io.ktor.client.*
import io.ktor.client.engine.cio.*

open class GitHubParametersGroup : OptionGroup(name = "GitHub parameters") {
    val token by option(
        "--github-token",
        envvar = "GITHUB_TOKEN",
        help = "Prefer passing the token via GITHUB_TOKEN environment variable"
    ).required()
    val organization by option("--github-organization").required()
    val issuesRepository by option("--github-issues-repository").required()

    fun issueLinkGenerator(
        bugzillaLinkGenerator: BugzillaLinkGenerator,
        bugToIssue: Map<BugId, IssueNumber> = emptyMap()
    ) =
        GitHubIssueLinkGenerator(bugzillaLinkGenerator, bugToIssue, organization, issuesRepository)

    val gitHubApi by lazy {
        GitHubApi(
            token = token,
            httpClient = HttpClient(CIO) {
                engine {
                    maxConnectionsCount = 1
                }
            }
        )
    }
}
