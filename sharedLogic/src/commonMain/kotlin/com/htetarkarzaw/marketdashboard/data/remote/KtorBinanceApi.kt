package com.htetarkarzaw.marketdashboard.data.remote

import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get

class KtorBinanceApi(private val httpClient: HttpClient) : BinanceApi {
    override suspend fun fetchTickers(): List<CoinDto> =
        httpClient.get("$BASE_URL/ticker/24hr").body()
}
