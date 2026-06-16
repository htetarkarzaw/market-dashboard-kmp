package com.htetarkarzaw.marketdashboard.data.remote.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class TickerDtoTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun parsesMinitTickerJsonCorrectly() {
        val raw = """
            {
                "s": "BTCUSDT",
                "c": "67432.10",
                "o": "65800.00",
                "h": "68100.00",
                "l": "65800.00",
                "v": "12345.67",
                "q": "1234567890.12"
            }
        """.trimIndent()

        val ticker = json.decodeFromString<TickerDto>(raw)

        assertEquals("BTCUSDT", ticker.symbol)
        assertEquals("67432.10", ticker.lastPrice)
        assertEquals("65800.00", ticker.openPrice)
        assertEquals("68100.00", ticker.highPrice)
        assertEquals("65800.00", ticker.lowPrice)
        assertEquals("12345.67", ticker.volume)
        assertEquals("1234567890.12", ticker.quoteVolume)
    }
}
