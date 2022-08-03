package io.github.vlsi.bugzilla.dbexport

import io.ktor.util.*

data class Link(
    val title: String,
    val url: String
) {
    val markdown: String
        get() = "[${title.replace("]", "\\]")}]($url)"

    val html: String
        get() = "<a href=\"$url\">${title.escapeHTML()}</a>"
}
