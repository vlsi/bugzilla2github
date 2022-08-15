package io.github.vlsi.bugzilla.github

import kotlinx.serialization.Serializable

@Serializable
enum class RenderMarkdownMode {
    markdown, gfm;
}
