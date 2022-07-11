package io.github.vlsi.bugzilla.crawler.model

@JvmInline
@kotlinx.serialization.Serializable
value class Username(val value: String) {
    override fun toString(): String = value.toString()
}
