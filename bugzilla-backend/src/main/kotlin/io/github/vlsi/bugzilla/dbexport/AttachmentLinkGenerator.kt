package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.AttachId
import io.github.vlsi.bugzilla.dto.BugId

interface AttachmentLinkGenerator {
    fun linkFor(bugId: BugId, attachId: AttachId, filename: String): String
}
