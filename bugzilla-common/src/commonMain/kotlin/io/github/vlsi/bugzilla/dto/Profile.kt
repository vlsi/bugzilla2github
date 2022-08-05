package io.github.vlsi.bugzilla.dto

import kotlinx.serialization.Serializable

@Serializable
data class Profile(
    val login: String,
    val realname: String,
)
