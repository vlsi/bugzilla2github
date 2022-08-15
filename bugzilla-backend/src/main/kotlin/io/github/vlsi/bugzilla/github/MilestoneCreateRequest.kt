package io.github.vlsi.bugzilla.github

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class MilestoneCreateRequest(
    val title: String,
    val state: MilestoneState = MilestoneState.open,
    val description: String? = null,
    val due_on: Instant? = null,
)
