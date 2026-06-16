package com.htetarkarzaw.marketdashboard

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ShowChart
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.htetarkarzaw.marketdashboard.android.navigation.CoinListRoute
import com.htetarkarzaw.marketdashboard.android.navigation.WatchlistRoute
import com.htetarkarzaw.marketdashboard.android.ui.coinlist.CoinListScreen
import com.htetarkarzaw.marketdashboard.android.ui.watchlist.WatchlistScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val backStack = rememberNavBackStack(CoinListRoute)

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = {
                        NavigationBar {
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
                ) { paddingValues ->
                    NavDisplay(
                        backStack = backStack,
                        modifier = Modifier.padding(paddingValues),
                        onBack = { backStack.removeLastOrNull() },
                        entryProvider = entryProvider {
                            entry<CoinListRoute> {
                                CoinListScreen(
                                    onNavigateToWatchlist = { backStack.add(WatchlistRoute) }
                                )
                            }
                            entry<WatchlistRoute> {
                                WatchlistScreen(
                                    onNavigateBack = { backStack.removeLastOrNull() }
                                )
                            }
                        }
                    )
                }
            }
        }
    }
}
