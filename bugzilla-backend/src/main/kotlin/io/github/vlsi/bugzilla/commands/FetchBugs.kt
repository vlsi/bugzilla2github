package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.default
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import com.github.ajalt.clikt.parameters.types.int
import io.github.vlsi.bugzilla.BugSummary
import io.github.vlsi.bugzilla.BugzillaOptionGroup
import io.github.vlsi.bugzilla.DatabaseContext
import io.github.vlsi.bugzilla.crawler.BugzillaClient
import io.github.vlsi.bugzilla.crawler.XdBug
import io.github.vlsi.bugzilla.crawler.model.ShowBugResponse
import io.github.vlsi.bugzilla.runApp
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.datetime.toJavaLocalDateTime
import kotlinx.dnq.creator.findOrNew
import kotlinx.dnq.query.filter
import kotlinx.dnq.query.firstOrNull
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.csv.Csv
import kotlinx.serialization.decodeFromString
import nl.adaptivity.xmlutil.serialization.XML
import org.slf4j.LoggerFactory

@OptIn(ExperimentalSerializationApi::class)
@Deprecated("Fetches data from Bugzilla UI, however it is faster to get the data from the database dump")
class FetchBugs : CliktCommand() {
    companion object {
        val log = LoggerFactory.getLogger(FetchBugs::class.java)
    }

    val bugzillaOptions by BugzillaOptionGroup()
    val limit by option(help = "Number of bugs to retrieve").int().default(10)
    val dataFolder by option(help = "Location of the folder for storing data").file(canBeFile = false).required()

    context(DatabaseContext)
    private fun BugSummary.isUpToDate(): Boolean {
        val bugId = bug_id
        val prevChangeddate = store.xodus.transactional {
            XdBug.filter { it.bug_id eq bugId.value }.firstOrNull()?.changeddate
        }
        return prevChangeddate == changeddate.toJavaLocalDateTime()
    }

    override fun run() {
        runApp(BugzillaClient(bugzillaOptions), dataFolder = dataFolder) {
            runBlocking {
                val bugsToRetrieve = Channel<BugSummary>()

                val bugzillaDispatcher = newSingleThreadContext("bugzilla")

                coroutineScope {
                    launch {
                        val results = bugzilla.listBugs(product = bugzillaOptions.product, limit = limit)
                        val bugs: List<BugSummary> = Csv {
                            hasHeaderRecord = true
                            ignoreUnknownColumns = true
                        }.decodeFromString(results)
                        for (bug in bugs) {
                            if (!bug.isUpToDate()) {
                                bugsToRetrieve.send(bug)
                            }
                        }
                        // No more data, closing the channel
                        bugsToRetrieve.close()
                    }
                    for (i in 1..5) {
                        launch(Dispatchers.Default) {
                            val xml = XML {
                            }
                            for (bugSummary in bugsToRetrieve) {
                                log.info("Processing {}", bugSummary)
                                val bugId = bugSummary.bug_id
                                val bugXml = withContext(bugzillaDispatcher) {
                                    bugzilla.getBugs(listOf(bugId))
                                }
                                store.storeBugXml(bugId, bugXml)
                                store.xodus.transactional {
                                    XdBug.findOrNew {
                                        bug_id = bugId.value
                                    }.apply {
                                        changeddate = bugSummary.changeddate.toJavaLocalDateTime()
                                    }
                                }
                                try {
                                    xml.decodeFromString<ShowBugResponse>(bugXml)
                                } catch (t: Throwable) {
                                    t.addSuppressed(Throwable(bugXml))
                                    t.printStackTrace()
                                }

                                log.info("Received bug {}", bugSummary)
                            }
                        }
                    }
                }
            }
        }
    }
}
