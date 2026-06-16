package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import coil3.compose.AsyncImage
import com.htetarkarzaw.marketdashboard.android.ui.model.CoinUiModel

@Composable
fun CoinListItem(coin: CoinUiModel, modifier: Modifier = Modifier, showStar: Boolean = true) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(modifier = Modifier.size(40.dp)) {
            AsyncImage(
                model = coin.iconUrl,
                contentDescription = coin.baseAsset,
                modifier = Modifier.size(40.dp)
            )
        }
        Column(
            modifier = Modifier.padding(start = 12.dp)
        ) {
            Text(
                text = coin.baseAsset,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = coin.symbol,
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        Column(horizontalAlignment = Alignment.End) {
            Text(
                text = coin.priceFormatted,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = coin.priceChangeFormatted,
                style = MaterialTheme.typography.bodySmall,
                color = if (coin.isPositiveChange) Color(0xFF00C853) else Color(0xFFD50000)
            )
        }
        if (showStar) {
            Icon(
                imageVector = if (coin.isWatchlisted) Icons.Filled.Star else Icons.Outlined.Star,
                contentDescription = if (coin.isWatchlisted) "Watchlisted" else "Not watchlisted",
                tint = if (coin.isWatchlisted) Color(0xFFFFB300) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .size(20.dp)
            )
        }
    }
}
