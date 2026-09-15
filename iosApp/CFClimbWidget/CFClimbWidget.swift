//
//  CFClimbWidget.swift
//  CFClimbWidget
//
//  Next round and the saved handle's rating on the home screen. The app writes a snapshot to
//  the shared App Group (see HomeWidget.ios.kt), so the extension doesn't load the Kotlin framework.
//

import SwiftUI
import WidgetKit

/// Keys must match `HomeWidget.ios.kt`.
private enum SnapshotKey {
    static let appGroup = "group.com.codeforcesvisualizer.iosApp"
    static let roundName = "widget.nextRoundName"
    static let roundStart = "widget.nextRoundStart"
    static let handle = "widget.handle"
    static let rating = "widget.rating"
    static let tierName = "widget.tierName"
    static let tierColor = "widget.tierColor"
}

struct RoundEntry: TimelineEntry {
    let date: Date
    let roundName: String?
    let roundStart: Date?
    let handle: String?
    let rating: Int?
    let tierName: String?
    let tierColor: Color

    static let sample = RoundEntry(
        date: .now,
        roundName: "Codeforces Round (Div. 2)",
        roundStart: .now.addingTimeInterval(2 * 24 * 3600),
        handle: "tourist",
        rating: 3301,
        tierName: "legendary grandmaster",
        tierColor: .red
    )
}

struct RoundProvider: TimelineProvider {
    func placeholder(in context: Context) -> RoundEntry { .sample }

    func getSnapshot(in context: Context, completion: @escaping (RoundEntry) -> Void) {
        completion(context.isPreview ? .sample : loadEntry())
    }

    func getTimeline(in context: Context, completion: @escaping (Timeline<RoundEntry>) -> Void) {
        let entry = loadEntry()
        // The countdown text updates on its own; ask again once the round starts so the widget
        // doesn't keep showing it, or hourly when there is nothing scheduled.
        let refresh = entry.roundStart.map { max($0, .now.addingTimeInterval(60)) } ?? .now.addingTimeInterval(3600)
        completion(Timeline(entries: [entry], policy: .after(refresh)))
    }

    private func loadEntry() -> RoundEntry {
        let defaults = UserDefaults(suiteName: SnapshotKey.appGroup)
        let start = defaults?.object(forKey: SnapshotKey.roundStart) as? Double
        let argb = UInt32(truncatingIfNeeded: defaults?.integer(forKey: SnapshotKey.tierColor) ?? 0)
        let upcoming = start.map { Date(timeIntervalSince1970: $0) }.flatMap { $0 > .now ? $0 : nil }
        return RoundEntry(
            date: .now,
            roundName: upcoming == nil ? nil : defaults?.string(forKey: SnapshotKey.roundName),
            roundStart: upcoming,
            handle: defaults?.string(forKey: SnapshotKey.handle),
            rating: defaults?.object(forKey: SnapshotKey.rating) as? Int,
            tierName: defaults?.string(forKey: SnapshotKey.tierName),
            tierColor: Color(
                red: Double((argb >> 16) & 0xFF) / 255,
                green: Double((argb >> 8) & 0xFF) / 255,
                blue: Double(argb & 0xFF) / 255
            )
        )
    }
}

struct CFClimbWidgetView: View {
    let entry: RoundEntry

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text("cf://next round")
                .font(.system(size: 11, design: .monospaced))
                .foregroundStyle(.tint)

            if let name = entry.roundName, let start = entry.roundStart {
                Text(name)
                    .font(.system(size: 14, weight: .bold))
                    .lineLimit(2)
                (Text("in ") + Text(start, style: .relative))
                    .font(.system(size: 12, design: .monospaced))
                    .foregroundStyle(.secondary)
                    .lineLimit(1)
            } else {
                Text("No upcoming rounds saved. Open CFClimb to refresh.")
                    .font(.system(size: 12))
                    .foregroundStyle(.secondary)
            }

            Spacer(minLength: 0)

            if let handle = entry.handle, let rating = entry.rating {
                // Verbatim, so the rating isn't shown with a thousands separator ("3,301").
                Text(verbatim: "\(handle) \(rating)")
                    .font(.system(size: 13, weight: .bold, design: .monospaced))
                    .foregroundStyle(entry.tierColor)
                    .lineLimit(1)
                if let tier = entry.tierName {
                    Text(tier)
                        .font(.system(size: 11, design: .monospaced))
                        .foregroundStyle(entry.tierColor)
                        .lineLimit(1)
                        .minimumScaleFactor(0.8)
                }
            }
        }
        .frame(maxWidth: .infinity, maxHeight: .infinity, alignment: .topLeading)
        .containerBackground(.fill.tertiary, for: .widget)
    }
}

@main
struct CFClimbWidget: Widget {
    let kind = "CFClimbWidget"

    var body: some WidgetConfiguration {
        StaticConfiguration(kind: kind, provider: RoundProvider()) { entry in
            CFClimbWidgetView(entry: entry)
        }
        .configurationDisplayName("Next round")
        .description("Next Codeforces round and your rating")
        .supportedFamilies([.systemSmall, .systemMedium])
    }
}

#Preview(as: .systemMedium) {
    CFClimbWidget()
} timeline: {
    RoundEntry.sample
}
