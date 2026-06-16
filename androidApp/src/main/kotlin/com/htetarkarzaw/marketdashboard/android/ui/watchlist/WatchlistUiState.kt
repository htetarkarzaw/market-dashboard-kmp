package com.htetarkarzaw.marketdashboard.android.ui.watchlist

import com.htetarkarzaw.marketdashboard.android.ui.model.CoinUiModel

sealed class WatchlistUiState {
    object Loading : WatchlistUiState()
    data class Success(val coins: List<CoinUiModel>) : WatchlistUiState()
    data class Error(val message: String) : WatchlistUiState()
}
