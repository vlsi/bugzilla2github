package io.github.vlsi.bugzilla.crawler.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
data class Attachment(
    @Serializable(with = BzBooleanSerializer::class)
    val isobsolete: Boolean,
    @Serializable(with = BzBooleanSerializer::class)
    val ispatch: Boolean,
    @Serializable(with = BzBooleanSerializer::class)
    val isprivate: Boolean,
    @XmlSerialName("attachid", "", "")
    @XmlElement(true) val attachid: AttachId,
    @Serializable(with = BzInstantSerializer::class)
    @XmlElement(true)
    val date: Instant,
    @Serializable(with = BzInstantSerializer::class)
    @XmlElement(true)
    val delta_ts: Instant,
    @XmlElement(true)
    val desc: String,
    @XmlElement(true)
    val filename: String,
    @XmlElement(true)
    val type: String,
    @XmlElement(true)
    val size: Long,
    @XmlSerialName("attacher", "", "")
    val attacher: Person,
    @XmlElement(true)
    val token: String?,
    @XmlSerialName("data", "", "")
    val data: Data,
)
