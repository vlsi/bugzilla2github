package io.github.vlsi.bz

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.int
import io.ktor.client.*
import io.ktor.client.engine.cio.*

interface BugzillaOptions {
    val login: String
    val maxConnections: Int
    val host: String
}

class BugzillaOptionGroup : OptionGroup(), BugzillaOptions {
    override val login by option(help = "Bugzilla login").required()
    override val maxConnections by option(help = "Maximum number of connections to use for Bugzilla").int().default(10)
    override val host by option(help = "Bugzilla URL (e.g. https://bz.apache.org)").required()
    val product by option(help = "Bugzilla product name").required()

    val client by lazy {
        HttpClient(CIO) {
            engine {
                maxConnectionsCount = maxConnections
            }
        }
    }
}
