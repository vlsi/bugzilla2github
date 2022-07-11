package io.github.vlsi.bugzilla.dto

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
class Comment(
    val markdown: String,
    val created_when: Instant,
    val author: Profile,
)
