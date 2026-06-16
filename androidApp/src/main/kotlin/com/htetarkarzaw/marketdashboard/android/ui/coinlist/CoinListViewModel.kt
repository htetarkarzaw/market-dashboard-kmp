package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.AddToWatchlist
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.DismissError
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.LoadInitial
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.ReachedEnd
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.Refresh
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.StartPriceUpdates
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.StopPriceUpdates
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Error
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Loading
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Success
import com.htetarkarzaw.marketdashboard.android.ui.model.toUiModel
import com.htetarkarzaw.marketdashboard.domain.usecase.AddToWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RefreshCoinsUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CoinListViewModel(
    private val getCoinsUseCase: GetCoinsUseCase,
    private val refreshCoinsUseCase: RefreshCoinsUseCase,
    private val startPriceUpdatesUseCase: StartPriceUpdatesUseCase,
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<CoinListUiState> = MutableStateFlow(Loading)
    val uiState: StateFlow<CoinListUiState> = _uiState.asStateFlow()

    private val pageSize = 20
    private var priceUpdateJob: Job? = null

    fun onIntent(intent: CoinListIntent) {
        when (intent) {
            is LoadInitial -> loadInitial()
            is Refresh -> refresh()
            is ReachedEnd -> {
                val current = _uiState.value as? Success ?: return
                if (current.hasReachedEnd || current.isLoadingMore) return
                loadNextPage()
            }
            is DismissError -> dismissError()
            is StartPriceUpdates -> startPriceUpdates()
            is StopPriceUpdates -> stopPriceUpdates()
            is AddToWatchlist -> addToWatchlist(intent.coinId)
        }
    }

    fun onAddToWatchlist(coinId: String) = onIntent(AddToWatchlist(coinId))

    private fun startPriceUpdates() {
        priceUpdateJob = viewModelScope.launch {
            var delayMs = 1_000L
            while (isActive) {
                try {
                    startPriceUpdatesUseCase()
                    delayMs = 1_000L
                } catch (e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    Napier.e("WebSocket error (${e.message}), retrying in ${delayMs}ms", tag = "WebSocket")
                }
                if (isActive) {
                    delay(delayMs.milliseconds)
                    delayMs = (delayMs * 2).coerceAtMost(30_000L)
                }
            }
        }
    }

    private fun stopPriceUpdates() {
        priceUpdateJob?.cancel()
    }

    private fun loadInitial() {
        _uiState.value = Loading
        viewModelScope.launch {
            try {
                refreshCoinsUseCase()
                combine(
                    getCoinsUseCase(page = 0, pageSize = pageSize),
                    getWatchlistUseCase()
                ) { coins, watchlist ->
                    val watchlistIds = watchlist.map { it.symbol }.toSet()
                    coins.map { it.toUiModel(isWatchlisted = it.symbol in watchlistIds) }
                }
                    .catch { e -> _uiState.value = Error(e.message ?: "Unknown error") }
                    .collect { coins ->
                        _uiState.value = Success(coins = coins)
                    }
            } catch (e: Exception) {
                _uiState.value = Error(e.message ?: "Unknown error")
            }
        }
    }

    private fun refresh() {
        viewModelScope.launch {
            try {
                refreshCoinsUseCase()
                combine(
                    getCoinsUseCase(page = 0, pageSize = pageSize),
                    getWatchlistUseCase()
                ) { coins, watchlist ->
                    val watchlistIds = watchlist.map { it.symbol }.toSet()
                    coins.map { it.toUiModel(isWatchlisted = it.symbol in watchlistIds) }
                }
                    .catch { e ->
                        val currentCoins = (_uiState.value as? Success)?.coins ?: emptyList()
                        _uiState.value = Success(coins = currentCoins, errorMessage = e.message)
                    }
                    .collect { coins ->
                        _uiState.value = Success(coins = coins, hasReachedEnd = false)
                    }
            } catch (e: Exception) {
                val currentCoins = (_uiState.value as? Success)?.coins ?: emptyList()
                _uiState.value = Success(coins = currentCoins, errorMessage = e.message ?: "Unknown error")
            }
        }
    }

    private fun loadNextPage() {
        val currentState = _uiState.value as? Success ?: return
        val nextPage = currentState.coins.size / pageSize
        _uiState.value = currentState.copy(isLoadingMore = true)
        viewModelScope.launch {
            combine(
                getCoinsUseCase(page = nextPage, pageSize = pageSize),
                getWatchlistUseCase()
            ) { newCoins, watchlist ->
                val watchlistIds = watchlist.map { it.symbol }.toSet()
                newCoins.map { it.toUiModel(isWatchlisted = it.symbol in watchlistIds) }
            }
                .catch { e ->
                    val currentCoins = (_uiState.value as? Success)?.coins ?: emptyList()
                    _uiState.value = Success(coins = currentCoins, errorMessage = e.message)
                }
                .collect { newCoins ->
                    val current = _uiState.value as? Success ?: return@collect
                    if (newCoins.isEmpty()) {
                        _uiState.value = current.copy(isLoadingMore = false, hasReachedEnd = true)
                        return@collect
                    }
                    _uiState.value = Success(coins = current.coins + newCoins)
                }
        }
    }

    private fun addToWatchlist(coinId: String) {
        viewModelScope.launch { addToWatchlistUseCase(coinId) }
    }

    private fun dismissError() {
        val currentState = _uiState.value as? Success ?: return
        _uiState.value = currentState.copy(errorMessage = null)
    }
}
