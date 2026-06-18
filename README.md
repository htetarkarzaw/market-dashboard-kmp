# MarketDashboardKMP

A Kotlin Multiplatform project targeting **Android** and **iOS** that delivers real-time crypto market data with offline caching and a personal watchlist — built as a portfolio showcase for production-grade KMP architecture.

> **Data source:** [Binance WebSocket API](https://binance-docs.github.io/apidocs/spot/en/#individual-symbol-mini-ticker-stream) — live ticker stream with REST fallback via Ktor.

---

## Features

- 📈 **Live price updates** via Binance WebSocket stream
- 💾 **Offline caching** with SQLDelight — data persists across sessions
- ⭐ **Watchlist** — add/remove coins with swipe gestures (Android) and local persistence
- 🔄 **MVI state management** — unidirectional data flow with Kotlin Coroutines + Flow
- 🧪 **Tested** — unit tests across shared logic, repository, mapper, and ViewModel layers

---

## Architecture

```
MarketDashboardKMP/
├── sharedLogic/          # KMP module — shared across Android & iOS
│   ├── commonMain/
│   │   ├── data/
│   │   │   ├── remote/   # Ktor HTTP + Binance WebSocket client
│   │   │   ├── local/    # SQLDelight database (CoinEntity, WatchlistEntity)
│   │   │   └── repository/
│   │   ├── domain/
│   │   │   ├── model/    # Coin, MarketSummary, WatchlistItem
│   │   │   ├── repository/
│   │   │   └── usecase/  # GetCoins, StartPriceUpdates, Watchlist CRUD
│   │   └── di/           # Koin AppModule
│   ├── androidMain/      # Android-specific: SQLDelight driver
│   └── iosMain/          # iOS-specific: SQLDelight driver
│
├── androidApp/           # Android — Jetpack Compose + Navigation3
│   ├── ui/
│   │   ├── coinlist/     # CoinListScreen, ViewModel, MVI Intent/State
│   │   └── watchlist/    # WatchlistScreen, swipe-to-remove
│   └── di/               # Koin AndroidModule
│
└── iosApp/               # iOS — SwiftUI entry point
```

### Data Flow

```
Binance WebSocket
       ↓
KtorBinanceWebSocketClient (Flow<List<TickerDto>>)
       ↓
CoinRepositoryImpl  ←→  SQLDelight (offline cache)
       ↓
Use Cases (GetCoinsUseCase, StartPriceUpdatesUseCase, ...)
       ↓
ViewModel (MVI: Intent → State)
       ↓
Compose UI
```

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
# Shared logic (JVM host)
./gradlew :sharedLogic:testAndroidHostTest

# Common tests (all targets)
./gradlew :sharedLogic:allTests -Pkotlin.native.ignoreDisabledTargets=true

# Android ViewModel tests
./gradlew :androidApp:test
```

---

## Branch Strategy

| Branch | Purpose |
|---|---|
| `main` | Stable releases only |
| `develop` | Active development base |
| `feature/*` | Feature branches → PR into `develop` |

CI runs on every push to `develop` and on PRs targeting `develop` or `main`.

---

## Roadmap

- [ ] iOS SwiftUI screens (CoinList + Watchlist)
- [ ] Price change sparkline chart
- [ ] Search and filter by coin name/symbol
- [ ] Dark / light theme toggle
- [ ] Widget support (Android)

---

## Author

**Billie (Htet Arkar Zaw)**  
Senior Android Engineer · Kotlin Multiplatform  
[GitHub](https://github.com/htetarkarzaw)
