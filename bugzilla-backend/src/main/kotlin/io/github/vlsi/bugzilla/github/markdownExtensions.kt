package io.github.vlsi.bugzilla.github

val BUG_LINK_REGEX = Regex("(see:?|bugzilla(?:\\s+id)?:?|bugs?:?|of|in)(?>[ \\t]+#?|#)[ \\t]*([1-9][0-9]{3,4})", RegexOption.IGNORE_CASE)
val COMMENT_ID_REGEX = Regex("(from comment)\\s+#(\\d+)", RegexOption.IGNORE_CASE)

fun fixupMarkdown(bugzillaUrl: String, src: String): String {
    // Leading > might mean "quote" in Markdown, so we escape only non-leading >
    return src.replace(Regex("(?<!^)>", RegexOption.MULTILINE), "&gt;")
        .replace("<", "&lt;")
        // Insert forced line breaks on soft newlines (e.g. newlines without blank lines between them)
        // sample: 65913
//        .replace(Regex("(?<!\\n)\\n(?!\\n)"), "<br/>\n")
        // Remove <br/> from the lines that start with several spaces (they are code blocks,
        // and we don't want to show <br/> for them)
        // Sample: 66070
//        .replace(Regex("^((?>\\t| {4,}).*)<br/>$", RegexOption.MULTILINE), "$1")
        .replace(BUG_LINK_REGEX) { mr ->
            val link = "[Bug ${mr.groupValues[2]}](${bugzillaUrl.removeSuffix("/")}//show_bug.cgi?id=${mr.groupValues[2]})"
            if (mr.groupValues[1].startsWith("bug", ignoreCase = true)) {
                link
            } else {
                mr.groupValues[1] + " " + link
            }
        }
        .replace(COMMENT_ID_REGEX, "$1 $2")
}
