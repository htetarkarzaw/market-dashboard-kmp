package com.htetarkarzaw.marketdashboard.android.ui.coindetail

import com.htetarkarzaw.marketdashboard.android.ui.model.CoinUiModel
import com.htetarkarzaw.marketdashboard.domain.model.PricePoint

data class CoinDetailUiState(
    val coin: CoinUiModel? = null,
    val pricePoints: List<PricePoint> = emptyList(),
    val livePrice: String = "",
    val chartYMin: Double = 0.0,
    val chartYMax: Double = 0.0,
    val selectedInterval: String = "1h",
    val showRemoveDialog: Boolean = false,
    val isLoading: Boolean = false,
    val error: String? = null,
)
