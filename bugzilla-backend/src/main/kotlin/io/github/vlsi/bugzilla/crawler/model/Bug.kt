package io.github.vlsi.bugzilla.crawler.model

import io.github.vlsi.bugzilla.dto.BugId
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class Bug(
    @XmlSerialName("bug_id", "", "")
    @XmlElement(true) val bug_id: BugId,
    @XmlSerialName("alias", "", "")
    @XmlElement(true) val alias: List<String>,
    @Serializable(with = BzInstantSerializer::class)
    @XmlElement(true) val creation_ts: Instant,
    @XmlElement(true) val short_desc: String,
    @Serializable(with = BzInstantSerializer::class)
    @XmlElement(true) val delta_ts: Instant,
    @Serializable(with = BzBooleanSerializer::class)
    @XmlElement(true) val reporter_accessible: Boolean,
    @Serializable(with = BzBooleanSerializer::class)
    @XmlElement(true) val cclist_accessible: Boolean,
    @XmlElement(true) val classification_id: Int,
    @XmlElement(true) val classification: String,
    @XmlElement(true) val product: String,
    @XmlElement(true) val component: String,
    @XmlElement(true) val version: String,
    @XmlElement(true) val rep_platform: String,
    @XmlElement(true) val op_sys: String,
    @XmlElement(true) val bug_status: String,
    @XmlElement(true) val resolution: String?,
    @XmlElement(true) val bug_file_loc: String?,
    @XmlElement(true) val status_whiteboard: String?,
    @XmlElement(true) val keywords: String?,
    @XmlElement(true) val priority: String,
    @XmlElement(true) val bug_severity: String,
    @XmlElement(true) val target_milestone: String?,
    @XmlSerialName("dependson", "", "")
    @XmlElement(true) val dependson: List<BugId>,
    @XmlSerialName("blocked", "", "")
    @XmlElement(true) val blocked: List<BugId>,
    @Serializable(with = BzBooleanSerializer::class)
    @XmlElement(true) val everconfirmed: Boolean,
    @XmlSerialName("reporter", "", "")
    val reporter: Person,
    @XmlSerialName("assigned_to", "", "")
    val assigned_to: Person,
    @XmlSerialName("cc", "", "")
    @XmlElement(true) val cc: List<Username>,
    @XmlElement(true) val votes: Int,
    @XmlElement(true) val comment_sort_order: String, // TODO: enum? oldest_to_newest
    @XmlSerialName("long_desc", "", "")
    val long_desc: List<LongDesc>,
    @XmlSerialName("attachment", "", "")
    val attachment: List<Attachment>
)
