package com.htetarkarzaw.marketdashboard.data.remote

import com.htetarkarzaw.marketdashboard.data.remote.dto.TickerDto
import io.github.aakira.napier.Napier
import io.ktor.client.HttpClient
import io.ktor.client.plugins.websocket.*
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.serialization.json.Json

private const val WS_URL = "wss://stream.binance.com:9443/ws/!miniTicker@arr"
private const val TAG = "WebSocket"

internal val wsJson = Json { ignoreUnknownKeys = true }

internal fun parseTickerFrame(text: String): List<TickerDto> =
    wsJson.decodeFromString(text)

class BinanceWebSocketClient(private val httpClient: HttpClient) {

    fun observePrices(): Flow<List<TickerDto>> = callbackFlow {
        try {
            Napier.d("WebSocket connected", tag = TAG)
            httpClient.wss(urlString = WS_URL) {
                for (frame in incoming) {
                    if (frame is Frame.Text) {
                        val tickers = parseTickerFrame(frame.readText())
                        Napier.d("Received ${tickers.size} tickers", tag = TAG)
                        trySend(tickers)
                    }
                }
            }
        } catch (e: Exception) {
            Napier.e("WebSocket error: ${e.message}", tag = TAG)
            close()
        }
        awaitClose()
    }
}
