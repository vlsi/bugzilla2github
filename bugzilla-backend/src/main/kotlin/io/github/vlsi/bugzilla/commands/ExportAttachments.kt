package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.groups.provideDelegate
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import io.github.vlsi.bugzilla.dbexport.*
import io.github.vlsi.bugzilla.dto.AttachId
import io.github.vlsi.bugzilla.dto.BugId
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun attachmentDir(bugId: BugId, attachId: AttachId): String =
    "${bugId.value.rem(100).toString().padStart(2, '0')}/$bugId/$attachId"

class ExportAttachments : CliktCommand(help = """
    Export attachments from Bugzilla database to a local folder
""".trimIndent()) {
    companion object {
        val log = LoggerFactory.getLogger(ExportAttachments::class.java)
    }

    val dbParams by DbParametersGroup()
    val bugzillaParams by BugzillaProductGroup()
    val dataFolder by option(help = "Location of the folder for storing data").file(canBeFile = false).required()

    override fun run() {
        dataFolder.mkdirs()
        dataFolder.resolve(".nojekyll").writeBytes(ByteArray(0))
        dataFolder.resolve("README.md").writeText(
            """
            This repository stores attachments from Bugzilla
            """.trimIndent()
        )
        transaction(dbParams.connect) {
            val attachments = (BugzillaQueries.allBugs(bugzillaParams.product) innerJoin Attachments innerJoin AttachData)
                .slice(
                    Attachments.id,
                    Attachments.bug_id,
                    Attachments.creation_ts,
                    Attachments.filename,
                    Attachments.description,
                    AttachData.thedata
                )
                .selectAll()

            attachments.forEach {
                val bugId = BugId(it[Attachments.bug_id].value)
                val attachId = AttachId(it[Attachments.id].value)
                val parent =
                    dataFolder.resolve(attachmentDir(bugId, attachId)).apply {
                        mkdirs()
                    }
                val file = parent.resolve(it[Attachments.filename])
                val bytes = it[AttachData.thedata].bytes
                log.info(
                    "Writing attachment for bug {}, {} bytes to {}",
                    bugId,
                    bytes.size,
                    file
                )
                file.writeBytes(bytes)
            }
        }
    }
}
