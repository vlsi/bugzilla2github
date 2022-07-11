package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
class BugMarkup(
    val bugId: BugId,
    val description: String,
    val html: String,
)
