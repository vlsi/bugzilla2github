package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.commands.attachmentDir
import io.github.vlsi.bugzilla.dto.AttachId
import io.github.vlsi.bugzilla.dto.BugId
import io.ktor.http.*
import io.ktor.server.util.*

private val needEncodePercent = "%20".encodeURLPath() == "%20"
private val semiEscaped = Regex("%([0-9a-z]{2})")

class GitHubPagesAttachmentLinkGenerator(
    private val organization: String,
    private val repository: String,
) : AttachmentLinkGenerator {
    override fun linkFor(bugId: BugId, attachId: AttachId, filename: String): String =
        url {
            protocol = URLProtocol.HTTPS
            host = "$organization.github.io"
            appendPathSegments(repository)
            appendEncodedPathSegments(attachmentDir(bugId))
            val encodedFileName = if (needEncodePercent) {
                // ktor fails to escape patterns that look like "already escaped" %XX
                filename.replace(semiEscaped, "%25$1")
            } else {
                filename
            }
            appendPathSegments("${attachId.value}-$encodedFileName")
        }
}
