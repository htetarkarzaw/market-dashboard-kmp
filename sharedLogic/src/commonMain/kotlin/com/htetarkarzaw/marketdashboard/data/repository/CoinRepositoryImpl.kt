package com.htetarkarzaw.marketdashboard.data.repository

import com.htetarkarzaw.marketdashboard.data.local.CoinEntity
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase
import com.htetarkarzaw.marketdashboard.data.remote.BinanceApi
import com.htetarkarzaw.marketdashboard.data.remote.BinanceWebSocketClient
import com.htetarkarzaw.marketdashboard.data.remote.mapper.toDomain
import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import app.cash.sqldelight.coroutines.mapToOne
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CoinRepositoryImpl(
    private val api: BinanceApi,
    private val database: MarketDatabase,
    private val wsClient: BinanceWebSocketClient
) : CoinRepository {

    override suspend fun startPriceUpdates() {
        wsClient.observePrices().collect { tickers ->
            tickers
                .filter { it.symbol.endsWith("USDT") }
                .forEach { ticker ->
                    val open = ticker.openPrice.toDoubleOrNull() ?: 0.0
                    val close = ticker.lastPrice.toDoubleOrNull() ?: 0.0
                    val priceChangePercent = if (open != 0.0) ((close - open) / open) * 100 else 0.0
                    database.coinEntityQueries.updatePrice(
                        lastPrice = close,
                        priceChangePercent = priceChangePercent,
                        highPrice = ticker.highPrice.toDoubleOrNull() ?: 0.0,
                        lowPrice = ticker.lowPrice.toDoubleOrNull() ?: 0.0,
                        volume = ticker.quoteVolume.toDoubleOrNull() ?: 0.0,
                        symbol = ticker.symbol
                    )
                }
        }
    }

    override suspend fun refreshCoins() {
        val coins = api.fetchTickers()
            .filter { it.symbol.endsWith("USDT") }
            .filter { (it.quoteVolume.toDoubleOrNull() ?: 0.0) > 1_000_000.0 }
            .take(100)
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

    override fun getMarketSummary(): Flow<MarketSummary> {
        return database.coinEntityQueries.getMarketSummary()
            .asFlow()
            .mapToOne(Dispatchers.Default)
            .map { result ->
                MarketSummary(
                    totalVolume = result.totalVolume ?: 0.0,
                    topGainerSymbol = result.topGainerSymbol,
                    topGainerPercent = result.topGainerPercent ?: 0.0,
                    topLoserSymbol = result.topLoserSymbol,
                    topLoserPercent = result.topLoserPercent ?: 0.0
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
