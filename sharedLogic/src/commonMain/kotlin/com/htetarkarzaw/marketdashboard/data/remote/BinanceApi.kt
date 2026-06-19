package com.htetarkarzaw.marketdashboard.data.remote

import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto
import com.htetarkarzaw.marketdashboard.data.remote.dto.KlineDto

interface BinanceApi {
    suspend fun fetchTickers(): List<CoinDto>
    suspend fun getKlines(symbol: String, interval: String, limit: Int): List<KlineDto>
}
