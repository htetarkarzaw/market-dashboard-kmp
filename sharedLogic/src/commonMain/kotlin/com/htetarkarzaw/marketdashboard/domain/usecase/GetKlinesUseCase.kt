package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.model.PricePoint
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository

class GetKlinesUseCase(private val repository: CoinRepository) {
    suspend operator fun invoke(symbol: String, interval: String = "1h", limit: Int = 24): List<PricePoint> {
        return repository.getKlines(symbol, interval, limit)
    }
}
