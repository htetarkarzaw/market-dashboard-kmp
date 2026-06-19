package com.htetarkarzaw.marketdashboard.data.remote

import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto
import com.htetarkarzaw.marketdashboard.data.remote.dto.KlineDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class KtorBinanceApi(private val httpClient: HttpClient) : BinanceApi {
    override suspend fun fetchTickers(): List<CoinDto> =
        httpClient.get("$BASE_URL/ticker/24hr").body()

    override suspend fun getKlines(symbol: String, interval: String, limit: Int): List<KlineDto> =
        httpClient.get("$BASE_URL/klines") {
            parameter("symbol", symbol)
            parameter("interval", interval)
            parameter("limit", limit)
        }.body()
}
