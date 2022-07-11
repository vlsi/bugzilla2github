package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
class MigrateStatusResponse(
    val isRunning: Boolean,
)
