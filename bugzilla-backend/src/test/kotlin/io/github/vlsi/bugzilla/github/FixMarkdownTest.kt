package io.github.vlsi.bugzilla.github

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class FixMarkdownTest {
    @Test
    internal fun `bugzilla id`() {
        assertEquals(
            "[Bug 12345](https://bz//show_bug.cgi?id=12345)",
            fixupMarkdown("https://bz/","Bugzilla Id: 12345")
        )
    }
}
