package com.htetarkarzaw.marketdashboard.android.ui.coinlist

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.repeatOnLifecycle
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.DismissError
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.LoadInitial
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.ReachedEnd
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.Refresh
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.StartPriceUpdates
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.AddToWatchlist
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListIntent.StopPriceUpdates
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Error
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Loading
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListUiState.Success
import com.htetarkarzaw.marketdashboard.android.ui.common.AppDialog
import kotlinx.coroutines.awaitCancellation
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CoinListScreen(
    onNavigateToWatchlist: () -> Unit = {},
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

    when (val state = uiState) {
        is Loading -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
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
            PullToRefreshBox(
                isRefreshing = false,
                onRefresh = { viewModel.onIntent(Refresh) },
                modifier = Modifier.fillMaxSize()
            ) {
                LazyColumn(modifier = Modifier.fillMaxSize()) {
                    itemsIndexed(
                        items = state.coins,
                        key = { _, coin -> coin.symbol }
                    ) { index, coin ->
                        val dismissState = rememberSwipeToDismissBoxState(
                            confirmValueChange = { value ->
                                if (value == SwipeToDismissBoxValue.EndToStart) {
                                    viewModel.onIntent(AddToWatchlist(coin.symbol))
                                }
                                false
                            }
                        )
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
