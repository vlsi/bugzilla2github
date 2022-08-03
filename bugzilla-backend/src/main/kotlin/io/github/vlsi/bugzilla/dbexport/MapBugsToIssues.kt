package io.github.vlsi.bugzilla.dbexport

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import io.github.vlsi.bugzilla.commands.BugzillaProductGroup
import io.github.vlsi.bugzilla.commands.DbParametersGroup
import io.github.vlsi.bugzilla.commands.GitHubParametersGroup
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

class MapBugsToIssues : CliktCommand(
    help = """
    Fetches the latest issue id from GitHub and builds conv_bug_issues mapping in the database
""".trimIndent()
) {
    val dbParams by DbParametersGroup()
    val gitHubParams by GitHubParametersGroup()
    val bugzillaParams by BugzillaProductGroup()

    val latestIssueNumber by option(help = "The number of the latest existing issue (0 if repository contains no issues and no pull requests")

    override fun run() {
        transaction(dbParams.connect) {
            SchemaUtils.create(ConvBugIssues)
        }
        transaction(dbParams.connect) {
            ConvBugIssues.deleteAll()

            val rowNum = ROW_NUMBER.orderBy(Bugs.id)

            ConvBugIssues.batchInsert(
                BugzillaQueries.allBugs(bugzillaParams.product)
                    .slice(Bugs.id, rowNum)
                    .selectAll()
                    .orderBy(Bugs.id)
            ) {
                this[ConvBugIssues.bug_id] = it[Bugs.id]
                this[ConvBugIssues.issue_number] = (latestIssueNumber + it[rowNum]).toInt()
            }
        }
    }
}
