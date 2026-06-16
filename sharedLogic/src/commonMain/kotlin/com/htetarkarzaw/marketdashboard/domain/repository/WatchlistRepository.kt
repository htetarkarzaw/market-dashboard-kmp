package com.htetarkarzaw.marketdashboard.domain.repository

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlistCoins(): Flow<List<Coin>>
    suspend fun addToWatchlist(coinId: String)
    suspend fun removeFromWatchlist(coinId: String)
}
