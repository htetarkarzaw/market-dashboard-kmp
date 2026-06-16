package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.AddToWatchlist
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.DismissError
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.LoadInitial
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.ReachedEnd
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.Refresh
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.StartPriceUpdates
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.StopPriceUpdates
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Error
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Loading
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Success
import com.htetarkarzaw.marketdashboard.android.ui.common.AppDialog
import com.htetarkarzaw.marketdashboard.android.ui.model.MarketSummaryUiModel
import kotlinx.coroutines.awaitCancellation
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListScreen(
    viewModel: CoinListViewModel = koinViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.onIntent(LoadInitial)
    }

    val lifecycle = LocalLifecycleOwner.current.lifecycle
    LaunchedEffect(lifecycle) {
        lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            try {
                viewModel.onIntent(StartPriceUpdates)
                awaitCancellation()
            } finally {
                viewModel.onIntent(StopPriceUpdates)
            }
        }
    }

    val listState = rememberLazyListState()
    val showTopBar by remember { derivedStateOf { listState.firstVisibleItemIndex > 0 } }
    val pullToRefreshState = rememberPullToRefreshState()


    Box(modifier = Modifier.fillMaxSize()) {
        PullToRefreshBox(
            isRefreshing = (uiState as? Success)?.isRefreshing ?: false,
            onRefresh = { viewModel.onIntent(Refresh) },
            modifier = Modifier.fillMaxSize(),
            state = pullToRefreshState
        ) {
            when (val state = uiState) {
                is Loading -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                is Error -> {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(text = state.message)
                        TextButton(onClick = { viewModel.onIntent(LoadInitial) }) {
                            Text("Retry")
                        }
                    }
                }
                is Success -> {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        item {
                            MarketSummaryHeader(
                                summary = state.marketSummary,
                                modifier = Modifier.padding(
                                    16.dp
                                )
                            )
                        }
                        itemsIndexed(
                            items = state.coins,
                            key = { _, coin -> coin.symbol }
                        ) { index, coin ->
                            val dismissState = rememberSwipeToDismissBoxState()
                            LaunchedEffect(dismissState.currentValue) {
                                if (dismissState.currentValue == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.onIntent(AddToWatchlist(coin.symbol))
                                    dismissState.snapTo(SwipeToDismissBoxValue.Settled)
                                }
                            }
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = false,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxSize()
                                            .background(Color(0xFF4CAF50))
                                            .padding(end = 16.dp),
                                        contentAlignment = Alignment.CenterEnd
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Star,
                                            contentDescription = "Add to watchlist",
                                            tint = Color.White
                                        )
                                    }
                                }
                            ) {
                                CoinListItem(coin = coin)
                            }
                            val shouldLoadMore = index == state.coins.lastIndex && !state.isLoadingMore
                            LaunchedEffect(state.coins.size) {
                                if (shouldLoadMore) {
                                    viewModel.onIntent(ReachedEnd)
                                }
                            }
                        }
                        if (state.isLoadingMore) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CircularProgressIndicator()
                                }
                            }
                        }
                        if (state.hasReachedEnd) {
                            item {
                                Text(
                                    text = "You've reached the end",
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(16.dp),
                                    textAlign = TextAlign.Center,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    state.errorMessage?.let { message ->
                        AppDialog(
                            title = "Error",
                            message = message,
                            confirmText = "Retry",
                            dismissText = "Cancel",
                            onConfirm = {
                                viewModel.onIntent(DismissError)
                                viewModel.onIntent(Refresh)
                            },
                            onDismiss = { viewModel.onIntent(DismissError) }
                        )
                    }
                }
            }
        }
        AnimatedVisibility(
            visible = showTopBar,
            enter = fadeIn(animationSpec = tween(300)),
            exit = fadeOut(animationSpec = tween(300)),
            modifier = Modifier.align(Alignment.TopCenter)
        ) {
            TopAppBar(
                title = { Text("Market") },
                colors = TopAppBarDefaults.topAppBarColors(),
                windowInsets = WindowInsets(0)
            )
        }
    }
}

@Composable
fun MarketSummaryHeader(
    summary: MarketSummaryUiModel?,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Text(
            text = "Market",
            style = MaterialTheme.typography.headlineLarge,
            fontWeight = FontWeight.Bold
        )
        summary?.let {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Vol ${it.totalVolumeFormatted}",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = "▲ ${it.topGainerSymbol} ${it.topGainerFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF4CAF50)
                )
                Text(
                    text = "▼ ${it.topLoserSymbol} ${it.topLoserFormatted}",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFFF44336)
                )
            }
        }
    }
}
