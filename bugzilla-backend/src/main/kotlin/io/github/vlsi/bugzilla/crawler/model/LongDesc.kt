package io.github.vlsi.bugzilla.crawler.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class LongDesc(
    val isprivate: Int, // TODO: Boolean?
    @XmlSerialName("commentid", "", "")
    @XmlElement(true) val commentid: CommentId,
    @XmlElement(true) val comment_count: Int,
    @XmlSerialName("attachid", "", "")
    @XmlElement(true) val attachid: AttachId?,
    @XmlSerialName("who", "", "")
    val who: Person,
    @Serializable(with = BzInstantSerializer::class)
    @XmlElement(true) val bug_when: Instant,
    @XmlElement(true) val thetext: String,
)
