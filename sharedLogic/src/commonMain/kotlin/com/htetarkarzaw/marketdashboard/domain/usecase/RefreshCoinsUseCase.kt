package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository

class RefreshCoinsUseCase(private val repository: CoinRepository) {
    suspend operator fun invoke() = repository.refreshCoins()
}
