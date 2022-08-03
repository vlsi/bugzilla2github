package io.github.vlsi.bugzilla.github

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

// See https://gist.github.com/jonmagic/5282384165e0f86ef105#supported-issue-and-comment-fields

@Serializable
data class ImportIssueRequest(
    val issue: Issue,
    val comments: List<Comment> = listOf()
)

@Serializable
data class IssueImportStatusResponse(
    val id: Int,
    val status: String,
    val url: String,
)

@Serializable
data class Issue(
    val title: String,
    val created_at: Instant? = null,
    val closed_at: Instant? = null,
    val updated_at: Instant? = null,
    val assignee: String? = null,
    val milestone: Int? = null,
    val closed: Boolean? = null,
    val labels: List<String> = listOf(),
    val body: String,
)

@Serializable
data class Comment(
    val created_at: Instant? = null,
    val body: String,
)
