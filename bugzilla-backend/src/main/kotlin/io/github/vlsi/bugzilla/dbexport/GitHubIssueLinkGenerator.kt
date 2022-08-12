package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.IssueNumber

class GitHubIssueLinkGenerator(
    val bugzilla: BugzillaLinkGenerator,
    val bugToIssue: Map<BugId, IssueNumber>,
    val organization: String,
    val repository: String,
) {
    val bugLinkRegex = Regex(
        (bugzilla.alternativeUrls + bugzilla.bugzillaUrl).toSet()
            .map { Regex.escape(it.removeSuffix("/")) }
            .joinToString("|", prefix = "(?>", postfix = ")") + "/show_bug\\.cgi\\?id=(\\d+)"
    )

    fun replaceBugzillaLinks(text: String) =
        text.replace(bugLinkRegex) {
            val bugId = BugId(it.groupValues[1].toInt())
            bugToIssue[bugId]?.let { "#$it" } ?: it.value
        }

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
