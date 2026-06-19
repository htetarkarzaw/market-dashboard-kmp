package com.htetarkarzaw.marketdashboard.data.remote.dto

import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

class KlineDtoSerializerTest {

    private val fullArray = "[1234567890000, \"65000.00\", \"66000.00\", \"64000.00\", \"64500.00\", \"100.0\", 1234567890999]"

    @Test
    fun parsesOpenTimeFromIndex0() {
        val result = Json.decodeFromString<KlineDto>(fullArray)

        assertEquals(1234567890000L, result.openTime)
    }

    @Test
    fun parsesClosePriceFromIndex4() {
        val result = Json.decodeFromString<KlineDto>(fullArray)

        assertEquals("64500.00", result.close)
    }

    @Test
    fun throwsOnMalformedInput() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString<KlineDto>("{\"openTime\": 123, \"close\": \"64500.00\"}")
        }
    }

    @Test
    fun throwsOnTooShortArray() {
        assertFailsWith<SerializationException> {
            Json.decodeFromString<KlineDto>("[1234567890000, \"65000.00\", \"66000.00\", \"64000.00\"]")
        }
    }
}
