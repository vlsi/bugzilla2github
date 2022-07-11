package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
actual value class BugSeverity actual constructor(actual val value: String) {
    override fun toString() = value
}
