package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required

class BugzillaParametersGroup: OptionGroup() {
    val url by option("--bugzilla-url", help = "Bugzilla URL (e.g. https://bz.apache.org/bugzilla/)").required()
}
