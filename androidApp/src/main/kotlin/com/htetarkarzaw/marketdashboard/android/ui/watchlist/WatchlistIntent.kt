package com.htetarkarzaw.marketdashboard.android.ui.watchlist

sealed class WatchlistIntent {
    object LoadWatchlist : WatchlistIntent()
    data class RemoveFromWatchlist(val coinId: String) : WatchlistIntent()
}
