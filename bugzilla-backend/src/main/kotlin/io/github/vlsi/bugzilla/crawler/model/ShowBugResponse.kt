package io.github.vlsi.bugzilla.crawler.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlElement
import nl.adaptivity.xmlutil.serialization.XmlSerialName

@Serializable
@SerialName("bugzilla")
data class ShowBugResponse(
    val version: String,
    val urlbase: String,
    val maintainer: String,
    val exporter: String?,
    @XmlElement(true)
    @XmlSerialName(value = "bug", namespace = "", prefix = "")
    val bug: List<Bug>,
)
