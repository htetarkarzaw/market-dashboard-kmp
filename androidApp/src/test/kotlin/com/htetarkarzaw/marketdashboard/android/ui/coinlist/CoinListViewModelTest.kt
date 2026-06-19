package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import app.cash.turbine.test
import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import com.htetarkarzaw.marketdashboard.domain.model.PricePoint
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import com.htetarkarzaw.marketdashboard.domain.repository.WatchlistRepository
import com.htetarkarzaw.marketdashboard.domain.usecase.AddToWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetMarketSummaryUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs

class FakeCoinRepository : CoinRepository {
    var coinsToReturn: List<Coin> = emptyList()
    var shouldThrow: Boolean = false

    override fun getCoins(page: Int, pageSize: Int): Flow<List<Coin>> = flow {
        if (shouldThrow) throw Exception("Network error")
        emit(coinsToReturn)
    }
    override fun getCoinWithWatchlist(symbol: String): Flow<Coin?> = emptyFlow()
    override fun getMarketSummary(): Flow<MarketSummary> = flowOf(
        MarketSummary(
            totalVolume = 1_000_000_000.0,
            topGainerSymbol = "BTCUSDT",
            topGainerPercent = 5.0,
            topLoserSymbol = "ETHUSDT",
            topLoserPercent = -3.0,
        )
    )
    override suspend fun refreshCoins() {
        if (shouldThrow) throw Exception("Network error")
    }
    override suspend fun startPriceUpdates() = Unit
    override suspend fun getKlines(symbol: String, interval: String, limit: Int): List<PricePoint> = emptyList()
}

class FakeWatchlistRepository : WatchlistRepository {
    override fun getWatchlistCoins(): Flow<List<Coin>> = flowOf(emptyList())
    override suspend fun addToWatchlist(coinId: String) = Unit
    override suspend fun removeFromWatchlist(coinId: String) = Unit
}

@OptIn(ExperimentalCoroutinesApi::class)
class CoinListViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeRepository: FakeCoinRepository
    private lateinit var fakeWatchlistRepository: FakeWatchlistRepository
    private lateinit var viewModel: CoinListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCoinRepository()
        fakeWatchlistRepository = FakeWatchlistRepository()
        viewModel = CoinListViewModel(
            getCoinsUseCase = GetCoinsUseCase(fakeRepository),
            refreshCoinsUseCase = RefreshCoinsUseCase(fakeRepository),
            startPriceUpdatesUseCase = StartPriceUpdatesUseCase(fakeRepository),
            getWatchlistUseCase = GetWatchlistUseCase(fakeWatchlistRepository),
            addToWatchlistUseCase = AddToWatchlistUseCase(fakeWatchlistRepository),
            getMarketSummaryUseCase = GetMarketSummaryUseCase(fakeRepository),
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateIsLoading() {
        assertIs<CoinListUiState.Loading>(viewModel.uiState.value)
    }

    @Test
    fun loadInitialEmitsSuccessWithCoins() = runTest(testDispatcher) {
        fakeRepository.coinsToReturn = listOf(testCoin("BTCUSDT"), testCoin("ETHUSDT"))

        viewModel.uiState.test {
            assertIs<CoinListUiState.Loading>(awaitItem())

            viewModel.onIntent(CoinListIntent.LoadInitial)
            advanceUntilIdle()

            val state = awaitItem()
            assertIs<CoinListUiState.Success>(state)
            assertEquals(2, state.coins.size)
            assertEquals("BTCUSDT", state.coins[0].symbol)
            assertEquals("ETHUSDT", state.coins[1].symbol)

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun loadInitialEmitsErrorOnFailure() = runTest(testDispatcher) {
        fakeRepository.shouldThrow = true

        viewModel.uiState.test {
            assertIs<CoinListUiState.Loading>(awaitItem())

            viewModel.onIntent(CoinListIntent.LoadInitial)
            advanceUntilIdle()

            val state = awaitItem()
            assertIs<CoinListUiState.Error>(state)
            assertEquals("Network error", state.message)

            cancelAndIgnoreRemainingEvents()
        }
    }

    private fun testCoin(symbol: String) = Coin(
        symbol = symbol,
        baseAsset = symbol.removeSuffix("USDT"),
        lastPrice = 45231.12,
        priceChangePercent = 2.34,
        highPrice = 46000.0,
        lowPrice = 44000.0,
        volume = 500000000.0,
        iconUrl = "https://assets.coincap.io/assets/icons/${symbol.removeSuffix("USDT").lowercase()}@2x.png"
    )
}
