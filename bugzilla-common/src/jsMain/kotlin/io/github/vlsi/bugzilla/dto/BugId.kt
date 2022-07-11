package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
actual value class BugId actual constructor(actual val value: Int) {
    override fun toString() = value.toString()
}
