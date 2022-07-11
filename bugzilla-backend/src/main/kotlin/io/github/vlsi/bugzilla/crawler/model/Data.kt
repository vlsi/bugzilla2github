package io.github.vlsi.bugzilla.crawler.model

import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class Data(
    val encoding: String,
    @XmlValue(true)
    val content: String
)
