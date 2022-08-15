package io.github.vlsi.bugzilla.github

import io.github.vlsi.bugzilla.dbexport.GitHubIssueLinkGenerator
import io.github.vlsi.bugzilla.dbexport.Markup
import io.github.vlsi.bugzilla.dto.BugId

val BUG_LINK_REGEX = Regex("(see:?|bugzilla(?:\\s+id)?:?|bugs?:?|of|in)(?>[ \\t]+#?|#)[ \\t]*([1-9][0-9]{3,4})", RegexOption.IGNORE_CASE)
val COMMENT_ID_REGEX = Regex("( comment)\\s+#(\\d+)", RegexOption.IGNORE_CASE)

fun fixupMarkdown(gitHubIssueLinkGenerator: GitHubIssueLinkGenerator, src: String, bugLinkRegex: Regex = BUG_LINK_REGEX): String {
    // Leading > might mean "quote" in Markdown, so we escape only non-leading >
    return src.replace(Regex("(?<!^)>", RegexOption.MULTILINE), "&gt;")
        .replace("<", "&lt;")
        .replace(bugLinkRegex) { mr ->
            val link = gitHubIssueLinkGenerator.issueLink(BugId(mr.groupValues[2].toInt()), Markup.MARKDOWN)
            if (mr.groupValues[1].startsWith("bug", ignoreCase = true)) {
                link
            } else {
                mr.groupValues[1] + " " + link
            }
        }
        .let { gitHubIssueLinkGenerator.replaceBugzillaLinks(it) }
        .replace(COMMENT_ID_REGEX, "$1 $2")
}
