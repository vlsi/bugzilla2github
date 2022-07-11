package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
actual value class BugId actual constructor(actual val value: Int)
