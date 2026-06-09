package com.htetarkarzaw.marketdashboard.data.repository

import com.htetarkarzaw.marketdashboard.data.local.CoinEntity
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase
import com.htetarkarzaw.marketdashboard.data.remote.BinanceApi
import com.htetarkarzaw.marketdashboard.data.remote.mapper.toDomain
import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CoinRepositoryImpl(
    private val api: BinanceApi,
    private val database: MarketDatabase
) : CoinRepository {

    override suspend fun refreshCoins() {
        val coins = api.fetchTickers()
            .filter { it.symbol.endsWith("USDT") }
            .sortedByDescending { it.volume.toDoubleOrNull() ?: 0.0 }
            .map { it.toDomain() }

        val queries = database.coinEntityQueries
        queries.deleteAll()
        coins.forEach { coin ->
            queries.upsertAll(
                symbol = coin.symbol,
                baseAsset = coin.baseAsset,
                lastPrice = coin.lastPrice,
                priceChangePercent = coin.priceChangePercent,
                highPrice = coin.highPrice,
                lowPrice = coin.lowPrice,
                volume = coin.volume,
                iconUrl = coin.iconUrl
            )
        }
    }

    override fun getCoins(page: Int, pageSize: Int): Flow<List<Coin>> {
        val offset = page * pageSize
        return database.coinEntityQueries
            .selectAll(limit = pageSize.toLong(), offset = offset.toLong())
            .asFlow()
            .mapToList(Dispatchers.Default)
            .map { entities -> entities.map { it.toDomain() } }
    }

    private fun CoinEntity.toDomain(): Coin = Coin(
        symbol = symbol,
        baseAsset = baseAsset,
        lastPrice = lastPrice,
        priceChangePercent = priceChangePercent,
        highPrice = highPrice,
        lowPrice = lowPrice,
        volume = volume,
        iconUrl = iconUrl
    )
}
