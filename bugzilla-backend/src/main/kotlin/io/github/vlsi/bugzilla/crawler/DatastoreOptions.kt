package io.github.vlsi.bugzilla.crawler

import com.github.ajalt.clikt.parameters.groups.OptionGroup
import com.github.ajalt.clikt.parameters.options.option

class DatastoreOptions: OptionGroup() {
    val datastore by option("--datastore", help = "Location of the folder to store the data")
}
