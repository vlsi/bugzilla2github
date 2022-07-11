package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
actual value class BugPriority actual constructor(actual val value: String) {
    override fun toString() = value
}
