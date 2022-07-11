package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.*
import io.github.vlsi.bugzilla.github.fixupMarkdown
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.github.vlsi.bugzilla.dto.BugStatus as BugStatusDto

class BugzillaExporter(
    private val db: Database,
    private val bugzillaUrl: String,
    private val attachmentLinkGenerator: AttachmentLinkGenerator,
) {
    val statuses = transaction(db) {
        BugStatuses.selectAll()
            .map {
                BugStatusDto(
                    value = it[BugStatuses.id].value,
                    isOpen = it[BugStatuses.is_open]
                )
            }
            .associateBy { it.value }
    }

    fun exportToMarkdown(bugId: BugId): Bug? =
        transaction(db) {
            val bug_closed_when = wrapAsExpression<Instant>(
                (FieldDefs innerJoin BugsActivity.join(
                    BugStatuses,
                    joinType = JoinType.INNER,
                    onColumn = BugsActivity.added,
                    otherColumn = BugStatuses.id,
                    additionalConstraint = {
                        BugStatuses.is_open as ExpressionWithColumnType<Int> eq 0
                    }
                ))
                    .slice(BugsActivity.bug_when.max())
                    .select { FieldDefs.name eq Bugs.bug_status.name and (BugsActivity.bug_id eq Bugs.id) }
            ).castTo<Instant?>(BugsActivity.bug_when.columnType)
                .alias("closed_when")
            val bug = Bugs.select { Bugs.id eq bugId.value }
                .adjustSlice { _ ->
                    slice(this.fields + bug_closed_when)
                }
                .firstOrNull()

//            val attachLength = CustomLongFunction("length", AttachData.thedata).alias("attach_length")

            bug?.let {
                Bug(
                    bugId = bugId,
                    description = it[Bugs.short_desc],
                    priority = it[Bugs.priority].let { BugPriority(it.value) },
                    severity = BugSeverity(it[Bugs.bug_severity].value),
                    status = it[Bugs.bug_status].value.let { statuses.getValue(it) },
                    creationDate = it[Bugs.creation_ts],
                    closedWhen = it[bug_closed_when] ?: it[Bugs.lastdiffed],
                    updatedWhen = it[Bugs.lastdiffed],
                    os = it[Bugs.op_sys].let { OperatingSystem(it.value) },
                    markdown = listOf(
                        "Migrated from" to "<a href='${bugzillaUrl.removeSuffix("/")}//show_bug.cgi?id=${bugId.value}'>Bug ${bugId.value}</a>",
                        // "Migrated from" to fixupMarkdown(bugzillaUrl, "Bug ${bugId.value}"),
                        "Resolution" to it[Bugs.resolution].takeIf { it.isNotBlank() },
                        "Version" to it[Bugs.version]?.takeIf { it != "unspecified" && it.startsWith("Nightly") },
                        "Target milestone" to it[Bugs.target_milestone].takeIf { it != "---" },
                        "Votes in Bugzilla" to it[Bugs.votes].takeIf { it > 0 }
                    ).filter { it.second != null && it.second.toString().isNotBlank() }
                        .joinToString(
                            separator = "\n",
                            prefix = "<table>\n",
                            postfix = "</table>\n"
                        ) { "<tr><th>${it.first}</th><td>${it.second}</td></tr>" },
                    comments = (LongDescs innerJoin Profiles)
                        .join(
                            Attachments,
                            joinType = JoinType.LEFT,
                            onColumn = LongDescs.extra_data,
                            otherColumn = Attachments.id
                        ) {
                            LongDescs.type eq CommentTypes.ATTACHMENT_CREATED
                        }
                        .join(
                            AttachData,
                            joinType = JoinType.LEFT,
                            onColumn = Attachments.id,
                            otherColumn = AttachData.id
                        ) {
                            Attachments.mimetype notLike "video/%" and (
                                    Attachments.mimetype notLike "audio/%"
                                    ) and (
                                    Attachments.mimetype notLike "%zip%"
                                    ) and (
                                    Attachments.mimetype notLike "%tar%"
                                    ) and (
                                    Attachments.mimetype notLike "%rar%"
                                    ) and (
                                    Attachments.mimetype notLike "%compressed%"
                                    ) and (
                                    Attachments.mimetype notLike "%archive%"
                                    ) and (
                                    Attachments.mimetype notLike "%binary%"
                                    ) and (
                                    Attachments.mimetype notLike "%octet%"
                                    ) and (
                                    Attachments.mimetype notLike "application/vnd%"
                                    )
                        }
                        .select { LongDescs.bug_id eq bugId.value }
//                        .adjustSlice {
//                            slice(fields - AttachData.thedata + attachLength)
//                        }
                        .orderBy(LongDescs.bug_when)
                        .map {
                            Comment(
                                created_when = it[LongDescs.bug_when],
                                markdown =
                                buildString {
                                    if (it[LongDescs.type] == CommentTypes.ATTACHMENT_CREATED) {
                                        append(addAttachment(bugId, it))
                                    }
                                    val thetext = it[LongDescs.thetext]
                                    if (thetext.isNotBlank()) {
                                        if (isNotEmpty()) {
                                            append("\n\n")
                                        }
                                        append(fixupMarkdown(bugzillaUrl, thetext))
                                    }
                                },
                                author = Profile(
                                    login = it[Profiles.login_name],
                                    realname = it[Profiles.realname].ifBlank {
                                        it[Profiles.login_name].substringBefore('@')
                                    }
                                )
                            )
                        }
                )
            }
        }

    private fun addAttachment(bugId: BugId, row: ResultRow): String? {
        val fileName = row[Attachments.filename]
        val res = StringBuilder()
        val attachId = row[Attachments.id]?.value?.let { AttachId(it) } ?: return null

        val attachmentLink = attachmentLinkGenerator.linkFor(
            bugId,
            attachId,
            fileName
        )
        // TODO: escape description
        res.append(
            "Created attachment [${fileName.replace("]", "\\]")}]($attachmentLink) (${row[Attachments.description]})"
        )
        val thedata = row[AttachData.thedata]
        val mimetype = row[Attachments.mimetype]
        if (thedata != null && thedata.bytes.size < 10000 && (
                    mimetype.startsWith("text/") ||
                            mimetype == "application/xml" ||
                            mimetype == "application/json" ||
                            mimetype == "application/php" ||
                            mimetype == "application/perl" ||
                            mimetype == "application/x-extension-jmx" ||
                            mimetype.endsWith("php") ||
                            mimetype.endsWith("") ||
                            mimetype.endsWith("jmx")

                    )
        ) {
            res.append("\n\n")
            res.append("Preview of ```$fileName```:\n")
            res.append("\n```")
            res.append(
                when {
                    mimetype.endsWith("jmx") -> "xml"
                    row[Attachments.ispatch] -> "diff"
                    mimetype == "text/plain" -> ""
                    mimetype == "text/doc" -> ""
                    mimetype == "text/1" -> ""
                    mimetype.startsWith("text") ->
                        mimetype.removePrefix("text/x-").removePrefix("text/")

                    mimetype.startsWith("application/") ->
                        mimetype.removePrefix("application/x-").removePrefix("application/")

                    else -> ""
                }
            )
            res.append("\n")
            res.append(thedata.bytes.toString(Charsets.UTF_8))
            res.append("```")
        } else if (mimetype.startsWith("image/")) {
            // TODO: add description, image dimensions
            res.append("\n\n")
            res.append("<img src='$attachmentLink'>")
        } else if (mimetype.startsWith("video/")) {
            // TODO: add description, image dimensions
            res.append("\n\n")
            res.append("<video src='$attachmentLink'>")
        }
        return res.toString()
    }
}

