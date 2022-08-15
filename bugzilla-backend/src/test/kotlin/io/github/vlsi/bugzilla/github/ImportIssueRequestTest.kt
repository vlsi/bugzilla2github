package io.github.vlsi.bugzilla.github

import kotlinx.datetime.Instant
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ImportIssueRequestTest {
    @Test
    internal fun name() {
        val json = Json {
            prettyPrint = true
        }

        val str = json.encodeToString(
            ImportIssueRequest(
                issue = Issue(
                    title = "issue title",
                    body = "issue body",
                    milestone = MilestoneNumber(1),
                    closed = false,
                ),
                comments = listOf(
                    Comment(
                        body = "Comment 1"
                    ),
                    Comment(
                        created_at = Instant.fromEpochMilliseconds(1659123774588),
                        body = "Comment 2"
                    ),
                )
            )
        )

        assertEquals(/* language=json */
            """
            {
                "issue": {
                    "title": "issue title",
                    "milestone": 1,
                    "closed": false,
                    "body": "issue body"
                },
                "comments": [
                    {
                        "body": "Comment 1"
                    },
                    {
                        "created_at": "2022-07-29T19:42:54.588Z",
                        "body": "Comment 2"
                    }
                ]
            }
            """.trimIndent(),
            str
        )
    }
}
