package io.github.vlsi.bz.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class AttachId(val value: Int) {
    override fun toString(): String = value.toString()
}

