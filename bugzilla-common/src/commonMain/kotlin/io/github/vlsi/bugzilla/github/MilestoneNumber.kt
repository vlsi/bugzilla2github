package io.github.vlsi.bugzilla.github

import kotlinx.serialization.Serializable

@Serializable
expect value class MilestoneNumber(val value: Int)
