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

## Git Rules
14. After creating any new source file (.kt, .swift, .sq, .gradle.kts, .md), run: git add <filename>
15. Never git add build outputs, .gradle folders, or generated files
16. Always respect .gitignore rules
17. Never stage: build/, .gradle/, *.class, local.properties, *.iml, .DS_Store
