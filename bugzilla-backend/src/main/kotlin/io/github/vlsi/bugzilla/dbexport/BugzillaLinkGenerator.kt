package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.BugId

class BugzillaLinkGenerator(
    val bugzillaUrl: String
) {
    fun linkBug(bugId: BugId, title: String = "Bug $bugId") =
        Link(title, "$bugzillaUrl/show_bug.cgi?id=$bugId")
}
