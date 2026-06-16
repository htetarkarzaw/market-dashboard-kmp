package com.htetarkarzaw.marketdashboard.android.ui.watchlist

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.htetarkarzaw.marketdashboard.android.ui.model.toUiModel
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistIntent.LoadWatchlist
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistIntent.RemoveFromWatchlist
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistUiState.Error
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistUiState.Loading
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistUiState.Success
import com.htetarkarzaw.marketdashboard.domain.usecase.GetWatchlistUseCase
import com.htetarkarzaw.marketdashboard.domain.usecase.RemoveFromWatchlistUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class WatchlistViewModel(
    private val getWatchlistUseCase: GetWatchlistUseCase,
    private val removeFromWatchlistUseCase: RemoveFromWatchlistUseCase
) : ViewModel() {

    private val _uiState: MutableStateFlow<WatchlistUiState> = MutableStateFlow(Loading)
    val uiState: StateFlow<WatchlistUiState> = _uiState.asStateFlow()

    fun onIntent(intent: WatchlistIntent) {
        when (intent) {
            is LoadWatchlist -> loadWatchlist()
            is RemoveFromWatchlist -> removeFromWatchlist(intent.coinId)
        }
    }

    private fun loadWatchlist() {
        viewModelScope.launch {
            getWatchlistUseCase()
                .catch { e -> _uiState.value = Error(e.message ?: "Unknown error") }
                .collect { coins ->
                    _uiState.value = Success(coins = coins.map { it.toUiModel() })
                }
        }
    }

    private fun removeFromWatchlist(coinId: String) {
        viewModelScope.launch {
            try {
                removeFromWatchlistUseCase(coinId)
            } catch (e: Exception) {
                _uiState.value = Error(e.message ?: "Unknown error")
            }
        }
    }
}
