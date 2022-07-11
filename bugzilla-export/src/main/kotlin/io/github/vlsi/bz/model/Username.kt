package io.github.vlsi.bz.model

@JvmInline
@kotlinx.serialization.Serializable
value class Username(val value: String) {
    override fun toString(): String = value.toString()
}
