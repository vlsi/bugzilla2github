package io.github.vlsi.bugzilla

import io.github.vlsi.bugzilla.crawler.BugzillaClient
import io.github.vlsi.bugzilla.crawler.XdBug
import io.github.vlsi.bugzilla.dto.BugId
import jetbrains.exodus.database.TransientEntityStore
import kotlinx.dnq.XdModel
import kotlinx.dnq.store.container.StaticStoreContainer
import kotlinx.dnq.util.initMetaData
import java.io.File

interface BugzillaContext {
    val bugzilla: BugzillaClient
}

class DataStore (
    private val dataFolder: File,
) {
    val xodus: TransientEntityStore by lazy {
        XdModel.registerNodes(
            XdBug
        )
        // See https://github.com/JetBrains/xodus-dnq/issues/37 for multiple stores
        StaticStoreContainer.init(dbFolder = dataFolder.resolve("xodus"), entityStoreName = "bugzilla").also {
            initMetaData(XdModel.hierarchy, it)
        }
    }

    private fun xmlLocation(bugId: BugId) : File {
        val suffix = bugId.value.mod(100).toString().padStart(2, '0')
        return dataFolder.resolve("bugs/$suffix/$bugId.xml")
    }

    fun getBugXml(bugId: BugId): String {
        return xmlLocation(bugId).readText()
    }

    fun storeBugXml(bugId: BugId, xml: String) {
        xmlLocation(bugId).apply { parentFile.mkdirs() }.writeText(xml)
    }
}

interface DatabaseContext {
    val store: DataStore
}

interface AppContext: BugzillaContext, DatabaseContext

fun <R> runApp(client: BugzillaClient, dataFolder: File, block: context(AppContext) () -> R): R =
    object: AppContext {
        override val bugzilla = client
        override val store = DataStore(dataFolder = dataFolder)
    }.run(block)
