import SwiftUI

struct CoinListScreen: View {
    @State private var viewModel = CoinListViewModel()
    @State private var showTopBar = false

    var body: some View {
        NavigationStack {
            Group {
                if viewModel.isLoading && viewModel.coins.isEmpty {
                    ProgressView()
                        .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else if let error = viewModel.error, viewModel.coins.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "exclamationmark.triangle")
                            .font(.largeTitle)
                        Text(error)
                            .multilineTextAlignment(.center)
                    }
                    .foregroundStyle(.secondary)
                    .padding()
                } else if !viewModel.isLoading && viewModel.coins.isEmpty {
                    VStack(spacing: 12) {
                        Image(systemName: "chart.bar.xaxis")
                            .font(.largeTitle)
                            .foregroundStyle(.secondary)
                        Text("No coins available")
                            .font(.headline)
                        Text("Pull down to refresh")
                            .font(.subheadline)
                            .foregroundStyle(.secondary)
                    }
                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                } else {
                    ZStack(alignment: .top) {
                        List {
                            MarketSummaryHeaderView(summary: viewModel.marketSummary)
                                .listRowSeparator(.hidden)
                                .listRowInsets(EdgeInsets(top: 8, leading: 16, bottom: 8, trailing: 16))
                                .onAppear { showTopBar = false }
                                .onDisappear { showTopBar = true }
                            ForEach(viewModel.coins, id: \.symbol) { coin in
                                CoinRowView(coin: coin)
                                    .listRowSeparator(.hidden)
                                    .onAppear {
                                        if coin.symbol == viewModel.coins.last?.symbol && !viewModel.isLoadingMore {
                                            Task { await viewModel.loadMore() }
                                        }
                                    }
                            }
                            if viewModel.isLoadingMore {
                                HStack {
                                    Spacer()
                                    ProgressView()
                                        .onAppear {
                                            Task { await viewModel.loadMore() }
                                        }
                                    Spacer()
                                }
                                .listRowSeparator(.hidden)
                            } else if viewModel.hasReachedEnd {
                                HStack(spacing: 12) {
                                    Rectangle()
                                        .fill(Color.secondary.opacity(0.3))
                                        .frame(height: 1)
                                    Text("All coins loaded")
                                        .font(.caption)
                                        .foregroundStyle(.secondary)
                                        .fixedSize()
                                    Rectangle()
                                        .fill(Color.secondary.opacity(0.3))
                                        .frame(height: 1)
                                }
                                .listRowInsets(EdgeInsets(top: 4, leading: 16, bottom: 4, trailing: 16))
                                .listRowSeparator(.hidden)
                                .listRowBackground(Color.clear)
                            }
                        }
                        .listStyle(.plain)
                        .refreshable {
                            await viewModel.refresh()
                        }

                        HStack {
                            Text("Market")
                                .font(.headline.weight(.semibold))
                        }
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(.horizontal, 16)
                        .padding(.bottom, 12)
                        .background {
                            Rectangle()
                                .fill(.regularMaterial)
                                .ignoresSafeArea(edges: .top)
                        }
                        .opacity(showTopBar ? 1 : 0)
                        .animation(.easeInOut(duration: 0.3), value: showTopBar)
                    }
                }
            }
            .navigationBarHidden(true)
            .overlay(alignment: .top) {
                if let errorMessage = viewModel.error, !viewModel.coins.isEmpty {
                    ErrorBannerView(message: errorMessage) {
                        viewModel.dismissError()
                    }
                    .transition(.move(edge: .top).combined(with: .opacity))
                    .padding(.horizontal)
                    .padding(.top, 4)
                }
            }
            .animation(.easeInOut(duration: 0.3), value: viewModel.error)
            .task {
                async let load: () = viewModel.loadCoins()
                async let prices: () = viewModel.startPriceUpdates()
                await load
                await prices
            }
        }
    }
}

private struct ErrorBannerView: View {
    let message: String
    let onDismiss: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            Text(message)
                .font(.subheadline)
                .foregroundStyle(.white)
                .multilineTextAlignment(.leading)
            Spacer()
            Button(action: onDismiss) {
                Image(systemName: "xmark")
                    .font(.subheadline.weight(.semibold))
                    .foregroundStyle(.white)
            }
        }
        .padding(.horizontal, 16)
        .padding(.vertical, 12)
        .background(Color.red)
        .clipShape(RoundedRectangle(cornerRadius: 10))
        .onAppear {
            Task {
                try? await Task.sleep(for: .seconds(3))
                onDismiss()
            }
        }
    }
}
