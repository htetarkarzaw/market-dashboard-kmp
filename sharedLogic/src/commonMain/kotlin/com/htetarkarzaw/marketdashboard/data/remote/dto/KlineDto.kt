package com.htetarkarzaw.marketdashboard.data.remote.dto

import kotlinx.serialization.KSerializer
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.descriptors.buildClassSerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonDecoder
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.long

@Serializable(with = KlineDtoSerializer::class)
data class KlineDto(
    val openTime: Long,
    val close: String,
)

object KlineDtoSerializer : KSerializer<KlineDto> {
    override val descriptor: SerialDescriptor = buildClassSerialDescriptor("KlineDto")

    override fun deserialize(decoder: Decoder): KlineDto {
        val jsonDecoder = decoder as? JsonDecoder
            ?: throw SerializationException("KlineDto can only be deserialized from JSON")
        val array = jsonDecoder.decodeJsonElement() as? JsonArray
            ?: throw SerializationException("Expected JsonArray for KlineDto")
        if (array.size < 5) throw SerializationException("Kline array too short: ${array.size}")
        return KlineDto(
            openTime = array[0].jsonPrimitive.long,
            close = array[4].jsonPrimitive.content,
        )
    }

    override fun serialize(encoder: Encoder, value: KlineDto) {
        throw SerializationException("KlineDto serialization is not supported")
    }
}
