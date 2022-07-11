package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.github.vlsi.bugzilla.dbexport.GitHubPagesAttachmentLinkGenerator
import io.github.vlsi.bugzilla.github.GitHubApi
import io.ktor.client.*
import io.ktor.client.engine.cio.*

class GitHubParametersGroup : OptionGroup() {
    val organization by option("--github-organization").required()
    val issuesRepository by option("--github-issues-repository").required()
    val attachmentsRepository by option("--github-attachments-repository").required()
    val token by option("--github-token", envvar = "GITHUB_TOKEN", help = "Prefer passing the token via GITHUB_TOKEN environment variable").required()

    val attachmentLinkGenerator by lazy {
        GitHubPagesAttachmentLinkGenerator(organization, attachmentsRepository)
    }

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
