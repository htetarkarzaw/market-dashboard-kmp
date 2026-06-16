package com.htetarkarzaw.marketdashboard.data.remote

import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto

interface BinanceApi {
    suspend fun fetchTickers(): List<CoinDto>
}
