# MarketDashboardKMP

A Kotlin Multiplatform project targeting **Android** and **iOS** that delivers real-time crypto market data with offline caching, a personal watchlist, and a detailed coin view with historical price charts — built as a portfolio showcase for production-grade KMP architecture.

> **Data source:** [Binance Public API](https://binance-docs.github.io/apidocs/spot/en/) — WebSocket miniTicker stream for live prices + REST Klines endpoint for historical chart data.

---

## Features

- 📈 **Live price updates** via Binance WebSocket stream across all screens
- 📊 **Coin detail screen** — historical Klines chart (1h / 24h / 7d) with live price overlay
- 💹 **Green/red chart coloring** — based on price direction over the selected period
- 💾 **Offline caching** with SQLDelight — data persists across sessions
- ⭐ **Watchlist** — add/remove coins with swipe gestures and confirmation dialog
- 🔄 **MVI state management** — unidirectional data flow with Kotlin Coroutines + Flow
- 🧪 **Tested** — unit tests across shared logic, repository, serializer, and ViewModel layers

---

## Screenshots

> Android — Jetpack Compose

| Market List | Coin Detail | Watchlist |
|---|---|---|
| *(coming soon)* | *(coming soon)* | *(coming soon)* |

---

## Architecture

```
MarketDashboardKMP/
├── sharedLogic/          # KMP module — shared across Android & iOS
│   ├── commonMain/
│   │   ├── data/
│   │   │   ├── remote/   # Ktor HTTP client + Binance WebSocket + Klines REST
│   │   │   ├── local/    # SQLDelight database (CoinEntity, WatchlistEntity)
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/    # Coin, PricePoint, MarketSummary, WatchlistItem
│   │   │   ├── repository/
│   │   │   └── usecase/  # GetCoins, GetKlines, GetCoinDetail, StartPriceUpdates, Watchlist CRUD
│   │   └── di/           # Koin AppModule
│   ├── androidMain/      # Android-specific: OkHttp client, SQLDelight driver, debug interceptors
│   └── iosMain/          # iOS-specific: Darwin client, SQLDelight driver
│
├── androidApp/           # Android — Jetpack Compose + Navigation3
│   ├── ui/
│   │   ├── coinlist/     # CoinListScreen, ViewModel, MVI Intent/State
│   │   ├── coindetail/   # CoinDetailScreen, Vico chart, interval selector, watchlist toggle
│   │   └── watchlist/    # WatchlistScreen, swipe-to-remove
│   └── di/               # Koin AndroidModule
│
└── iosApp/               # iOS — SwiftUI entry point
```

### Data Flow

```
Binance WebSocket (miniTicker)          Binance REST (Klines)
         ↓                                       ↓
KtorBinanceWebSocketClient              KtorBinanceApi.getKlines()
         ↓                                       ↓
CoinRepositoryImpl  ←————————→  SQLDelight (offline cache)
         ↓                                       ↓
StartPriceUpdatesUseCase            GetKlinesUseCase → List<PricePoint>
         ↓                                       ↓
getCoinDetailUseCase (Flow)         CoinDetailViewModel
         ↓                                       ↓
ViewModel (MVI: Intent → State)         Vico LineChart
         ↓
Compose UI (price updates via DB Flow)
```

**Key design decisions:**
- WebSocket writes to SQLDelight; UI reads reactively via Flow — offline-first by default
- `getCoinDetailUseCase` uses a LEFT JOIN to combine coin price + watchlist state in one query
- Chart Y-axis locked to initial Klines range — WebSocket ticks only update the last data point, preventing chart distortion
- Compose recomposition optimized by passing primitives to child composables — only `PriceBlock` recomposes on each tick

---

## Tech Stack

| Layer | Library | Version |
|---|---|---|
| Networking | Ktor Client (OkHttp / Darwin) | 3.1.3 |
| WebSocket | Ktor WebSockets | 3.1.3 |
| Local DB | SQLDelight | 2.0.2 |
| DI | Koin | 4.0.3 |
| Async | Kotlinx Coroutines | 1.10.2 |
| Serialization | Kotlinx Serialization | 1.8.1 |
| UI (Android) | Jetpack Compose + Material3 | — |
| Charts | Vico | 2.0.0-beta.2 |
| Navigation | AndroidX Navigation3 | 1.1.2 |
| Image loading | Coil 3 | 3.2.0 |
| Logging | Napier | 2.7.1 |
| Testing | MockK + Turbine | 1.13.10 / 1.2.0 |
| Kotlin | 2.4.0 | — |

---

## Getting Started

### Prerequisites

- Android Studio Hedgehog or later
- JDK 17
- Xcode 15+ (for iOS)

### Clone

```bash
git clone https://github.com/htetarkarzaw/market-dashboard-kmp.git
cd market-dashboard-kmp
```

### Run Android

```bash
./gradlew :androidApp:assembleDebug
```

Or use the **Run** button in Android Studio targeting `androidApp`.

### Run iOS

Open `iosApp/iosApp.xcodeproj` in Xcode and press **Run** (⌘R).

---

## Running Tests

```bash
# Shared logic — KMP (all targets)
./gradlew :sharedLogic:allTests -Pkotlin.native.ignoreDisabledTargets=true

# Shared logic — JVM host only (faster)
./gradlew :sharedLogic:testAndroidHostTest

# Android ViewModel tests
./gradlew :androidApp:test

# Lint
./gradlew :androidApp:lintDebug
```

---

## CI

GitHub Actions runs on every push to `develop` and on PRs targeting `develop` or `main`:

1. Shared logic tests (KMP)
2. Android unit tests
3. Lint
4. Build Android APK (debug)

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable releases only |
| `develop` | Active development base |
| `feature/*` | Feature branches → PR into `develop` |

---

## Roadmap

- [x] Coin list with live WebSocket prices
- [x] Watchlist with swipe gestures
- [x] Coin detail screen with Klines chart
- [x] Green/red chart coloring by price direction
- [x] Interval selector (1h / 24h / 7d)
- [ ] iOS SwiftUI screens (CoinList + Watchlist + CoinDetail)
- [ ] Search and filter by coin name/symbol
- [ ] Dark / light theme toggle
- [ ] Widget support (Android)

---

## Author

**Billie (Htet Arkar Zaw)**  
Senior Android Engineer · Kotlin Multiplatform  
[GitHub](https://github.com/htetarkarzaw)
