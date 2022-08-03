package io.github.vlsi.bugzilla.github

import kotlinx.serialization.Serializable

@Serializable
actual value class IssueNumber actual constructor(actual val value: Int) {
    override fun toString() = value.toString()
}
