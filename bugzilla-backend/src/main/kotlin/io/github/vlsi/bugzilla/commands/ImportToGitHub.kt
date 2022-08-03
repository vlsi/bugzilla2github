package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.github.vlsi.bugzilla.dbexport.Bugs
import io.github.vlsi.bugzilla.dbexport.BugzillaExporter
import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.BugToIssueConverter
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.slf4j.LoggerFactory

class ImportToGitHub : CliktCommand(name = "import-to-github", help = """
    Import bugs from Bugzilla to GitHub issues starting with given first-bug-id (e.g. to resume from a previous run).
""".trimIndent()) {
    companion object {
        val log = LoggerFactory.getLogger(ImportToGitHub::class.java)
    }

    val dbParams by DbParametersGroup()
    val gitHubParams by GitHubParametersGroup()
    val bugzillaParams by BugzillaParametersGroup()

    val firstBugId by option(
        "--first-bug-id",
        help = "The first bug_id to import (inclusive). The bugs are imported in the order of increasing the bug_id"
    ).int().default(0)

    override fun run() {
        val exporter = BugzillaExporter(
            dbParams.connect,
            bugzillaUrl = bugzillaParams.url,
            attachmentLinkGenerator = gitHubParams.attachmentLinkGenerator
        )
        val converter = BugToIssueConverter()
        val start = Clock.System.now()
        var imported = 0
        runBlocking {
            newSuspendedTransaction(db = dbParams.connect) {
                val totalBugs = Bugs.select { Bugs.id greater firstBugId }.count()
                log.info("Bugs to be migrated {}", totalBugs)
                Bugs.slice(Bugs.id).select { Bugs.id greater firstBugId }.orderBy(Bugs.id).forEach {
                    val bugId = it[Bugs.id].value
                    val bug = exporter.exportToMarkdown(bugId = BugId(bugId))!!
                    val issue = converter.convert(bug)
                    gitHubParams.gitHubApi.startImport(
                        username = gitHubParams.organization,
                        repository = gitHubParams.issuesRepository,
                        issue = issue,
                        waitCompletion = false
                    )
                    imported += 1
                    val duration = Clock.System.now() - start
                    log.info(
                        "Imported bug {}, total {} bugs in {}, estimated time left {}",
                        bugId,
                        imported,
                        duration,
                        duration * ((totalBugs - imported).toDouble() / imported)
                    )
                }
            }
        }
    }
}
