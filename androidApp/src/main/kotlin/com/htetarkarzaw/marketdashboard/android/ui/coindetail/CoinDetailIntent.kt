package com.htetarkarzaw.marketdashboard.android.ui.coindetail

sealed class CoinDetailIntent {
    data class LoadCoin(val symbol: String) : CoinDetailIntent()
    data class PriceUpdated(val symbol: String, val newPrice: Double) : CoinDetailIntent()
    data class ChangeInterval(val interval: String) : CoinDetailIntent()
    data object ToggleWatchlist : CoinDetailIntent()
    data object ShowRemoveDialog : CoinDetailIntent()
    data object DismissRemoveDialog : CoinDetailIntent()
    data object ConfirmRemoveWatchlist : CoinDetailIntent()
}
