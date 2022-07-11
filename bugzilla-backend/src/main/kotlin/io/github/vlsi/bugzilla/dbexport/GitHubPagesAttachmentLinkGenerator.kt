package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.commands.attachmentDir
import io.github.vlsi.bugzilla.dto.AttachId
import io.github.vlsi.bugzilla.dto.BugId
import io.ktor.http.*
import io.ktor.server.util.*

class GitHubPagesAttachmentLinkGenerator(
    private val organization: String,
    private val repository: String,
) : AttachmentLinkGenerator {
    override fun linkFor(bugId: BugId, attachId: AttachId, filename: String): String =
        url {
            protocol = URLProtocol.HTTPS
            host = "$organization.github.io"
            path(repository, attachmentDir(bugId), "${attachId.value}-$filename")
        }
}
