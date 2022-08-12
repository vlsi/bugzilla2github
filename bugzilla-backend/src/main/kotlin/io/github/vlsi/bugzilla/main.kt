package io.github.vlsi.bugzilla

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.Logger
import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.github.ajalt.clikt.output.CliktHelpFormatter
import com.github.ajalt.clikt.parameters.options.flag
import com.github.ajalt.clikt.parameters.options.option
import com.typesafe.config.ConfigFactory
import io.github.vlsi.bugzilla.commands.Backend
import io.github.vlsi.bugzilla.commands.ExportAttachments
import io.github.vlsi.bugzilla.commands.ImportToGitHub
import io.github.vlsi.bugzilla.dbexport.MapBugsToIssues
import org.slf4j.LoggerFactory

val configuration = ConfigFactory.load()

class RootCommand: CliktCommand() {
    init {
        context {
            valueSource = ConfigValueSource(configuration)
            helpFormatter = CliktHelpFormatter(showDefaultValues = true, showRequiredTag = true, maxWidth = 120)
        }
    }

    val verbose by option("-v", "--verbose", help = "Print verbose output").flag()

    override fun run() {
        if (verbose) {
            (LoggerFactory.getLogger(org.slf4j.Logger.ROOT_LOGGER_NAME) as Logger).apply {
                level = Level.DEBUG
            }
        }
    }
}

fun main(args: Array<String>) =
    RootCommand()
        .subcommands(ExportAttachments())
        .subcommands(MapBugsToIssues())
        .subcommands(Backend())
        .subcommands(ImportToGitHub())
        .main(args)
