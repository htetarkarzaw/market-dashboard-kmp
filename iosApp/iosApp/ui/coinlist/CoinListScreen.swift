import SwiftUI

struct CoinListScreen: View {
    @State private var viewModel = CoinListViewModel()

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
                    List {
                        ForEach(viewModel.coins, id: \.symbol) { coin in
                            CoinRowView(coin: coin)
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
                            .padding(.vertical, 8)
                            .listRowSeparator(.hidden)
                        }
                    }
                    .listStyle(.plain)
                    .refreshable {
                        await viewModel.refresh()
                    }
                }
            }
            .navigationTitle("Market")
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
