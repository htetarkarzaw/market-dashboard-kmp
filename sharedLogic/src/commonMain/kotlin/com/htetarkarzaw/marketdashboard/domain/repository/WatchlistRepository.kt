package com.htetarkarzaw.marketdashboard.domain.repository

import com.htetarkarzaw.marketdashboard.domain.model.WatchlistItem
import kotlinx.coroutines.flow.Flow

interface WatchlistRepository {
    fun getWatchlist(): Flow<List<WatchlistItem>>
    suspend fun addToWatchlist(coinId: String)
    suspend fun removeFromWatchlist(coinId: String)
}
