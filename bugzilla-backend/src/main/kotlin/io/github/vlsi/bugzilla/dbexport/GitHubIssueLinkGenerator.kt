package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.IssueNumber

class GitHubIssueLinkGenerator(
    val bugzilla: BugzillaLinkGenerator,
    val bugToIssue: Map<BugId, IssueNumber>,
    val organization: String,
    val repository: String,
) {
    fun issueLink(bugId: BugId, markup: Markup, includeBugzilla : Boolean = true) : String {
        val bugzillaLink = bugzilla.linkBug(bugId).let {
            when(markup) {
                Markup.MARKDOWN -> it.markdown
                Markup.HTML -> it.html
            }
        }

        val issueId = bugToIssue[bugId] ?: return bugzillaLink
        if (!includeBugzilla) {
            return "https://github.com/$organization/$repository/issues/${issueId.value}"
        }
        return "https://github.com/$organization/$repository/issues/${issueId.value} ($bugzillaLink)"
    }
}
