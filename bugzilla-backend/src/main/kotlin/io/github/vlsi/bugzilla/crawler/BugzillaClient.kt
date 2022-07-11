package io.github.vlsi.bugzilla.crawler

import io.github.vlsi.bugzilla.BugzillaOptions
import io.github.vlsi.bugzilla.dto.BugId
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

class BugzillaClient(var options: BugzillaOptions) {
    val client = HttpClient(CIO) {
        engine {
            maxConnectionsCount = options.maxConnections
        }
    }

    suspend fun listBugs(product: String, limit: Int): String {
        val results = client.get(options.host) {
            url {
                appendPathSegments("buglist.cgi")
                parameters.run {
                    append("product", product)
                    append("query_format", "advanced")
                    append("limit", limit.toString())
                    append("ctype", "csv")
                }
            }
        }
        return results.bodyAsText()
    }

    suspend fun getBugs(bugId: List<BugId>): String {
        val results = client.get(options.host) {
            url {
                appendPathSegments("show_bug.cgi")
                parameters.run {
                    appendAll("id", bugId.map { it.value.toString() })
                    append("ctype", "xml")
                }
            }
        }
        return results.bodyAsText()
    }
}
