package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
expect value class BugPriority(val value: String)
