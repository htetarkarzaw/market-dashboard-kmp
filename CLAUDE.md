# MarketDashboardKMP — Project Context

## What is this project?
A Kotlin Multiplatform (KMP) crypto market dashboard app.
- Android UI: Jetpack Compose
- iOS UI: SwiftUI (Phase 2)
- Shared business logic: Clean Architecture + MVI

## Current Status
- Phase 1: Android coin list screen with Binance API data ← IN PROGRESS
- Phase 2: WebSocket live price updates
- Phase 3: Watchlist feature
- Phase 4: iOS SwiftUI UI

## Current Branch
feature/ui

## Architecture
- sharedLogic: domain + data layers (pure KMP)
- androidApp: Compose UI + ViewModel (MVI)
- iosApp: SwiftUI (not started)

## Data Source
Binance public API (no key needed)
- REST: /api/v3/ticker/24hr for coin list
- WebSocket planned for Phase 2

## Key Decisions
- Offline-first: network → SQLDelight → Flow → UI
- Single source of truth: UI only observes DB
- quoteVolume used for sorting/filtering (not base asset volume)
- Client-side paging: 20 items per page from SQLDelight

---

# Claude Code Rules for MarketDashboardKMP

## Architecture
1. Always follow Clean Architecture — domain layer must have zero platform dependencies
2. Always use MVI pattern — single UiState per screen, sealed classes for Intent and Effect
3. sharedLogic is pure KMP — never add Android or iOS imports here
4. androidApp — Compose UI, ViewModel, UiModel, Mapper only
5. iosApp — SwiftUI only

## Libraries
6. Use Ktor for networking
7. Use SQLDelight for local database
8. Use Koin for DI
9. Coroutines and Flow for async operations — no callbacks

## Layer Separation
10. Always separate DTO (data layer) from Domain Model (domain layer)
11. Always separate Domain Model from UiModel (presentation layer)
12. Repository interface lives in domain layer
13. Repository implementation lives in data layer

## MVI Pattern Rules
18. Every screen has exactly three components: UiState, Intent, ViewModel
19. UiState is a sealed class with Loading, Success, Error at minimum
20. Intent is a sealed class — all user actions go through onIntent()
21. ViewModel never exposes mutable state directly — only StateFlow<UiState>
22. Screen only calls viewModel.onIntent() — no other public ViewModel methods except simple delegators
23. Screen never contains business logic or calculations
24. Screen never reads ViewModel internal state directly
25. All side effects (navigation, toasts) go through a separate Effect sealed class via SharedFlow
26. ViewModel functions that are not onIntent() must be simple delegators to onIntent()

## Testing Rules
28. Whenever a method is added to an interface that has a Fake implementation in the test sources (e.g. FakeBinanceApi, FakeCoinRepository), always update the fake with the new method in the same change. Never leave a fake out of sync with its interface. If the fake is a stub, return a sensible default (emptyList(), null, Unit). Run ./gradlew :sharedLogic:allTests before considering any task complete.

## Git Rules
14. After creating any new source file (.kt, .swift, .sq, .gradle.kts, .md), run: git add <filename>
15. Never git add build outputs, .gradle folders, or generated files
16. Always respect .gitignore rules
17. Never stage: build/, .gradle/, *.class, local.properties, *.iml, .DS_Store
27. Do not add Co-Authored-By trailer to commit messages

## "Ship It" Workflow
When the user says "ship it", execute these steps in order. Stop immediately and report the error clearly if any step fails — do not continue.

1. `git add -A`
2. `git commit -m "<conventional commit message based on what changed>"`
3. `git push origin <current-branch>`
4. `gh pr create --base develop --fill`
5. `gh pr merge --auto --squash`
6. Echo: "PR created with auto-merge enabled. Switching to develop — check GitHub Actions to confirm CI passes and the PR merges."
7. `git checkout develop`
8. `git pull origin develop`
