package io.github.vlsi.bugzilla.github

import io.github.vlsi.bugzilla.dbexport.GitHubPagesAttachmentLinkGenerator
import io.github.vlsi.bugzilla.dto.AttachId
import io.github.vlsi.bugzilla.dto.BugId
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GitHubPagesAttachmentLinkGeneratorTest {
    @Test
    internal fun percentEscape() {
        val gen = GitHubPagesAttachmentLinkGenerator(
            "org",
            "repo"
        )
        assertEquals(
            "https://org.github.io/repo/42/45642/123/test%2520file.png",
            gen.linkFor(BugId(45642), AttachId(123), "test%20file.png")
        )
    }
}
