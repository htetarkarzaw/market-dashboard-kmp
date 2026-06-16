package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import com.htetarkarzaw.marketdashboard.android.ui.model.CoinUiModel

sealed class CoinListUiState {
    object Loading : CoinListUiState()
    data class Success(
        val coins: List<CoinUiModel>,
        val isLoadingMore: Boolean = false,
        val hasReachedEnd: Boolean = false,
        val errorMessage: String? = null
    ) : CoinListUiState()
    data class Error(val message: String) : CoinListUiState()
}
