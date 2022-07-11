package io.github.vlsi.bz

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.core.subcommands

class RootCommand: CliktCommand() {
    override fun run() = Unit
}

fun main(args: Array<String>) =
    RootCommand()
        .subcommands(FetchBugs())
        .main(args)
