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
                    os = it[Bugs.op_sys].let { OperatingSystem(it.value) },
                    markdown = listOf(
                        "Migrated from" to gitHubLinkGenerator.bugzilla.linkBug(bugId).html,
                        // "Migrated from" to fixupMarkdown(bugzillaUrl, "Bug ${bugId.value}"),
                        "Resolution" to it[Bugs.resolution].takeIf { it.isNotBlank() },
                        "Version" to it[Bugs.version]?.takeIf { it != "unspecified" && it.startsWith("Nightly") },
                        "Target milestone" to it[Bugs.target_milestone].takeIf { it != "---" },
                        "Votes in Bugzilla" to it[Bugs.votes].takeIf { it > 0 }
                    ).filter { it.second != null && it.second.toString().isNotBlank() }
                        .joinToString(
                            separator = "\n",
                            prefix = "<table>\n",
                            postfix = "</table>\n\n"
                        ) { "<tr><th>${it.first}</th><td>${it.second}</td></tr>" } +
                            listOf(
                                "Duplicates" to bugLinks.duplicates[bugId]?.issueList(),
                                "Duplicated by" to bugLinks.duplicatedBy[bugId]?.issueList(),
                                "Depends on" to bugLinks.blockedBy[bugId]?.issueList(),
                                "Blocks" to bugLinks.blocks[bugId]?.issueList(),
                            ).filter { it.second != null && it.second.toString().isNotBlank() }
                                .joinToString("\n\n") {
                                    "${it.first}:\n${it.second}"
                                },
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
                            val commentType = it[LongDescs.type]
                            Comment(
                                created_when = it[LongDescs.bug_when],
                                markdown =
                                buildString {
                                    if (commentType == CommentTypes.ATTACHMENT_CREATED) {
                                        append(addAttachment(bugId, it))
                                    }
                                    val thetext = it[LongDescs.thetext]
                                    if (thetext.isNotBlank()) {
                                        if (isNotEmpty()) {
                                            append("\n\n")
                                        }
                                        append(fixupMarkdown(gitHubLinkGenerator, thetext))
                                    }
                                    if (commentType == CommentTypes.DUPE_OF) {
                                        it[LongDescs.extra_data]?.toInt()?.let { extraData ->
                                            if (isNotEmpty()) {
                                                append("\n\n")
                                            }
                                            append("This bug has been marked as a duplicate of ")
                                            append(gitHubLinkGenerator.issueLink(BugId(extraData), Markup.MARKDOWN))
                                        }
                                    }
                                },
                                author = Profile(
                                    login = it[Profiles.login_name],
                                    realname = it[Profiles.realname].ifBlank {
                                        it[Profiles.login_name].substringBefore('@')
                                    },
                                    githubProfile = gitHubUserMapping.gitHubLoginOrNull(it[Profiles.login_name]),
                                )
                            )
                        }.let { list ->
                            list.filterIndexed { index, comment -> index == 0 || comment != list[index - 1] }
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
        val description = row[Attachments.description]
        res.append(
            "Created attachment ${Link(fileName, attachmentLink).markdown}: $description"
        )
        val thedata = row[AttachData.thedata]
        val mimetype = row[Attachments.mimetype]
        val knownTextType =
            when {
                mimetype == "text/plain" ||
                mimetype == "text/1" ||
                mimetype == "text/x-csrc" ||
                mimetype == "text/x-matlab" ||
                mimetype == "application/octet-stream"->
                    when {
                        fileName.endsWith(".patch") -> "patch"
                        fileName.endsWith(".diff") -> "diff"
                        fileName.endsWith(".java") -> "java"
                        fileName.endsWith(".js") -> "js"
                        fileName.endsWith(".json") -> "json"
                        fileName.endsWith(".jmx") -> "xml"
                        fileName.endsWith(".xml") -> "xml"
                        fileName.endsWith(".php") -> "php"
                        fileName.endsWith(".perl") -> "perl"
                        fileName.endsWith(".sh") -> "sh"
                        fileName.endsWith(".bat") -> "batch"
                        fileName.endsWith(".properties") -> "properties"
                        else -> ""
                    }
                else -> null
            }
        val knownImage =
            fileName.endsWith(".png") ||
            fileName.endsWith(".jpg") ||
            fileName.endsWith(".jpeg") ||
            fileName.endsWith(".gif") ||
            fileName.endsWith(".bmp") ||
            fileName.endsWith(".tiff") ||
            fileName.endsWith(".svg");

        if (thedata != null && thedata.bytes.size < 10000 && !knownImage && (
                    knownTextType != null ||
                    mimetype.startsWith("text/") ||
                            mimetype == "application/xml" ||
                            mimetype == "application/json" ||
                            mimetype == "application/php" ||
                            mimetype == "application/perl" ||
                            mimetype == "application/x-shellscript" ||
                            mimetype == "application/x-extension-jmx" ||
                            mimetype.endsWith("php") ||
                            mimetype.endsWith("perl") ||
                            mimetype.endsWith("javascript") ||
                            mimetype.endsWith("jmx")

                    )
        ) {
            res.append("\n\n")
            res.append("Preview of ```$fileName```:\n")
            res.append("\n```")
            res.append(knownTextType ?:
                when {
                    mimetype == "application/x-shellscript" -> "sh"
                    mimetype.endsWith("jmx") -> "xml"
                    row[Attachments.ispatch] -> "diff"
                    mimetype == "text/doc" -> ""
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
        } else if (knownImage || mimetype.startsWith("image/")) {
            res.append("\n\n")
            res.append("<img src='$attachmentLink' alt='${description.escapeHTML()}'>")
        } else if (mimetype.startsWith("video/")) {
            res.append("\n\n")
            res.append("<video src='$attachmentLink' title='${description.escapeHTML()}'>")
        }
        return res.toString()
    }
}

