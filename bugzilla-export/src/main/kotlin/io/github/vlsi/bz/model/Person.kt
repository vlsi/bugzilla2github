package io.github.vlsi.bz.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import nl.adaptivity.xmlutil.serialization.XmlValue

@Serializable
data class Person(
    @SerialName("name")
    val name: String?,
    @XmlValue(true)
    val username: Username,
)
