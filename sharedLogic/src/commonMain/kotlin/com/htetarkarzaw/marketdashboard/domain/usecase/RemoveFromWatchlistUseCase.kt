package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.repository.WatchlistRepository

class RemoveFromWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(coinId: String) = repository.removeFromWatchlist(coinId)
}
