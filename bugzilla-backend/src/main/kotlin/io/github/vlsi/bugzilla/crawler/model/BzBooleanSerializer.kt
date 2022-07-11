package io.github.vlsi.bugzilla.crawler.model

import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

class BzBooleanSerializer : KSerializer<Boolean> {
    override val descriptor: SerialDescriptor =
        PrimitiveSerialDescriptor("BzBooleanSerializer", PrimitiveKind.BOOLEAN)

    override fun deserialize(decoder: Decoder): Boolean =
        decoder.decodeChar() == '1'

    override fun serialize(encoder: Encoder, value: Boolean) {
        encoder.encodeChar(if (value) '1' else '0')
    }
}
