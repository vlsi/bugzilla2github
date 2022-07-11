package io.github.vlsi.bugzilla.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Bug(
    val bugId: BugId,
    val markdown: String,
    val description: String,
    val priority: BugPriority,
    val severity: BugSeverity,
    val status: BugStatus,
    val creationDate: Instant?,
    val closedWhen: Instant?,
    val updatedWhen: Instant?,
    val os: OperatingSystem,
    val comments: List<Comment>,
)
