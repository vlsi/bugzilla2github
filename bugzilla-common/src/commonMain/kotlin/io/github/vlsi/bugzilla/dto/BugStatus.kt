package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
data class BugStatus(
    val value: String,
    val isOpen: Boolean,
)
