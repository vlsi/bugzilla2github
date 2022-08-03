package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import io.github.vlsi.bugzilla.dbexport.BugLinks
import io.github.vlsi.bugzilla.dbexport.ConvBugIssues
import io.github.vlsi.bugzilla.dbexport.Dependencies
import io.github.vlsi.bugzilla.dbexport.Duplicates
import io.github.vlsi.bugzilla.dto.BugId
import io.github.vlsi.bugzilla.github.IssueNumber
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class DbParametersGroup : OptionGroup() {
    enum class DbType {
        mysql,
    }

    val username by option("--db-username").required()
    val password by option(
        "--db-password",
        envvar = "BUGZILLA_DB_PASSWORD",
        help = "Prefer passing the password via BUGZILLA_DB_PASSWORD environment variable"
    ).defaultLazy("same as db-username") { username }
    val type by option("--db-type").enum<DbType>().default(DbType.mysql)
    val name by option("--db-name").default("bugzilla")
    val host by option("--db-host").default("localhost")
    val port by option("--db-port").int()
        .defaultLazy("3306 for db-type=mysql") {
            when (type) {
                DbType.mysql -> 3306
            }
        }
    val driver by option("--db-driver")
        .defaultLazy("com.mysql.cj.jdbc.Driver for db-type=mysql") {
            when (type) {
                DbType.mysql -> "com.mysql.cj.jdbc.Driver"
            }
        }
    val url by option("--db-url").defaultLazy("jdbc:\${db-type}://\${db-host}:\${db-port}/\${db-name}") {
        when (type) {
            DbType.mysql -> "jdbc:mysql://$host:$port/$name"
        }
    }

    val connect by lazy {
        Database.connect(
            url = url,
            driver = driver,
            user = username,
            password = password,
        )
    }

    val bugToIssue by lazy {
        transaction(connect) {
            ConvBugIssues.selectAll()
                .associateBy({ BugId(it[ConvBugIssues.bug_id].value) }, { IssueNumber(it[ConvBugIssues.issue_number]) })
        }
    }

    val bugLinks by lazy {
        transaction(connect) {
            val duplicates = Duplicates.selectAll()
                .map { BugId(it[Duplicates.dupe].value) to BugId(it[Duplicates.dupe_of].value) }

            val dependencies = Dependencies.selectAll()
                .map { BugId(it[Dependencies.blocked].value) to BugId(it[Dependencies.dependson].value) }

            BugLinks(duplicates, dependencies)
        }
    }
}
