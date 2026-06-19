package com.htetarkarzaw.marketdashboard.android.ui.coindetail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.htetarkarzaw.marketdashboard.android.ui.model.toUiModel
import com.htetarkarzaw.marketdashboard.android.util.formatPrice
import com.htetarkarzaw.marketdashboard.domain.usecase.AddToWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetCoinDetailUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.GetKlinesUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RemoveFromWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.StartPriceUpdatesUseCase
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlin.time.Duration.Companion.milliseconds

class CoinDetailViewModel(
    private val getCoinDetailUseCase: GetCoinDetailUseCase,
    private val getKlinesUseCase: GetKlinesUseCase,
    private val startPriceUpdatesUseCase: StartPriceUpdatesUseCase,
    private val addToWatchlistUseCase: AddToWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase,
) : ViewModel() {

    private val _uiState = MutableStateFlow(CoinDetailUiState())
    val uiState: StateFlow<CoinDetailUiState> = _uiState.asStateFlow()

    private var priceUpdateJob: Job? = null
    private var currentSymbol: String? = null

    fun onIntent(intent: CoinDetailIntent) {
        when (intent) {
            is CoinDetailIntent.LoadCoin -> loadCoin(intent.symbol)
            is CoinDetailIntent.PriceUpdated -> updatePrice(intent.newPrice)
            is CoinDetailIntent.ChangeInterval -> changeInterval(intent.interval)
            is CoinDetailIntent.ToggleWatchlist -> toggleWatchlist()
            is CoinDetailIntent.ShowRemoveDialog -> _uiState.update { it.copy(showRemoveDialog = true) }
            is CoinDetailIntent.DismissRemoveDialog -> _uiState.update { it.copy(showRemoveDialog = false) }
            is CoinDetailIntent.ConfirmRemoveWatchlist -> confirmRemoveWatchlist()
        }
    }

    private fun loadCoin(symbol: String) {
        currentSymbol = symbol
        loadKlines(symbol, _uiState.value.selectedInterval)
        viewModelScope.launch {
            getCoinDetailUseCase(symbol)
                .catch { e -> _uiState.update { it.copy(error = e.message) } }
                .collect { coin ->
                    val hadCoin = _uiState.value.coin != null
                    _uiState.update { it.copy(coin = coin?.toUiModel(isWatchlisted = coin.isWatchlisted)) }
                    if (hadCoin && coin != null) {
                        onIntent(CoinDetailIntent.PriceUpdated(symbol, coin.lastPrice))
                    }
                }
        }
        observePriceUpdates()
    }

    private fun loadKlines(symbol: String, interval: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            try {
                val (apiInterval, limit) = when (interval) {
                    "1h" -> "1m" to 60
                    "7d" -> "4h" to 42
                    else -> "1h" to 24  // "24h"
                }
                val klines = getKlinesUseCase(symbol, apiInterval, limit)
                val min = klines.minOfOrNull { it.price } ?: 0.0
                val max = klines.maxOfOrNull { it.price } ?: 0.0
                val range = max - min
                val padding = if (range < 0.01) max * 0.001 else range * 0.05
                _uiState.update {
                    it.copy(
                        pricePoints = klines,
                        chartYMin = min - padding,
                        chartYMax = max + padding,
                        isLoading = false,
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    private fun changeInterval(interval: String) {
        val symbol = currentSymbol ?: return
        _uiState.update { it.copy(selectedInterval = interval) }
        loadKlines(symbol, interval)
    }

    private fun observePriceUpdates() {
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

    private fun updatePrice(newPrice: Double) {
        _uiState.update { it.copy(livePrice = newPrice.formatPrice()) }
    }

    private fun toggleWatchlist() {
        val coin = _uiState.value.coin ?: return
        if (coin.isWatchlisted) {
            _uiState.update { it.copy(showRemoveDialog = true) }
        } else {
            _uiState.update { it.copy(coin = it.coin?.copy(isWatchlisted = true)) }
            viewModelScope.launch {
                try {
                    addToWatchlistUseCase(coin.symbol)
                } catch (e: Exception) {
                    _uiState.update { it.copy(coin = it.coin?.copy(isWatchlisted = false)) }
                }
            }
        }
    }

    private fun confirmRemoveWatchlist() {
        val symbol = _uiState.value.coin?.symbol ?: return
        _uiState.update { it.copy(showRemoveDialog = false, coin = it.coin?.copy(isWatchlisted = false)) }
        viewModelScope.launch {
            try {
                removeFromWatchlistUseCase(symbol)
            } catch (e: Exception) {
                _uiState.update { it.copy(coin = it.coin?.copy(isWatchlisted = true)) }
            }
        }
    }
}
