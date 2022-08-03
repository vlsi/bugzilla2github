package io.github.vlsi.bugzilla.dbexport

import org.jetbrains.exposed.sql.Table
import java.math.BigDecimal

object BugSeverities: StringIdTable("bug_severity", "value") {
    val priority_id = integer("id").uniqueIndex()
    val sortkey = integer("sortkey")
    val isactive = bool01("isactive")
}

object Priorities: StringIdTable("priority", "value") {
    val priority_id = integer("id").uniqueIndex()
    val sortkey = integer("sortkey")
    val isactive = bool01("isactive")
}

object BugStatuses: StringIdTable("bug_status", "value") {
    val priority_id = integer("id").uniqueIndex()
    val sortkey = integer("sortkey")
    val isactive = bool01("isactive")
    val is_open = bool01("is_open")
}

object OperatingSystems: StringIdTable("op_sys", "value") {
    val os_id = integer("id").uniqueIndex()
    val sortkey = integer("sortkey")
    val isactive = bool01("isactive")
}

object Tags: MediumIntIdTable("tags") {
    val name = varchar("name", 64)
    val user_id = reference("user_id", Profiles)
}

object BugTags: Table("bug_tag") {
    val bug_id = reference("bug_id", Bugs)
    val tag_id = reference("tag_id", Tags)
}

object KeywordDefs: MediumIntIdTable("keyworddefs") {
    val name = varchar("name", 64)
    val description = text("description")
}

object Keywords: Table("keywords") {
    val bug_id = reference("bug_id", Bugs)
    val keywordid = reference("keywordid", KeywordDefs)
}

object Bugs : MediumIntIdTable("bugs", "bug_id") {
    val assigned_to = integer("assigned_to")
    val bug_file_loc = text("bug_file_loc")
    val bug_severity = reference("bug_severity", BugSeverities)
    val bug_status = reference("bug_status", BugStatuses)
    val creation_ts = instantAsUtcDateTime("creation_ts").nullable()
    val delta_ts = instantAsUtcDateTime("delta_ts")
    val short_desc = varchar("short_desc", 255)
    val op_sys = reference("op_sys", OperatingSystems)
    val priority = reference("priority", Priorities)
    val rep_platform = varchar("rep_platform", 20).nullable()
    val reporter = integer("reporter").nullable()
    val version = varchar("version", 64)
    val resolution = varchar("resolution", 64).default("")
    val target_milestone = varchar("target_milestone", 64).default("---")
    val qa_contact = integer("qa_contact").nullable()
    val status_whiteboard = text("status_whiteboard").nullable()
    val votes = integer("votes").default(0)
    val lastdiffed = instantAsUtcDateTime("lastdiffed").nullable()
    val everconfirmed = bool01("everconfirmed")
    val reporter_accessible = bool01("reporter_accessible").default(true)
    val cclist_accessible = bool01("cclist_accessible").default(true)
    val estimated_time = decimal("estimated_time", 7, 2).default(BigDecimal.ZERO)
    val remaining_time = decimal("remaining_time", 7, 2).default(BigDecimal.ZERO)
    val product_id = reference("product_id", Products)
    val component_id = integer("component_id")
    val deadline = instantAsUtcDateTime("deadline").nullable()
}

object FieldDefs: MediumIntIdTable("fielddefs", "id") {
    val name = varchar("name", 64)
    val description = varchar("description", 255)
//    val type = varchar("type", 64)
//    val custom = bool01("custom")
    val mailhead = bool01("mailhead")
    val obsolete = bool01("obsolete")
    val enter_bug = bool01("enter_bug")
//    val bugnumber = bool01("bugnumber")
//    val visibility = enumerationByName<FieldVisibility>("visibility", 1)
//    val value_field_id = integer("value_field_id").nullable()
//    val field_id = reference("field_id", Fields)
    val is_mandatory = bool01("is_mandatory")
    val is_numeric = bool01("is_numeric")
    val long_desc = varchar("long_desc", 255)
}

object BugsActivity: MediumIntIdTable("bugs_activity", "id") {
    val bug_id = reference("bug_id", Bugs)
    val who = reference("who", Profiles)
    val bug_when = instantAsUtcDateTime("bug_when")
    val fieldid = reference("fieldid", FieldDefs)
    val added = varchar("added", 255)
    val removed = varchar("removed", 255)
    val attach_id = reference("attach_id", Attachments).nullable()
    val comment_id = reference("comment_id", LongDescs).nullable()
}

object Profiles : MediumIntIdTable("profiles", "userid") {
    val login_name = varchar("login_name", 255).uniqueIndex()
    val realname = varchar("realname", 255).default("")
    val disabledtext = text("disabledtext")
    val mybugslink = bool01("mybugslink").default(true)
    val extern_id = varchar("extern_id", 64).nullable().uniqueIndex()
    val disable_mail = bool01("disable_mail").default(false)
    val is_enabled = bool01("is_enabled").default(true)
    val last_seen_date = instantAsUtcDateTime("last_seen_date").nullable()
}

object Products : MediumIntIdTable("products", "id") {
    val name = varchar("name", 64).uniqueIndex()
    val description = text("description")
    val votesperuser = integer("votesperuser").default(0)
    val maxvotesperbug = integer("maxvotesperbug").default(1)
    val votestoconfirm = integer("votestoconfirm").default(0)
    val defaultmilestone = varchar("defaultmilestone", 64).default("---")
    val isactive = bool01("isactive").default(true)
    val allows_unconfirmed = bool01("allows_unconfirmed").default(true)
}

object Attachments : MediumIntIdTable("attachments", "attach_id") {
    val bug_id = reference("bug_id", Bugs)
    val creation_ts = instantAsUtcDateTime("creation_ts")
    val description = varchar("description", 255)
    val mimetype = varchar("mimetype", 255)
    val ispatch = bool01("ispatch")
    val filename = varchar("filename", 255)
    val submitter_id = reference("submitter_id", Profiles)
    val isobsolete = bool01("isobsolete")
    val isprivate = bool01("isprivate")
    val modification_time = instantAsUtcDateTime("modification_time")
}

object AttachData : MediumIntIdTable("attach_data", "id") {
    val thedata = blob("thedata")

    init {
        id.references(Attachments.id)
    }
}

object LongDescs : MediumIntIdTable("longdescs", "comment_id") {
    val bug_id = reference("bug_id", Bugs)
    val who = reference("who", Profiles)
    val bug_when = instantAsUtcDateTime("bug_when")
    val thetext = text("thetext")
    val work_time = decimal("work_time", 7, 2).default(BigDecimal.ZERO)
    val isprivate = bool01("isprivate").default(false)
    val already_wrapped = bool01("already_wrapped").default(false)
    val type = dbEnumeration("type", CommentTypes)
    val extra_data = varchar("extra_data", 255).nullable()
}

object Duplicates: Table("duplicates") {
    val dupe_of = reference("dupe_of", Bugs)
    val dupe = reference("dupe", Bugs)
}

object Dependencies: Table("dependencies") {
    val blocked = reference("blocked", Bugs)
    val dependson = reference("dependson", Bugs)
}
