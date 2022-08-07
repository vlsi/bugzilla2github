package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.IssueNumber

class GitHubIssueLinkGenerator(
    val bugzilla: BugzillaLinkGenerator,
    val bugToIssue: Map<BugId, IssueNumber>,
    val organization: String,
    val repository: String,
) {
    fun issueLink(bugId: BugId, markup: Markup) : String {
        val issueId = bugToIssue[bugId] ?: return bugzilla.linkBug(bugId).let {
            when(markup) {
                Markup.MARKDOWN -> it.markdown
                Markup.HTML -> it.html
            }
        }
        return when(markup) {
            Markup.MARKDOWN -> "#$issueId"
            Markup.HTML -> "https://github.com/$organization/$repository/issues/$issueId"
        }
    }
}
