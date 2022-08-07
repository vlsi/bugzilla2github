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
            "#42",
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
}
