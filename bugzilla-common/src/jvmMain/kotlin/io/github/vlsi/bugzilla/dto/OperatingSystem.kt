package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
actual value class OperatingSystem actual constructor(actual val value: String) {
    override fun toString() = value
}
