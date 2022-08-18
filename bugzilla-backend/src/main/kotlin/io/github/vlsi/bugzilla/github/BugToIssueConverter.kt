package io.github.vlsi.bugzilla.github

import RenderedComment
import RenderedIssue
import io.github.vlsi.bugzilla.dbexport.BugzillaLinkGenerator
import io.github.vlsi.bugzilla.dto.Bug
import io.github.vlsi.bugzilla.dto.BugId

class BugToIssueConverter(
    val milestones: Map<String, Milestone>,
    val bugzillaLinkGenerator: BugzillaLinkGenerator,
) {
    private fun io.github.vlsi.bugzilla.dto.Comment.githubMarkdown(bugId: BugId): String =
        (author.githubProfile?.let { "@$it" } ?: "**${author.realname}**") +
                if (index == 0) {
                    " (" + bugzillaLinkGenerator.linkBug(bugId, "Bug $bugId").markdown + ")"
                } else {
                    " (" + bugzillaLinkGenerator.linkComment(bugId, index, "migrated from Bugzilla").markdown + ")"
                } +
                ":\n$markdown"

    fun convert(bug: Bug): ImportIssueRequest =
        ImportIssueRequest(
            issue = Issue(
                title = bug.description,
                body = listOfNotNull(bug.header, bug.comments.firstOrNull()?.githubMarkdown(bug.bugId), bug.footer)
                    .joinToString("\n\n"),
                created_at = bug.creationDate,
                closed_at = bug.closedWhen,
                updated_at = bug.updatedWhen,
                closed = !bug.status.isOpen,
                milestone = bug.targetMilestone?.let { milestones[it]?.number },
                labels = buildList {
                    add("${bug.priority}")
                    if (bug.severity.value == "enhancement" || bug.severity.value == "regression") {
                        add(bug.severity.value)
                    }
                    bug.os?.let {
                        val os = when {
                            it.value.equals("All", ignoreCase = true) -> "All"
                            it.value.startsWith("Win", ignoreCase = true) -> "Windows"
                            it.value.startsWith("Mac", ignoreCase = true) -> "macOS"
                            it.value.startsWith("Linux", ignoreCase = true) -> "Linux"
                            else -> "other"
                        }

                        add("os: $os")
                    }
                    if (bug.status.value == "NEEDINFO") {
                        add("need-info")
                    }
                    bug.resolution?.let {
                        when(it) {
                            "FIXED", "CLOSED" -> {
                                // ignore, the resolutions are mapped to the issue status
                            }
                            else -> add("resolution: $it")
                        }
                    }
                    addAll(bug.keywords.map { "keyword: $it" })
                }
            ),
            comments = bug.comments
                .drop(1)
                .filterNot { it.markdown.isBlank() }
                .map {
                    Comment(
                        created_at = it.created_when,
                        body = it.githubMarkdown(bug.bugId),
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
