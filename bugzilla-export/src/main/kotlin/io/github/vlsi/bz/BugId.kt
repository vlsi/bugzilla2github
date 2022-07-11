package io.github.vlsi.bz

@JvmInline
@kotlinx.serialization.Serializable
value class BugId(val value: Int) {
    override fun toString(): String = value.toString()
}
