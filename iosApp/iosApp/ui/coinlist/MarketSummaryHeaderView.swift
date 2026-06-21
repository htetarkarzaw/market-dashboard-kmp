import SwiftUI
import SharedLogic

struct MarketSummaryHeaderView: View {
    let summary: MarketSummary?

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("Market")
                .font(.largeTitle)
                .fontWeight(.bold)

            if let s = summary {
                Text("Vol \(formatVolume(s.totalVolume))")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)

                HStack(spacing: 16) {
                    Text("▲ \(formatSymbol(s.topGainerSymbol)) \(formatPercent(s.topGainerPercent))")
                        .foregroundStyle(.green)
                    Text("▼ \(formatSymbol(s.topLoserSymbol)) \(formatPercent(s.topLoserPercent))")
                        .foregroundStyle(.red)
                }
                .font(.caption)
            }
        }
        .frame(maxWidth: .infinity, alignment: .leading)
    }
}

private func formatSymbol(_ symbol: String) -> String {
    symbol.hasSuffix("USDT") ? String(symbol.dropLast(4)) : symbol
}

private func formatVolume(_ v: Double) -> String {
    let trillion = 1_000_000_000_000.0
    let billion  = 1_000_000_000.0
    let million  = 1_000_000.0
    switch v {
    case trillion...:
        return String(format: "$%.2fT", v / trillion)
    case billion...:
        return String(format: "$%.2fB", v / billion)
    default:
        return String(format: "$%.2fM", v / million)
    }
}

private func formatPercent(_ p: Double) -> String {
    p >= 0
        ? String(format: "+%.2f%%", p)
        : String(format: "-%.2f%%", abs(p))
}
