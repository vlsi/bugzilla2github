package io.github.vlsi.bugzilla.crawler.model

import kotlinx.serialization.Serializable

@JvmInline
@Serializable
value class CommentId(val value: Int) {
    override fun toString(): String = value.toString()
}

