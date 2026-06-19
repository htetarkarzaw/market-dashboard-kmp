package com.htetarkarzaw.marketdashboard.domain.repository

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import com.htetarkarzaw.marketdashboard.domain.model.PricePoint
import kotlinx.coroutines.flow.Flow

interface CoinRepository {
    fun getCoins(page: Int, pageSize: Int): Flow<List<Coin>>
    fun getCoinWithWatchlist(symbol: String): Flow<Coin?>
    fun getMarketSummary(): Flow<MarketSummary>
    suspend fun refreshCoins()
    suspend fun startPriceUpdates()
    suspend fun getKlines(symbol: String, interval: String, limit: Int): List<PricePoint>
}
