package io.github.vlsi.bugzilla

import io.github.vlsi.bugzilla.dto.BugId
import kotlinx.datetime.LocalDateTime
import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

@Serializable
data class BugSummary(
    val bug_id: BugId,
    @Serializable(with = LocalDateTimeSerializer::class)
    val changeddate: LocalDateTime
)

public object LocalDateTimeSerializer: KSerializer<LocalDateTime> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): LocalDateTime =
        LocalDateTime.parse(decoder.decodeString().replaceRange(10..10, "T"))

    override fun serialize(encoder: Encoder, value: LocalDateTime) {
        encoder.encodeString(value.toString().replaceRange(10..10, " "))
    }
}
