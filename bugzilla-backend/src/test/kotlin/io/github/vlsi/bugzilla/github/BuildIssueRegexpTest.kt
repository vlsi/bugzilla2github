package io.github.vlsi.bugzilla.github

import io.github.vlsi.bugzilla.dbexport.regexpForStrings
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class BuildIssueRegexpTest {
    @Test
    internal fun escapingTest() {
        assertEquals("(?>\\Q*\\E|a)", regexpForStrings(listOf("a", "*").sorted()))
    }

    @Test
    internal fun a() {
        assertEquals("a", regexpForStrings(listOf("a").sorted()))
    }

    @Test
    internal fun `a b`() {
        assertEquals("[ab]", regexpForStrings(listOf("a", "b").sorted()))
    }

    @Test
    internal fun `a ab`() {
        assertEquals("ab?", regexpForStrings(listOf("a", "ab").sorted()))
    }

    @Test
    internal fun `a ab ac`() {
        assertEquals("a[bc]?", regexpForStrings(listOf("a", "ab", "ac").sorted()))
    }

    @Test
    internal fun `a ab abc`() {
        assertEquals("a(?:bc?)?", regexpForStrings(listOf("a", "ab", "abc").sorted()))
    }

    @Test
    internal fun `abc bc c`() {
        assertEquals("(?>abc|bc|c)", regexpForStrings(listOf("abc", "bc", "c").sorted()))
    }

    @Test
    internal fun `ab ac ad`() {
        assertEquals("a[bcd]", regexpForStrings(listOf("ab", "ac", "ad").sorted()))
    }
}
