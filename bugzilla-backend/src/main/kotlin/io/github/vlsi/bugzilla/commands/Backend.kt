package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.github.vlsi.bugzilla.configuration
import io.github.vlsi.bugzilla.dbexport.BugzillaExporter
import io.github.vlsi.bugzilla.dbexport.GitHubUserMapping
import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.dto.MigrateStatusResponse
import io.github.vlsi.bugzilla.dto.StartMigrateResponse
import io.github.vlsi.bugzilla.github.BugToIssueConverter
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.engine.*
import io.ktor.server.http.content.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.launch
import org.jetbrains.exposed.sql.Database
import org.slf4j.LoggerFactory

class Backend : CliktCommand(help = """
    Spawn a web server that displays bugs from Bugzilla database for preview purposes before data hits GitHub.

    Note: markdown to HTML conversion uses GitHub /markdown API, so you need to specify GITHUB_TOKEN for it to work.
""".trimIndent()) {
    companion object {
        val log = LoggerFactory.getLogger(Backend::class.java)
    }

    val port: Int by option(help = "Port to listen on", valueSourceKey = "ui.port").int().default(8080)
    val host: String by option(help = "Host to listen on", valueSourceKey = "ui.host").default("0.0.0.0")
    val dbParams by DbParametersGroup()
    val bugzillaParams by BugzillaUrlGroup()
    val gitHubParams by GitHubWithAttachmentsParametersGroup()

    override fun run() {
        embeddedServer(Netty, port = port, host = host) {
            install(ContentNegotiation) {
                json()
            }

            launch {
                Database.connect(
                    "jdbc:mysql://localhost:3306/bugzilla",
                    "com.mysql.cj.jdbc.Driver",
                    user = "root",
                    password = "root"
                )
            }
            routing {
                get("/hi") {
                    call.respondText("Hello, world!")
                }
                route("/api") {
                    bugRoutes(dbParams, bugzillaParams, gitHubParams)
                    migrationRoutes()
                }
                static("/") {
                    staticBasePackage = "static"
                    defaultResource("index.html")
                    resource("bugzilla-frontend.js")
                }
            }
        }.start(wait = true)
    }
}

fun Route.migrationRoutes() {
    route("/migration") {
        get("/status") {
            call.respond(MigrateStatusResponse(isRunning = false))
        }
        post("/start") {
            call.respond(StartMigrateResponse())
        }
    }
}

fun Route.bugRoutes(
    dbParams: DbParametersGroup,
    bugzillaParams: BugzillaUrlGroup,
    gitHubParams: GitHubWithAttachmentsParametersGroup
) {
    route("/bug") {
        val gitHubUserMapping = GitHubUserMapping(configuration)
        get {
            val id = call.parameters["id"]?.toIntOrNull() ?: return@get call.respondText(
                "Missing id",
                status = HttpStatusCode.BadRequest
            )
            val exporter = BugzillaExporter(
                dbParams.connect,
                dbParams.bugLinks,
                gitHubParams.issueLinkGenerator(bugzillaParams.linkGenerator, dbParams.bugToIssue),
                gitHubParams.attachmentLinkGenerator,
                gitHubUserMapping,
            )
            val dto = exporter.exportToMarkdown(BugId(id))
            if (dto == null) {
                call.respondText("No such bug", status = HttpStatusCode.NotFound)
            } else {
                // TODO: add milestone map
                val converter = BugToIssueConverter(
                    milestones = mapOf(),
                    bugzillaLinkGenerator = bugzillaParams.linkGenerator
                )
                val gfm = converter.convert(dto)
                call.respond(
                    converter.render(
                        gitHubApi = gitHubParams.gitHubApi,
                        markdown = gfm,
                        organization = gitHubParams.organization,
                        repository = gitHubParams.issuesRepository
                    )
                )
            }
        }
    }
}
