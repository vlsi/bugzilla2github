package io.github.vlsi.bugzilla.github

import kotlinx.serialization.Serializable

@Serializable
expect value class IssueNumber(val value: Int)
