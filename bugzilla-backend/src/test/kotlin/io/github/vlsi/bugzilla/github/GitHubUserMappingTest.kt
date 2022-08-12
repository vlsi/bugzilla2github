package io.github.vlsi.bugzilla.github

import com.typesafe.config.ConfigFactory
import io.github.vlsi.bugzilla.configuration
import io.github.vlsi.bugzilla.dbexport.GitHubUserMapping
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class GitHubUserMappingTest {
    @Test
    internal fun load() {
        val mapping = GitHubUserMapping(
            ConfigFactory.parseMap(
                mapOf(
                    "converter-settings.users" to mapOf(
                        "\"test-user@example.com\"" to "test"
                    )
                )
            )
        )

        assertEquals("test", mapping.gitHubLoginOrNull("test-user@example.com"))
    }
}
