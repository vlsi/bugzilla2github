package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.defaultLazy
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.enum
import com.github.ajalt.clikt.parameters.types.int
import org.jetbrains.exposed.sql.Database

class DbParametersGroup : OptionGroup() {
    enum class DbType {
        mysql,
    }

    val username by option("--db-username").required()
    val password by option(
        "--db-password",
        envvar = "BUGZILLA_DB_PASSWORD",
        help = "Prefer passing the password via BUGZILLA_DB_PASSWORD environment variable"
    ).defaultLazy { username }
    val type by option("--db-type").enum<DbType>().default(DbType.mysql)
    val name by option("--db-name").default("bugzilla")
    val host by option("--db-host").default("localhost")
    val port by option("--db-port").int()
        .defaultLazy {
            when (type) {
                DbType.mysql -> 3306
            }
        }
    val driver by option("--db-driver")
        .defaultLazy {
            when (type) {
                DbType.mysql -> "com.mysql.cj.jdbc.Driver"
            }
        }
    val url by option("--db-url").defaultLazy {
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
}
