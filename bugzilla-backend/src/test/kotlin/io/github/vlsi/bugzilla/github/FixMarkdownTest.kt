package io.github.vlsi.bugzilla.github

import io.github.vlsi.bugzilla.dbexport.BugzillaLinkGenerator
import io.github.vlsi.bugzilla.dbexport.GitHubIssueLinkGenerator
import io.github.vlsi.bugzilla.dto.BugId
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FixMarkdownTest {
    @Test
    internal fun `bugzilla id`() {
        assertEquals(
            "https://github.com/test-org/repo/issues/42",
            fixupMarkdown(
                GitHubIssueLinkGenerator(
                    BugzillaLinkGenerator("https://bz/"),
                    mapOf(
                        BugId(12345) to IssueNumber(42)
                    ),
                    "test-org",
                    "repo"
                ),
                "Bugzilla Id: 12345"
            )
        )
    }

    @Test
    internal fun `bugzilla url`() {
        assertEquals(
            "See https://github.com/test-org/repo/issues/42, https://github.com/test-org/repo/issues/43",
            fixupMarkdown(
                GitHubIssueLinkGenerator(
                    BugzillaLinkGenerator("https://bz/", listOf("https://issues")),
                    mapOf(
                        BugId(12345) to IssueNumber(42),
                        BugId(12346) to IssueNumber(43),
                    ),
                    "test-org",
                    "repo"
                ),
                "See https://bz/show_bug.cgi?id=12345, https://issues/show_bug.cgi?id=12346"
            )
        )
    }
}
