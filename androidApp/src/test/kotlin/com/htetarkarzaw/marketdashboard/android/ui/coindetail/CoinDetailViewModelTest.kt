package com.htetarkarzaw.marketdashboard.android.ui.coindetail

import app.cash.turbine.test
import com.htetarkarzaw.marketdashboard.domain.model.Coin
import com.htetarkarzaw.marketdashboard.domain.model.PricePoint
import com.htetarkarzaw.marketdashboard.domain.usecase.AddToWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinDetailUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetKlinesUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RemoveFromWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.awaitCancellation
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
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class CoinDetailViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = StandardTestDispatcher(testScheduler)

    private val getCoinDetailUseCase: GetCoinDetailUseCase = mockk()
    private val getKlinesUseCase: GetKlinesUseCase = mockk()
    private val startPriceUpdatesUseCase: StartPriceUpdatesUseCase = mockk()
    private val addToWatchlistUseCase: AddToWatchlistUseCase = mockk()
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase = mockk()

    private lateinit var viewModel: CoinDetailViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        every { getCoinDetailUseCase(any()) } returns flowOf(testCoin())
        coEvery { getKlinesUseCase(any(), any(), any()) } returns testPricePoints()
        coEvery { startPriceUpdatesUseCase() } coAnswers { awaitCancellation() }
        coEvery { addToWatchlistUseCase(any()) } just Runs
        coEvery { removeFromWatchlistUseCase(any()) } just Runs
        viewModel = CoinDetailViewModel(
            getCoinDetailUseCase = getCoinDetailUseCase,
            getKlinesUseCase = getKlinesUseCase,
            startPriceUpdatesUseCase = startPriceUpdatesUseCase,
            addToWatchlistUseCase = addToWatchlistUseCase,
            removeFromWatchlistUseCase = removeFromWatchlistUseCase,
        )
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadCoinSetsLoadingThenPopulatesState() = runTest(testDispatcher) {
        viewModel.uiState.test {
            awaitItem() // initial state

            viewModel.onIntent(CoinDetailIntent.LoadCoin("BTCUSDT"))
            advanceUntilIdle()

            val finalState = expectMostRecentItem()
            assertFalse(finalState.isLoading)
            assertNotNull(finalState.coin)
            assertEquals("BTCUSDT", finalState.coin!!.symbol)
            assertEquals(2, finalState.pricePoints.size)

            cancel()
        }
    }

    @Test
    fun updatePriceOnlyMutatesLastPoint() = runTest(testDispatcher) {
        viewModel.onIntent(CoinDetailIntent.LoadCoin("BTCUSDT"))
        advanceUntilIdle()

        val pricePointsBefore = viewModel.uiState.value.pricePoints
        assertTrue(pricePointsBefore.isNotEmpty())

        viewModel.uiState.test {
            viewModel.onIntent(CoinDetailIntent.PriceUpdated("BTCUSDT", 70000.0))
            advanceUntilIdle()
            val updated = expectMostRecentItem()
            // pricePoints are stable — livePrice is the only indicator that changes on each tick
            assertEquals(pricePointsBefore, updated.pricePoints)
            assertEquals("\$70,000.00", updated.livePrice)
            cancel()
        }
    }

    @Test
    fun toggleWatchlistAddsWhenNotWatchlisted() = runTest(testDispatcher) {
        viewModel.onIntent(CoinDetailIntent.LoadCoin("BTCUSDT"))
        advanceUntilIdle()

        assertNotNull(viewModel.uiState.value.coin)
        assertFalse(viewModel.uiState.value.coin!!.isWatchlisted)

        viewModel.onIntent(CoinDetailIntent.ToggleWatchlist)
        advanceUntilIdle()

        coVerify { addToWatchlistUseCase("BTCUSDT") }
    }

    @Test
    fun toggleWatchlistShowsDialogWhenWatchlisted() = runTest(testDispatcher) {
        every { getCoinDetailUseCase(any()) } returns flowOf(testCoin(isWatchlisted = true))

        viewModel.onIntent(CoinDetailIntent.LoadCoin("BTCUSDT"))
        advanceUntilIdle()

        viewModel.onIntent(CoinDetailIntent.ToggleWatchlist)

        assertTrue(viewModel.uiState.value.showRemoveDialog)
        coVerify(exactly = 0) { addToWatchlistUseCase(any()) }
    }

    @Test
    fun changeIntervalFetchesNewKlines() = runTest(testDispatcher) {
        viewModel.onIntent(CoinDetailIntent.LoadCoin("BTCUSDT"))
        advanceUntilIdle()

        coVerify { getKlinesUseCase("BTCUSDT", "1m", 60) }

        viewModel.onIntent(CoinDetailIntent.ChangeInterval("24h"))
        advanceUntilIdle()
        coVerify { getKlinesUseCase("BTCUSDT", "1h", 24) }

        viewModel.onIntent(CoinDetailIntent.ChangeInterval("7d"))
        advanceUntilIdle()
        coVerify { getKlinesUseCase("BTCUSDT", "4h", 42) }
    }

    private fun testCoin(isWatchlisted: Boolean = false) = Coin(
        symbol = "BTCUSDT",
        baseAsset = "BTC",
        lastPrice = 67000.0,
        priceChangePercent = 2.5,
        highPrice = 68000.0,
        lowPrice = 66000.0,
        volume = 500_000_000.0,
        iconUrl = "https://assets.coincap.io/assets/icons/btc@2x.png",
        isWatchlisted = isWatchlisted,
    )

    private fun testPricePoints() = listOf(
        PricePoint(time = 1_700_000_000_000L, price = 67000.0),
        PricePoint(time = 1_700_003_600_000L, price = 67500.0),
    )
}
