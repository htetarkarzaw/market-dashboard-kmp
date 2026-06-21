import SwiftUI
import SharedLogic

@MainActor
@Observable class CoinListViewModel {
    var coins: [Coin] = []
    var marketSummary: MarketSummary? = nil
    var isLoading: Bool = false
    var isLoadingMore: Bool = false
    var hasReachedEnd: Bool = false
    var error: String? = nil
    var currentPage: Int = 1

    private let pageSize = 20
    private let getCoinsUseCase = KoinHelperKt.makeGetCoinsUseCase()
    private let refreshCoinsUseCase = KoinHelperKt.makeRefreshCoinsUseCase()
    private let startPriceUpdatesUseCase = KoinHelperKt.makeStartPriceUpdatesUseCase()
    private let getMarketSummaryUseCase = KoinHelperKt.makeGetMarketSummaryUseCase()

    nonisolated(unsafe) private var cancelObservers: [() -> Void] = []
    nonisolated(unsafe) private var marketSummaryCancelToken: (() -> Void)? = nil
    private var refreshContinuation: CheckedContinuation<Void, Never>? = nil

    deinit {
        cancelObservers.forEach { $0() }
        marketSummaryCancelToken?()
    }

    func loadCoins() async {
        currentPage = 1
        hasReachedEnd = false
        coins = []
        isLoading = true
        defer { isLoading = false }
        do {
            try await refreshCoinsUseCase.invoke()
            restartObserver()
            marketSummaryCancelToken?()
            observeMarketSummary()
        } catch {
            self.error = error.localizedDescription
        }
    }

    func refresh() async {
        await withCheckedContinuation { (continuation: CheckedContinuation<Void, Never>) in
            refreshContinuation = continuation
            Task {
                currentPage = 1
                hasReachedEnd = false
                do {
                    try await refreshCoinsUseCase.invoke()
                    restartObserver()
                } catch {
                    self.error = error.localizedDescription
                    self.refreshContinuation?.resume()
                    self.refreshContinuation = nil
                }
            }
        }
    }

    func loadMore() async {
        guard !isLoadingMore && !hasReachedEnd else { return }
        isLoadingMore = true
        currentPage += 1
        restartObserver()
    }

    func observeMarketSummary() {
        marketSummaryCancelToken = FlowHelperKt.observeMarketSummary(
            useCase: getMarketSummaryUseCase,
            onUpdate: { [weak self] summary in
                self?.marketSummary = summary
            },
            onError: { _ in }
        )
    }

    private func restartObserver() {
        cancelObservers.forEach { $0() }
        cancelObservers.removeAll()
        let limit = currentPage * pageSize
        let cancel = FlowHelperKt.observeCoins(
            useCase: getCoinsUseCase,
            page: 0,
            pageSize: Int32(limit),
            onUpdate: { [weak self] updatedCoins in
                guard let self else { return }
                self.coins = updatedCoins
                self.isLoadingMore = false
                self.hasReachedEnd = updatedCoins.count < limit
                self.refreshContinuation?.resume()
                self.refreshContinuation = nil
            },
            onError: { [weak self] message in
                if message.contains("CancellationException") || message.contains("Job was cancelled") {
                    return
                }
                self?.isLoadingMore = false
                self?.error = message
                self?.refreshContinuation?.resume()
                self?.refreshContinuation = nil
            }
        )
        cancelObservers.append(cancel)
    }

    func dismissError() {
        error = nil
    }

    func startPriceUpdates() async {
        do {
            try await startPriceUpdatesUseCase.invoke()
        } catch {
            self.error = error.localizedDescription
        }
    }
}
