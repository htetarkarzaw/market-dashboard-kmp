import SwiftUI
import SharedLogic

struct CoinRowView: View {
    let coin: Coin

    var body: some View {
        HStack(spacing: 12) {
            AsyncImage(url: URL(string: coin.iconUrl)) { image in
                image.resizable().scaledToFit()
            } placeholder: {
                Circle().fill(Color.secondary.opacity(0.2))
            }
            .frame(width: 40, height: 40)
            .clipShape(Circle())

            VStack(alignment: .leading, spacing: 2) {
                Text(coin.baseAsset)
                    .font(.headline)
                Text(coin.symbol)
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }

            Spacer()

            VStack(alignment: .trailing, spacing: 2) {
                Text(coin.priceFormatted)
                    .font(.subheadline)
                    .fontWeight(.medium)

                let positive = coin.priceChangePercent >= 0
                Text(coin.priceChangeFormatted)
                    .font(.caption)
                    .padding(.horizontal, 6)
                    .padding(.vertical, 2)
                    .foregroundStyle(positive ? Color.green : Color.red)
                    .clipShape(Capsule())
            }
        }
        .padding(.vertical, 4)
    }
}
