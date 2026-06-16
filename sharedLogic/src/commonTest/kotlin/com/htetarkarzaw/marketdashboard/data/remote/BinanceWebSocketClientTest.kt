package com.htetarkarzaw.marketdashboard.data.remote

import app.cash.turbine.test
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals

class BinanceWebSocketClientTest {

    private val fakeFrame = """
        [
            {
                "s": "BTCUSDT",
                "c": "67432.10",
                "o": "65800.00",
                "h": "68100.00",
                "l": "65800.00",
                "v": "12345.67",
                "q": "1234567890.12"
            },
            {
                "s": "ETHUSDT",
                "c": "3521.00",
                "o": "3565.00",
                "h": "3600.00",
                "l": "3450.00",
                "v": "9876.54",
                "q": "987654321.00"
            }
        ]
    """.trimIndent()

    @Test
    fun observePricesEmitsParsedTickerListFromFrame() = runTest {
        val pricesFlow = flow { emit(parseTickerFrame(fakeFrame)) }

        pricesFlow.test {
            val list = awaitItem()
            assertEquals(2, list.size)

            val btc = list[0]
            assertEquals("BTCUSDT", btc.symbol)
            assertEquals("67432.10", btc.lastPrice)
            assertEquals("65800.00", btc.openPrice)
            assertEquals("68100.00", btc.highPrice)
            assertEquals("65800.00", btc.lowPrice)
            assertEquals("12345.67", btc.volume)
            assertEquals("1234567890.12", btc.quoteVolume)

            val eth = list[1]
            assertEquals("ETHUSDT", eth.symbol)
            assertEquals("3521.00", eth.lastPrice)
            assertEquals("3565.00", eth.openPrice)

            awaitComplete()
        }
    }

    @Test
    fun parseTickerFrameIgnoresUnknownKeys() = runTest {
        val frameWithExtraKeys = """
            [{"s":"BTCUSDT","c":"67432.10","o":"65800.00","h":"68100.00","l":"65800.00","v":"12345.67","q":"1234567890.12","e":"24hrMiniTicker","E":1234567890}]
        """.trimIndent()

        val pricesFlow = flow { emit(parseTickerFrame(frameWithExtraKeys)) }

        pricesFlow.test {
            val list = awaitItem()
            assertEquals(1, list.size)
            assertEquals("BTCUSDT", list[0].symbol)
            awaitComplete()
        }
    }
}
