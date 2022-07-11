package io.github.vlsi.bugzilla

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.context
import com.github.ajalt.clikt.core.subcommands
import com.typesafe.config.ConfigFactory
import io.github.vlsi.bugzilla.commands.Backend
import io.github.vlsi.bugzilla.commands.ExportAttachments
import io.github.vlsi.bugzilla.commands.ImportToGitHub

class RootCommand: CliktCommand() {
    init {
        context {
            valueSource = ConfigValueSource(ConfigFactory.load())
        }
    }
    override fun run() = Unit
}

fun main(args: Array<String>) =
    RootCommand()
        .subcommands(ExportAttachments())
        .subcommands(Backend())
        .subcommands(ImportToGitHub())
        .main(args)
