package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.repository.WatchlistRepository

class AddToWatchlistUseCase(private val repository: WatchlistRepository) {
    suspend operator fun invoke(coinId: String) = repository.addToWatchlist(coinId)
}
