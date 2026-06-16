package com.htetarkarzaw.marketdashboard.domain.repository

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getCoins(page: Int, pageSize: Int): Flow<List<Coin>>
    fun getMarketSummary(): Flow<MarketSummary>
    suspend fun refreshCoins()
    suspend fun startPriceUpdates()
}
