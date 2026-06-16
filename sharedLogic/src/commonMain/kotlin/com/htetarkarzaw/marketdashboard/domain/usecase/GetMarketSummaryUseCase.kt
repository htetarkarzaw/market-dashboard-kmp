package com.htetarkarzaw.marketdashboard.domain.usecase

import com.htetarkarzaw.marketdashboard.domain.model.MarketSummary
import com.htetarkarzaw.marketdashboard.domain.repository.CoinRepository
import kotlinx.coroutines.flow.Flow

class GetMarketSummaryUseCase(private val repository: CoinRepository) {
    operator fun invoke(): Flow<MarketSummary> = repository.getMarketSummary()
}
