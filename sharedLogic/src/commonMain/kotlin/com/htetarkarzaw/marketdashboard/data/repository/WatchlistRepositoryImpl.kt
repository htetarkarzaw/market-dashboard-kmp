package com.htetarkarzaw.marketdashboard.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase
import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.repository.WatchlistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class WatchlistRepositoryImpl(
    private val database: MarketDatabase
) : WatchlistRepository {

    override fun getWatchlistCoins(): Flow<List<Coin>> =
        database.watchlistEntityQueries.selectWatchlistCoins()
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities ->
                entities.map {
                    Coin(
                        symbol = it.symbol,
                        baseAsset = it.baseAsset,
                        lastPrice = it.lastPrice,
                        priceChangePercent = it.priceChangePercent,
                        highPrice = it.highPrice,
                        lowPrice = it.lowPrice,
                        volume = it.volume,
                        iconUrl = it.iconUrl
                    )
                }
            }

    override suspend fun addToWatchlist(coinId: String) {
        database.watchlistEntityQueries.insert(coinId)
    }

    override suspend fun removeFromWatchlist(coinId: String) {
        database.watchlistEntityQueries.delete(coinId)
    }
}
