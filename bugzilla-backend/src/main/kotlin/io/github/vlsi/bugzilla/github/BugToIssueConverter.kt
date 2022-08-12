package io.github.vlsi.bugzilla.github

import RenderedComment
import RenderedIssue
import io.github.vlsi.bugzilla.dto.Bug

class BugToIssueConverter {
    private val io.github.vlsi.bugzilla.dto.Comment.githubMarkdown: String
        get() = (author.githubProfile?.let { "@$it" } ?: "**${author.realname}**") + ":\n$markdown"

    fun convert(bug: Bug): ImportIssueRequest =
        ImportIssueRequest(
            issue = Issue(
                title = bug.description,
                body = bug.markdown + "\n\n" + (bug.comments.firstOrNull()?.githubMarkdown ?: ""),
                created_at = bug.creationDate,
                closed_at = bug.closedWhen,
                updated_at = bug.updatedWhen,
                closed = !bug.status.isOpen,
                labels = buildList {
                    add("priority: ${bug.priority.value}")
                    add("severity: ${bug.severity.value}")
                    add("os: ${bug.os.value}")
                    add("status: ${bug.status.value}")
                    add("bugzilla")
                    addAll(bug.keywords.map { "keyword: $it" })
                }
            ),
            comments = bug.comments
                .drop(1)
                .filterNot { it.markdown.isBlank() }
                .map {
                    Comment(
                        created_at = it.created_when,
                        body = it.githubMarkdown,
                    )
                }
        )

    suspend fun render(
        gitHubApi: GitHubApi,
        markdown: ImportIssueRequest,
        organization: String,
        repository: String
    ) = RenderedIssue(
        title = markdown.issue.title,
        closedAt = markdown.issue.closed_at,
        markdown = markdown.issue.body,
        labels = markdown.issue.labels,
        html = gitHubApi.render(
            markdown = markdown.issue.body,
            organization = organization,
            repository = repository
        ),
        comments = markdown.comments.map { comment ->
            RenderedComment(
                created_at = comment.created_at,
                markdown = comment.body,
                html = gitHubApi.render(
                    markdown = comment.body,
                    organization = organization,
                    repository = repository
                ),
            )
        }
    )
}
