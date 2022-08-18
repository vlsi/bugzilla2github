package io.github.vlsi.bugzilla.dbexport

import io.github.vlsi.bugzilla.dto.*
import io.github.vlsi.bugzilla.github.fixupMarkdown
import io.ktor.util.*
import kotlinx.datetime.Instant
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction
import io.github.vlsi.bugzilla.dto.BugStatus as BugStatusDto

class BugzillaExporter(
    private val db: Database,
    private val bugLinks: BugLinks,
    private val gitHubLinkGenerator: GitHubIssueLinkGenerator,
    private val attachmentLinkGenerator: AttachmentLinkGenerator,
    private val gitHubUserMapping: GitHubUserMapping,
) {
    companion object {
        val extToLanguage = mapOf(
            "bat" to "batch",
            "diff" to "diff",
            "html" to "html",
            "java" to "java",
            "jmx" to "xml",
            "js" to "js",
            "json" to "json",
            "patch" to "patch",
            "perl" to "perl",
            "php" to "php",
            "pl" to "perl",
            "properties" to "properties",
            "py" to "python",
            "sh" to "sh",
            "xml" to "xml",
            "xsl" to "xsl",
        )
        val imageExtensions = setOf(
            "bmp",
            "gif",
            "jpeg",
            "jpg",
            "png",
            "svg",
            "tiff",
        )
        val binaryExtensions = setOf(
            "7z",
            "bz2",
            "gz",
            "jar",
            "rar",
            "tgz",
            "zip",
        )
    }

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

    private fun List<BugId>.issueList(markup: Markup = Markup.MARKDOWN): String {
        val body = joinToString(separator = "\n") {
            "* " + gitHubLinkGenerator.issueLink(it, markup)
        }
        return when {
            size < 15 -> body
            else -> "<details><summary>Show details\n\n" +
                    joinToString(", ") { gitHubLinkGenerator.issueLink(it, markup = markup) } +
                    "</summary>" +
                    body +
                    "</details>"
        }

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
                    keywords = (Keywords innerJoin KeywordDefs).slice(KeywordDefs.name)
                        .select { Keywords.bug_id eq bugId.value }
                        .distinct()
                        .map { it[KeywordDefs.name] }
                        .sorted(),
                    os = it[Bugs.op_sys]?.takeIf { it.isNotBlank() }?.let { OperatingSystem(it) },
                    targetMilestone = it[Bugs.target_milestone].takeIf { it != "---" },
                    resolution = it[Bugs.resolution].takeIf { it.isNotBlank() },
                    header = null,
                    footer = listOfNotNull(
                        it.getOrNull(Bugs.version)?.takeIf { it != "unspecified" && it.startsWith("Nightly") }
                            ?.let { "Version: $it" },
                        it[Bugs.votes].takeIf { it > 0 }
                            ?.let { "Votes in Bugzilla: $it" },
                        it[Bugs.bug_severity].value.takeIf { it != "enhancement" && it != "regression" }
                            ?.let { "Severity: $it" },
                        it[Bugs.op_sys]
                            ?.let { "OS: $it" },
                        listOf(
                            "Duplicates" to bugLinks.duplicates[bugId]?.issueList(),
                            "Duplicated by" to bugLinks.duplicatedBy[bugId]?.issueList(),
                            "Depends on" to bugLinks.blockedBy[bugId]?.issueList(),
                            "Blocks" to bugLinks.blocks[bugId]?.issueList(),
                        ).filter { it.second != null && it.second.toString().isNotBlank() }
                            .joinToString("\n\n") {
                                "\n${it.first}:\n${it.second}"
                            }
                    ).filter { it.isNotBlank() }.joinToString("\n"),
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
                            CustomLongFunction("length", AttachData.thedata) lessEq 20000L and (
                                    Attachments.mimetype notLike "video/%"
                                    ) and (
                                    Attachments.mimetype notLike "audio/%"
                                    ) and (
                                    Attachments.mimetype notLike "image/%"
                                    ) and (
                                    Attachments.mimetype notLike "%7z%"
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
                                    )
                        }
                        .select { LongDescs.bug_id eq bugId.value }
//                        .adjustSlice {
//                            slice(fields - AttachData.thedata + attachLength)
//                        }
                        .orderBy(LongDescs.bug_when)
                        .mapIndexed { index, row ->
                            val commentType = row[LongDescs.type]
                            Comment(
                                index = index,
                                created_when = row[LongDescs.bug_when],
                                markdown =
                                buildString {
                                    val thetext = row[LongDescs.thetext]
                                    if (thetext.isNotBlank()) {
                                        append(fixupMarkdown(gitHubLinkGenerator, thetext))
                                    }
                                    if (commentType == CommentTypes.ATTACHMENT_CREATED) {
                                        if (isNotEmpty()) {
                                            append("\n\n")
                                        }
                                        append(addAttachment(bugId, row))
                                    }
                                    if (commentType == CommentTypes.DUPE_OF) {
                                        row[LongDescs.extra_data]?.toInt()?.let { extraData ->
                                            if (isNotEmpty()) {
                                                append("\n\n")
                                            }
                                            append("This bug has been marked as a duplicate of ")
                                            append(gitHubLinkGenerator.issueLink(BugId(extraData), Markup.MARKDOWN))
                                        }
                                    }
                                },
                                author = Profile(
                                    login = row[Profiles.login_name],
                                    realname = row[Profiles.realname].ifBlank {
                                        row[Profiles.login_name].substringBefore('@')
                                    },
                                    githubProfile = gitHubUserMapping.gitHubLoginOrNull(row[Profiles.login_name]),
                                )
                            )
                        }.let { list ->
                            list.filterIndexed { index, comment -> index == 0 || comment != list[index - 1] }
                        }
                )
            }
        }

    private fun addAttachment(bugId: BugId, row: ResultRow): String? {
        val attachId = row.getOrNull(Attachments.id)?.value?.let { AttachId(it) } ?: return null
        val fileName = row[Attachments.filename]
        val res = StringBuilder()

        val attachmentLink = attachmentLinkGenerator.linkFor(
            bugId,
            attachId,
            fileName
        )
        // TODO: escape description
        val description = row[Attachments.description]
        res.append(
            "Created attachment ${Link(fileName, attachmentLink).markdown}: $description"
        )
        val thedata = row.getOrNull(AttachData.thedata)
        val mimetype = row[Attachments.mimetype]
        val extension = fileName.substringAfterLast('.')
        val detectedLanguage =
            "diff".takeIf { row[Attachments.ispatch] } ?:
            when (extension) {
                "jtl" ->
                    if (thedata == null) {
                        ""
                    } else if (thedata.bytes.sumOf {
                            when(it) {
                                ','.code.toByte() -> 1.toInt()
                                '<'.code.toByte() -> -1
                                else -> 0
                            }
                        } > 0) {
                        "csv"
                    } else {
                        "xml"
                    }
                else ->
                    extToLanguage[extension] ?:

                    when (mimetype) {
                        "application/x-extension-jmx" -> "xml"
                        "application/x-shellscript" -> "sh"

                        "application/xml",
                        "application/html",
                        "application/json",
                        "application/php",
                        "application/perl",
                        "application/python" ->
                            mimetype.removePrefix("application/")

                        "text/plain",
                        "text/1",
                        "text/x-csrc",
                        "text/x-matlab",
                        "application/octet-stream" ->
                            "" // Looks like a generic text

                        else -> null
                    }
            }
        val isImage = extension in imageExtensions
        val isBinary = extension in binaryExtensions ||
            thedata?.bytes?.contains(0) == true;

        if (!isBinary && !isImage && thedata != null && thedata.bytes.size < 10000 && detectedLanguage != null) {
            res.append("\n")
            res.append("<details open><summary>")
            res.append(fileName.escapeHTML())
            res.append("</summary>")
            res.append("\n\n````")
            res.append(detectedLanguage)
            res.append("\n")
            res.append(thedata.bytes.toString(Charsets.UTF_8))
            res.append("````")
            res.append("\n\n</details>")
        } else if (isImage || mimetype.startsWith("image/")) {
            res.append("\n\n")
            res.append("<img src='$attachmentLink' alt='${description.escapeHTML()}'>")
        } else if (mimetype.startsWith("video/")) {
            res.append("\n\n")
            res.append("<video src='$attachmentLink' title='${description.escapeHTML()}'>")
        }
        return res.toString()
    }
}

