package io.github.vlsi.bugzilla.github

import kotlinx.serialization.Serializable

@Serializable
data class Milestone(
    val number: MilestoneNumber,
    val title: String,
)
