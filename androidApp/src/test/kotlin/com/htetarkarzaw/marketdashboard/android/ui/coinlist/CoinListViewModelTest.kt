package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import app.cash.turbine.test
import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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

    override suspend fun refreshCoins() {
        if (shouldThrow) throw Exception("Network error")
    }

    override suspend fun startPriceUpdates() {
        // no-op for tests
    }
}

@OptIn(ExperimentalCoroutinesApi::class)
class CoinListViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private lateinit var fakeRepository: FakeCoinRepository
    private lateinit var viewModel: CoinListViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        fakeRepository = FakeCoinRepository()
        viewModel = CoinListViewModel(
            getCoinsUseCase = GetCoinsUseCase(fakeRepository),
            refreshCoinsUseCase = RefreshCoinsUseCase(fakeRepository),
            startPriceUpdatesUseCase = StartPriceUpdatesUseCase(fakeRepository)
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
