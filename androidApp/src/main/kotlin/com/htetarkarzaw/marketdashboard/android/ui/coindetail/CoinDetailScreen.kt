package com.htetarkarzaw.marketdashboard.android.ui.coindetail

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.StarOutline
import androidx.compose.foundation.clickable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImage
import com.htetarkarzaw.marketdashboard.domain.model.PricePoint
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.offset
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLine
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianLayerRangeProvider
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.cartesian.layer.LineCartesianLayer
import com.htetarkarzaw.marketdashboard.android.ui.common.AppDialog
import org.koin.androidx.compose.koinViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val Green = Color(0xFF4CAF50)
private val Red = Color(0xFFF44336)
private val Intervals = listOf("1h", "24h", "7d")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinDetailScreen(
    symbol: String,
    navigateUp: () -> Unit,
    viewModel: CoinDetailViewModel = koinViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var isNavigatingBack by remember { mutableStateOf(false) }
    val onBackClick = {
        if (!isNavigatingBack) {
            isNavigatingBack = true
            navigateUp()
        }
    }

    LaunchedEffect(symbol) {
        viewModel.onIntent(CoinDetailIntent.LoadCoin(symbol))
    }

    Scaffold(
        contentWindowInsets = WindowInsets(0),
        topBar = {
            TopAppBar(
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                title = {
                    Text(
                        text = "Market",
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.Bold,
                    )
                },
                colors = TopAppBarDefaults.topAppBarColors(),
                windowInsets = WindowInsets(0),
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                uiState.isLoading && uiState.coin == null -> {
                    CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
                }
                uiState.error != null && uiState.coin == null -> {
                    AppDialog(
                        title = "Connection Error",
                        message = "Unable to connect to Binance. Please check your internet connection.",
                        confirmText = "Retry",
                        dismissText = "Cancel",
                        onConfirm = { viewModel.onIntent(CoinDetailIntent.LoadCoin(symbol)) },
                        onDismiss = onBackClick,
                    )
                }
                uiState.coin != null -> {
                    val coin = uiState.coin!!
                    // Show live WebSocket price when available, otherwise fall back to REST snapshot
                    val displayPrice = uiState.livePrice.ifEmpty { coin.priceFormatted }
                    // key ensures interval/scroll state resets if symbol ever changes
                    key(coin.symbol) {
                        CoinDetailContent(
                            symbol = coin.symbol,
                            baseAsset = coin.baseAsset,
                            iconUrl = coin.iconUrl,
                            priceFormatted = displayPrice,
                            priceChangeFormatted = coin.priceChangeFormatted,
                            isPositiveChange = coin.isPositiveChange,
                            highFormatted = coin.highFormatted,
                            lowFormatted = coin.lowFormatted,
                            volumeFormatted = coin.volumeFormatted,
                            isWatchlisted = coin.isWatchlisted,
                            onToggleWatchlist = { viewModel.onIntent(CoinDetailIntent.ToggleWatchlist) },
                            pricePoints = uiState.pricePoints,
                            chartYMin = uiState.chartYMin,
                            chartYMax = uiState.chartYMax,
                            selectedInterval = uiState.selectedInterval,
                            onChangeInterval = { viewModel.onIntent(CoinDetailIntent.ChangeInterval(it)) },
                        )
                    }
                }
            }

            if (uiState.showRemoveDialog) {
                AppDialog(
                    title = "Remove from watchlist?",
                    message = "${uiState.coin?.baseAsset ?: ""} will be removed from your watchlist.",
                    confirmText = "Confirm",
                    dismissText = "Cancel",
                    onConfirm = { viewModel.onIntent(CoinDetailIntent.ConfirmRemoveWatchlist) },
                    onDismiss = { viewModel.onIntent(CoinDetailIntent.DismissRemoveDialog) },
                )
            }
        }
    }
}

@Composable
private fun CoinDetailContent(
    symbol: String,
    baseAsset: String,
    iconUrl: String,
    priceFormatted: String,
    priceChangeFormatted: String,
    isPositiveChange: Boolean,
    highFormatted: String,
    lowFormatted: String,
    volumeFormatted: String,
    isWatchlisted: Boolean,
    onToggleWatchlist: () -> Unit,
    pricePoints: List<PricePoint>,
    chartYMin: Double,
    chartYMax: Double,
    selectedInterval: String,
    onChangeInterval: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp),
    ) {
        // Only reads symbol/baseAsset/iconUrl/isWatchlisted — skips recomposition on price ticks
        CoinHeader(
            symbol = symbol,
            baseAsset = baseAsset,
            iconUrl = iconUrl,
            isWatchlisted = isWatchlisted,
            onToggleWatchlist = onToggleWatchlist,
        )
        // Reads live price fields — intentionally recomposes on every tick
        PriceBlock(
            priceFormatted = priceFormatted,
            priceChangeFormatted = priceChangeFormatted,
            isPositiveChange = isPositiveChange,
        )
        IntervalSelector(selected = selectedInterval, onSelect = onChangeInterval)
        PriceChart(
            pricePoints = pricePoints,
            chartYMin = chartYMin,
            chartYMax = chartYMax,
            livePrice = priceFormatted,
            selectedInterval = selectedInterval,
        )
        StatsGrid(
            highFormatted = highFormatted,
            lowFormatted = lowFormatted,
            volumeFormatted = volumeFormatted,
            changeFormatted = priceChangeFormatted,
        )
        LiveIndicator()
        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun CoinHeader(
    symbol: String,
    baseAsset: String,
    iconUrl: String,
    isWatchlisted: Boolean,
    onToggleWatchlist: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        AsyncImage(
            model = iconUrl,
            contentDescription = baseAsset,
            modifier = Modifier.size(48.dp),
        )
        Column {
            Text(
                text = baseAsset,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            Text(
                text = symbol,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        Spacer(modifier = Modifier.weight(1f))
        IconButton(onClick = onToggleWatchlist) {
            Icon(
                imageVector = if (isWatchlisted) Icons.Filled.Star else Icons.Outlined.StarOutline,
                contentDescription = if (isWatchlisted) "Remove from watchlist" else "Add to watchlist",
                tint = if (isWatchlisted) Color(0xFFFFB800) else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(28.dp),
            )
        }
    }
}

@Composable
private fun PriceBlock(
    priceFormatted: String,
    priceChangeFormatted: String,
    isPositiveChange: Boolean,
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(
            text = priceFormatted,
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold,
        )
        Box(
            modifier = Modifier
                .background(
                    color = if (isPositiveChange) Green else Red,
                    shape = RoundedCornerShape(4.dp),
                )
                .padding(horizontal = 8.dp, vertical = 3.dp)
        ) {
            Text(
                text = priceChangeFormatted,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun IntervalSelector(
    selected: String,
    onSelect: (String) -> Unit,
) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Intervals.forEach { interval ->
            val isSelected = interval == selected
            Box(
                modifier = Modifier
                    .background(
                        color = if (isSelected) Color.Black else Color.Transparent,
                        shape = RoundedCornerShape(50),
                    )
                    .then(
                        if (!isSelected) Modifier.background(
                            color = MaterialTheme.colorScheme.surfaceVariant,
                            shape = RoundedCornerShape(50),
                        ) else Modifier
                    )
                    .clickable { onSelect(interval) }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = interval,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) Color.White else MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}

@Composable
private fun PriceChart(
    pricePoints: List<PricePoint>,
    chartYMin: Double,
    chartYMax: Double,
    livePrice: String,
    selectedInterval: String,
) {
    if (pricePoints.isEmpty() || chartYMax <= chartYMin) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator()
        }
        return
    }

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(pricePoints) {
        modelProducer.runTransaction {
            lineSeries {
                series(
                    x = pricePoints.map { it.time.toDouble() },
                    y = pricePoints.map { it.price },
                )
            }
        }
    }

    val chartColor = remember(pricePoints) {
        if (pricePoints.size >= 2 && pricePoints.last().price >= pricePoints.first().price) {
            Color(0xFF639922)
        } else {
            Color(0xFFE24B4A)
        }
    }

    val dateFormat = remember(selectedInterval) {
        val pattern = if (selectedInterval == "7d") "EEE" else "HH:mm"
        SimpleDateFormat(pattern, Locale.getDefault())
    }
    val timeFormatter = remember(selectedInterval) {
        CartesianValueFormatter { _, value, _ ->
            dateFormat.format(Date(value.toLong()))
        }
    }
    val xLabelSpacing = remember(selectedInterval) {
        when (selectedInterval) {
            "1h" -> 15   // 60 points / 4 labels
            "7d" -> 6    // 42 points / 7 labels
            else -> 4    // 24 points / 6 labels  ("24h")
        }
    }
    val xItemPlacer = remember(xLabelSpacing) {
        HorizontalAxis.ItemPlacer.aligned(spacing = xLabelSpacing)
    }
    val priceFormatter = remember {
        CartesianValueFormatter { _, value, _ -> formatChartPrice(value) }
    }

    val rangeProvider = remember(chartYMin, chartYMax) {
        CartesianLayerRangeProvider.fixed(minY = chartYMin, maxY = chartYMax)
    }

    val lineFill = remember(chartColor) { LineCartesianLayer.LineFill.single(fill(chartColor)) }
    val line = LineCartesianLayer.rememberLine(fill = lineFill)
    val lineProvider = remember(line) { LineCartesianLayer.LineProvider.series(line) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp),
    ) {
        CartesianChartHost(
            chart = rememberCartesianChart(
                rememberLineCartesianLayer(
                    lineProvider = lineProvider,
                    rangeProvider = rangeProvider,
                ),
                startAxis = VerticalAxis.rememberStart(
                    valueFormatter = priceFormatter,
                    itemPlacer = VerticalAxis.ItemPlacer.count({ 5 }),
                ),
                bottomAxis = HorizontalAxis.rememberBottom(
                    valueFormatter = timeFormatter,
                    itemPlacer = xItemPlacer,
                ),
            ),
            modelProducer = modelProducer,
            animationSpec = null,
            modifier = Modifier.fillMaxSize(),
        )

        if (livePrice.isNotEmpty()) {
            val lastPrice = pricePoints.last().price
            BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                val chartTopPad = 8.dp
                val chartBottomAxis = 24.dp
                val plotHeight = maxHeight - chartTopPad - chartBottomAxis
                val fraction = ((chartYMax - lastPrice) / (chartYMax - chartYMin))
                    .coerceIn(0.0, 1.0)
                    .toFloat()
                val yOffset = chartTopPad + plotHeight * fraction

                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .offset(y = yOffset - 10.dp),
                ) {
                    Text(
                        text = livePrice,
                        color = Color.White,
                        fontSize = 11.sp,
                        modifier = Modifier
                            .background(chartColor, RoundedCornerShape(50))
                            .padding(horizontal = 8.dp, vertical = 3.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsGrid(
    highFormatted: String,
    lowFormatted: String,
    volumeFormatted: String,
    changeFormatted: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(label = "24h High", value = highFormatted, modifier = Modifier.weight(1f))
            StatCard(label = "24h Low", value = lowFormatted, modifier = Modifier.weight(1f))
        }
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            StatCard(label = "24h Volume", value = volumeFormatted, modifier = Modifier.weight(1f))
            StatCard(label = "Change", value = changeFormatted, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
            )
        }
    }
}

@Composable
private fun LiveIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "live")
    val dotAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "dot",
    )

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .alpha(dotAlpha)
                .background(Green, CircleShape)
        )
        Text(
            text = "Live via Binance WebSocket",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun formatChartPrice(value: Double): String = when {
    value >= 1_000_000 -> "$" + "%.1f".format(value / 1_000_000) + "M"
    value >= 1_000 -> "$" + "%.1f".format(value / 1_000) + "k"
    value >= 1 -> "$" + "%.2f".format(value)
    value >= 0.01 -> "$" + "%.4f".format(value)
    else -> "$" + "%.6f".format(value)
}
