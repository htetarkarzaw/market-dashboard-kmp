import SwiftUI
import SharedLogic

struct CoinRowView: View {
    private let baseAsset: String
    private let symbol: String
    private let iconUrl: String
    private let priceFormatted: String
    private let priceChangeFormatted: String
    private let priceChangePercent: Double
    private let isWatchlisted: Bool

    init(coin: Coin) {
        baseAsset = coin.baseAsset
        symbol = coin.symbol
        iconUrl = coin.iconUrl
        priceFormatted = coin.priceFormatted
        priceChangeFormatted = coin.priceChangeFormatted
        priceChangePercent = coin.priceChangePercent
        isWatchlisted = coin.isWatchlisted
    }

    fileprivate init(preview: PreviewCoin) {
        baseAsset = preview.baseAsset
        symbol = preview.symbol
        iconUrl = preview.iconUrl
        priceFormatted = preview.priceFormatted
        priceChangeFormatted = preview.priceChangeFormatted
        priceChangePercent = preview.priceChangePercent
        isWatchlisted = preview.isWatchlisted
    }

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: iconUrl)) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Circle().fill(Color.secondary.opacity(0.2))
            }
            .frame(width: 40, height: 40)
            .clipShape(Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text(baseAsset)
                    .font(.body)
                    .fontWeight(.bold)
                Text(symbol)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(priceFormatted)
                    .font(.body)
                Text(priceChangeFormatted)
                    .font(.caption)
                    .foregroundStyle(priceChangePercent >= 0 ? Color(hex: "00C853") : Color(hex: "D50000"))
            }

            Image(systemName: isWatchlisted ? "star.fill" : "star")
                .foregroundStyle(isWatchlisted ? Color(hex: "FFB300") : .secondary)
                .frame(width: 20, height: 20)
                .padding(.leading, 8)
        }
        .padding(.horizontal, 8)
        
    }
}

private struct PreviewCoin {
    let baseAsset: String
    let symbol: String
    let priceFormatted: String
    let priceChangeFormatted: String
    let priceChangePercent: Double
    let iconUrl: String
    let isWatchlisted: Bool
}

extension Color {
    init(hex: String) {
        let hex = hex.trimmingCharacters(in: .alphanumerics.inverted)
        var int: UInt64 = 0
        Scanner(string: hex).scanHexInt64(&int)
        let r = Double((int >> 16) & 0xFF) / 255
        let g = Double((int >> 8) & 0xFF) / 255
        let b = Double(int & 0xFF) / 255
        self.init(red: r, green: g, blue: b)
    }
}

#Preview("Positive change") {
    CoinRowView(preview: PreviewCoin(
        baseAsset: "BTC",
        symbol: "BTCUSDT",
        priceFormatted: "$67,450.12",
        priceChangeFormatted: "+2.34%",
        priceChangePercent: 2.34,
        iconUrl: "",
        isWatchlisted: false
    ))
}

#Preview("Negative change") {
    CoinRowView(preview: PreviewCoin(
        baseAsset: "ETH",
        symbol: "ETHUSDT",
        priceFormatted: "$3,521.88",
        priceChangeFormatted: "-1.07%",
        priceChangePercent: -1.07,
        iconUrl: "",
        isWatchlisted: true
    ))
}
