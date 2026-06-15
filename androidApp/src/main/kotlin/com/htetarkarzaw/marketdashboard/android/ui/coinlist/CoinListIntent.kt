package com.htetarkarzaw.marketdashboard.android.ui.coinlist

sealed class CoinListIntent {
    object LoadInitial : CoinListIntent()
    object Refresh : CoinListIntent()
    object ReachedEnd : CoinListIntent()
    object DismissError : CoinListIntent()
    object StartPriceUpdates : CoinListIntent()
    object StopPriceUpdates : CoinListIntent()
}
