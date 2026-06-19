package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import com.htetarkarzaw.marketdashboard.domain.model.PricePoint
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.test.assertEquals

class FakeCoinRepository : CoinRepository {
    var klinesResult: List<PricePoint> = emptyList()
    var capturedSymbol: String? = null
    var capturedInterval: String? = null
    var capturedLimit: Int? = null

    override fun getCoins(page: Int, pageSize: Int): Flow<List<Coin>> = emptyFlow()
    override fun getCoinWithWatchlist(symbol: String): Flow<Coin?> = emptyFlow()
    override fun getMarketSummary(): Flow<MarketSummary> = emptyFlow()
    override suspend fun refreshCoins() = Unit
    override suspend fun startPriceUpdates() = Unit

    override suspend fun getKlines(symbol: String, interval: String, limit: Int): List<PricePoint> {
        capturedSymbol = symbol
        capturedInterval = interval
        capturedLimit = limit
        return klinesResult
    }
}

class GetKlinesUseCaseTest {

    private val repository = FakeCoinRepository()
    private val useCase = GetKlinesUseCase(repository)

    @Test
    fun invokeForwardsArgumentsToRepository() = runTest {
        useCase("BTCUSDT", "1m", 60)

        assertEquals("BTCUSDT", repository.capturedSymbol)
        assertEquals("1m", repository.capturedInterval)
        assertEquals(60, repository.capturedLimit)
    }

    @Test
    fun invokeReturnsRepositoryResult() = runTest {
        val expected = listOf(
            PricePoint(time = 1_000_000L, price = 67_000.0),
            PricePoint(time = 1_060_000L, price = 67_500.0),
        )
        repository.klinesResult = expected

        val result = useCase("BTCUSDT", "1m", 60)

        assertEquals(expected, result)
    }

    @Test
    fun invokeUsesDefaultIntervalAndLimit() = runTest {
        useCase("ETHUSDT")

        assertEquals("ETHUSDT", repository.capturedSymbol)
        assertEquals("1h", repository.capturedInterval)
        assertEquals(24, repository.capturedLimit)
    }
}
