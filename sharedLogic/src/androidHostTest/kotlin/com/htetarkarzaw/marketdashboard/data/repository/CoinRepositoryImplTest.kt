package com.htetarkarzaw.marketdashboard.data.repository

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.htetarkarzaw.marketdashboard.data.local.MarketDatabase
import com.htetarkarzaw.marketdashboard.data.remote.BinanceApi
import com.htetarkarzaw.marketdashboard.data.remote.BinanceWebSocketClient
import com.htetarkarzaw.marketdashboard.data.remote.dto.CoinDto
import com.htetarkarzaw.marketdashboard.data.remote.dto.KlineDto
import com.htetarkarzaw.marketdashboard.data.remote.dto.TickerDto
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals

class FakeBinanceApi : BinanceApi {
    var tickers: List<CoinDto> = emptyList()
    var klines: List<KlineDto> = emptyList()
    var capturedKlinesSymbol: String? = null
    var capturedKlinesInterval: String? = null
    var capturedKlinesLimit: Int? = null

    override suspend fun fetchTickers(): List<CoinDto> = tickers

    override suspend fun getKlines(symbol: String, interval: String, limit: Int): List<KlineDto> {
        capturedKlinesSymbol = symbol
        capturedKlinesInterval = interval
        capturedKlinesLimit = limit
        return klines
    }
}

class FakeBinanceWebSocketClient : BinanceWebSocketClient {
    override fun observePrices(): Flow<List<TickerDto>> = emptyFlow()
}

class CoinRepositoryImplTest {

    private lateinit var database: MarketDatabase
    private lateinit var fakeApi: FakeBinanceApi
    private lateinit var repository: CoinRepositoryImpl

    @Before
    fun setUp() {
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MarketDatabase.Schema.create(driver)
        database = MarketDatabase(driver)
        fakeApi = FakeBinanceApi()
        repository = CoinRepositoryImpl(fakeApi, database, FakeBinanceWebSocketClient())
    }

    @Test
    fun refreshCoinsSavesCoinsToDatabase() = runTest {
        fakeApi.tickers = listOf(
            coinDto("BTCUSDT", quoteVolume = "5000000.00"),
            coinDto("ETHUSDT", quoteVolume = "3000000.00")
        )

        repository.refreshCoins()

        val coins = repository.getCoins(page = 0, pageSize = 20).first()
        assertEquals(2, coins.size)
    }

    @Test
    fun refreshCoinsFiltersNonUsdtPairs() = runTest {
        fakeApi.tickers = listOf(
            coinDto("BTCUSDT", quoteVolume = "5000000.00"),
            coinDto("BTCETH", quoteVolume = "5000000.00")
        )

        repository.refreshCoins()

        val coins = repository.getCoins(page = 0, pageSize = 20).first()
        assertEquals(1, coins.size)
        assertEquals("BTCUSDT", coins[0].symbol)
    }

    @Test
    fun updatePriceUpdatesExistingCoin() = runTest {
        fakeApi.tickers = listOf(coinDto("BTCUSDT", lastPrice = "45000.00"))
        repository.refreshCoins()

        database.coinEntityQueries.updatePrice(
            lastPrice = 50000.0,
            priceChangePercent = 11.11,
            highPrice = 51000.0,
            lowPrice = 44000.0,
            volume = 6000000.0,
            symbol = "BTCUSDT"
        )

        val coins = repository.getCoins(page = 0, pageSize = 20).first()
        assertEquals(50000.0, coins[0].lastPrice)
    }

    @Test
    fun getKlinesForwardsArgumentsToApi() = runTest {
        repository.getKlines("BTCUSDT", "1m", 60)

        assertEquals("BTCUSDT", fakeApi.capturedKlinesSymbol)
        assertEquals("1m", fakeApi.capturedKlinesInterval)
        assertEquals(60, fakeApi.capturedKlinesLimit)
    }

    @Test
    fun getKlinesMapsKlineDtoToPricePoint() = runTest {
        fakeApi.klines = listOf(
            KlineDto(openTime = 1_700_000_000_000L, close = "67500.50"),
            KlineDto(openTime = 1_700_003_600_000L, close = "68100.25"),
        )

        val result = repository.getKlines("BTCUSDT", "1m", 60)

        assertEquals(2, result.size)
        assertEquals(1_700_000_000_000L, result[0].time)
        assertEquals(67500.50, result[0].price)
        assertEquals(1_700_003_600_000L, result[1].time)
        assertEquals(68100.25, result[1].price)
    }

    @Test
    fun getKlinesReturnsEmptyListWhenApiReturnsEmpty() = runTest {
        fakeApi.klines = emptyList()

        val result = repository.getKlines("BTCUSDT", "1m", 60)

        assertEquals(emptyList(), result)
    }

    private fun coinDto(
        symbol: String,
        lastPrice: String = "45000.00",
        quoteVolume: String = "5000000.00"
    ) = CoinDto(
        symbol = symbol,
        lastPrice = lastPrice,
        priceChangePercent = "2.00",
        highPrice = "46000.00",
        lowPrice = "44000.00",
        volume = "12345.00",
        quoteVolume = quoteVolume
    )
}
