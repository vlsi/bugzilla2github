package io.github.vlsi.bugzilla.commands

import com.github.ajalt.clikt.core.CliktCommand
import com.github.ajalt.clikt.parameters.options.option
import com.github.ajalt.clikt.parameters.options.required
import com.github.ajalt.clikt.parameters.types.file
import io.github.vlsi.bugzilla.dbexport.AttachData
import io.github.vlsi.bugzilla.dbexport.Attachments
import io.github.vlsi.bugzilla.dbexport.Bugs
import io.github.vlsi.bugzilla.dbexport.Products
import io.github.vlsi.bugzilla.dto.BugId
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction
import org.slf4j.LoggerFactory

fun attachmentDir(bugId: BugId): String =
    "${bugId.value.rem(100).toString().padStart(2, '0')}/${bugId.value}"

class ExportAttachments : CliktCommand(help = """
    Export attachments from Bugzilla database to a local folder
""".trimIndent()) {
    companion object {
        val log = LoggerFactory.getLogger(ExportAttachments::class.java)
    }

    val product by option(help = "Bugzilla product name").required()
    val dataFolder by option(help = "Location of the folder for storing data").file(canBeFile = false).required()

    override fun run() {
        Database.connect(
            "jdbc:mysql://localhost:3306/bugzilla",
            "com.mysql.cj.jdbc.Driver",
            user = "root",
            password = "root"
        )
        dataFolder.mkdirs()
        dataFolder.resolve(".nojekyll").writeBytes(ByteArray(0))
        dataFolder.resolve("README.md").writeText(
            """
            This repository stores attachments from Bugzilla
            """.trimIndent()
        )
        transaction {
            val attachments = (Products innerJoin Bugs innerJoin Attachments innerJoin AttachData)
                .slice(
                    Attachments.id,
                    Attachments.bug_id,
                    Attachments.creation_ts,
                    Attachments.filename,
                    Attachments.description,
                    AttachData.thedata
                )
                .select { Products.name eq product }

            attachments.forEach {
                val parent =
                    dataFolder.resolve(attachmentDir(BugId(it[Attachments.bug_id].value))).apply {
                        mkdirs()
                    }
                val file = parent.resolve("${it[Attachments.id]}-${it[Attachments.filename]}")
                val bytes = it[AttachData.thedata].bytes
                log.info(
                    "Writing attachment for bug {}, {} bytes to {}",
                    it[Attachments.bug_id],
                    bytes.size,
                    file
                )
                file.writeBytes(bytes)
            }
        }
    }
}
