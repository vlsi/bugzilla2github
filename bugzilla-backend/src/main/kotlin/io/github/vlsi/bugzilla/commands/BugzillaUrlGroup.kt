package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.multiple
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import io.github.vlsi.bugzilla.dbexport.BugzillaLinkGenerator

open class BugzillaParametersGroup: OptionGroup("Bugzilla parameters")

open class BugzillaProductGroup: BugzillaParametersGroup() {
    val product by option("--bugzilla-product", help = "Bugzilla product name").required()
}

open class BugzillaUrlGroup: BugzillaParametersGroup() {
    val url by option("--bugzilla-url", help = "Bugzilla URL (e.g. https://bz.apache.org/bugzilla/)").required()
    val alternativeUrls by option("--bugzilla-alternative-url", help = "Alternative URLs for Bugzilla").multiple()

    val linkGenerator by lazy {
        BugzillaLinkGenerator(
            url,
            alternativeUrls,
        )
    }
}

open class BugzillaUrlAndProduct: BugzillaUrlGroup() {
    val product by option("--bugzilla-product", help = "Bugzilla product name").required()
}
