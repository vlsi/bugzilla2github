package io.github.vlsi.bugzilla.github

import kotlinx.serialization.Serializable

@Serializable
class RenderMarkdownRequest(
    val text: String,
    val mode: RenderMarkdownMode,
    val context: String,
)
