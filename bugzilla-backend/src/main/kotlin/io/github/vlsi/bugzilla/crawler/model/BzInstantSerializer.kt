package io.github.vlsi.bugzilla.crawler.model

import kotlinx.datetime.Instant
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

public object BzInstantSerializer : KSerializer<Instant> {

    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("LocalDateTime", PrimitiveKind.STRING)

    override fun deserialize(decoder: Decoder): Instant =
        Instant.parse(
            decoder.decodeString()
                .replaceRange(10..10, "T")
                .replaceRange(19..24, "+00:00")
        )

    override fun serialize(encoder: Encoder, value: Instant) {
        encoder.encodeString(
            value.toString()
                .replaceRange(10..10, " ")
                .replaceRange(19..24, " +0000")
        )
    }
}
