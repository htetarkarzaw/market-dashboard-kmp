package com.htetarkarzaw.marketdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.htetarkarzaw.marketdashboard.android.navigation.CoinDetailRoute
import com.htetarkarzaw.marketdashboard.android.navigation.CoinListRoute
import com.htetarkarzaw.marketdashboard.android.navigation.WatchlistRoute
import com.htetarkarzaw.marketdashboard.android.ui.coindetail.CoinDetailScreen
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListScreen
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val backStack = rememberNavBackStack(CoinListRoute)
                val showBottomNav = backStack.lastOrNull() !is CoinDetailRoute

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsPadding(WindowInsets.statusBars)
                ) {
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier
                            .fillMaxSize()
                            .navigationBarsPadding()
                            .then(if (showBottomNav) Modifier.padding(bottom = 80.dp) else Modifier),
                        onBack = { if (backStack.size > 1) backStack.removeLastOrNull() },
                        entryProvider = entryProvider {
                            entry<CoinListRoute> {
                                CoinListScreen(
                                    onCoinClick = { symbol -> backStack.add(CoinDetailRoute(symbol)) }
                                )
                            }
                            entry<WatchlistRoute> { WatchlistScreen() }
                            entry<CoinDetailRoute> { route ->
                                CoinDetailScreen(
                                    symbol = route.symbol,
                                    navigateUp = { backStack.removeLastOrNull() },
                                )
                            }
                        }
                    )
                    if (showBottomNav) NavigationBar(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        windowInsets = WindowInsets.navigationBars
                    ) {
                        NavigationBarItem(
                            selected = backStack.lastOrNull() is CoinListRoute,
                            onClick = {
                                backStack.clear()
                                backStack.add(CoinListRoute)
                            },
                            icon = { Icon(Icons.AutoMirrored.Filled.ShowChart, contentDescription = "Market") },
                            label = { Text("Market") }
                        )
                        NavigationBarItem(
                            selected = backStack.lastOrNull() is WatchlistRoute,
                            onClick = {
                                backStack.clear()
                                backStack.add(WatchlistRoute)
                            },
                            icon = { Icon(Icons.Default.Star, contentDescription = "Watchlist") },
                            label = { Text("Watchlist") }
                        )
                    }
                }
            }
        }
    }
}
