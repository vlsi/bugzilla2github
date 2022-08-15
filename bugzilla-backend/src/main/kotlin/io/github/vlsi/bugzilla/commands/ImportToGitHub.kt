package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.types.int
import io.github.vlsi.bugzilla.configuration
import io.github.vlsi.bugzilla.dbexport.*
import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.BugToIssueConverter
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.booleanLiteral
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
    val gitHubParams by GitHubWithAttachmentsParametersGroup()
    val bugzillaParams by BugzillaUrlAndProduct()
    val dryRun by option("--dry-run", help = "Don't actually import anything to GitHub").flag()

    val firstBugId by option(
        help = "The first bug_id to import (exclusive). The bugs are imported in the order of increasing the bug_id"
    ).int().default(0)

    val lastBugId by option(
        help = "The last bug_id to import (inclusive). The bugs are imported in the order of increasing the bug_id"
    ).int()

    override fun run() {
        val milestones = runBlocking {
            prepareMilestones(
                gitHubParams,
                dbParams,
                dryRun,
            )
        }

        val exporter = BugzillaExporter(
            dbParams.connect,
            dbParams.bugLinks,
            gitHubLinkGenerator = gitHubParams.issueLinkGenerator(bugzillaParams.linkGenerator, dbParams.bugToIssue),
            attachmentLinkGenerator = gitHubParams.attachmentLinkGenerator,
            gitHubUserMapping = GitHubUserMapping(configuration),
        )
        val converter = BugToIssueConverter(
            milestones = milestones,
            bugzillaLinkGenerator = bugzillaParams.linkGenerator,
        )
        val start = Clock.System.now()
        var imported = 0
        runBlocking {
            newSuspendedTransaction(db = dbParams.connect) {
                val bugsToImport =
                    BugzillaQueries.allBugs(bugzillaParams.product, includeIssueNumber = true)
                        .slice(Bugs.id, ConvBugIssues.issue_number)
                        .select { Bugs.id greater firstBugId and (lastBugId?.let { Bugs.id lessEq it } ?: booleanLiteral(true)) }
                val totalBugs = bugsToImport.count()
                log.info("Number of bugs to be migrated {}", totalBugs)
                bugsToImport.orderBy(Bugs.id).forEach {
                    val bugId = it[Bugs.id].value
                    val bug = exporter.exportToMarkdown(bugId = BugId(bugId))!!
                    val issue = converter.convert(bug)
                    if (dryRun) {
                        log.info("Dry run: skipping import issue {}", issue)
                    } else {
                        gitHubParams.gitHubApi.startImport(
                            username = gitHubParams.organization,
                            repository = gitHubParams.issuesRepository,
                            issue = issue,
                            waitCompletion = true
                        )
                    }
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
