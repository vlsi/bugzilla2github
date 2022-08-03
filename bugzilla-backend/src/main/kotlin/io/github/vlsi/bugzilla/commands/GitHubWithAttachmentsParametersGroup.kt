package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.github.vlsi.bugzilla.dbexport.GitHubPagesAttachmentLinkGenerator

class GitHubWithAttachmentsParametersGroup : GitHubParametersGroup() {
    val attachmentsRepository by option("--github-attachments-repository").required()

    val attachmentLinkGenerator by lazy {
        GitHubPagesAttachmentLinkGenerator(organization, attachmentsRepository)
    }
}
