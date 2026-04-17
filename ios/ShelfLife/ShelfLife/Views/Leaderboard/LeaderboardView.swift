import SwiftUI

struct LeaderboardView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = LeaderboardViewModel()

    var body: some View {
        NavigationStack {
            VStack(spacing: 0) {
                modePicker

                if vm.isLoading {
                    Spacer()
                    ProgressView("Loading leaderboard...")
                    Spacer()
                } else if vm.entries.isEmpty {
                    emptyState
                } else {
                    leaderboardList
                }
            }
            .navigationTitle("Leaderboard")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }
                }
            }
        }
        .onAppear { vm.loadEntries() }
    }

    private var modePicker: some View {
        Picker("Mode", selection: Binding(
            get: { vm.mode },
            set: { vm.mode = $0; vm.loadEntries() }
        )) {
            Label("RAT Points 🐀", systemImage: "pawprint.fill").tag(LeaderboardMode.rat)
            Label("STINKY Points 💀", systemImage: "skull.fill").tag(LeaderboardMode.stinky)
        }
        .pickerStyle(.segmented)
        .padding()
    }

    private var leaderboardList: some View {
        ScrollView {
            VStack(spacing: 12) {
                ForEach(vm.entries) { entry in
                    LeaderboardEntryRow(
                        entry: entry,
                        isCurrentUser: entry.id == UserService.shared.currentSLUser?.id,
                        onKingTap: entry.rank == 1 ? { vm.playKingSound() } : nil
                    )
                }
            }
            .padding()
        }
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "trophy").font(.system(size: 60)).foregroundStyle(.secondary)
            Text("No scores yet").font(.headline)
            Text("Points are tracked as you use the app.")
                .foregroundStyle(.secondary)
            Spacer()
        }
        .padding()
    }
}

struct LeaderboardEntryRow: View {
    let entry: LeaderboardEntry
    let isCurrentUser: Bool
    let onKingTap: (() -> Void)?

    var body: some View {
        HStack(spacing: 16) {
            rankBadge

            Text(entry.username)
                .font(.subheadline.bold())
                .foregroundStyle(isCurrentUser ? .green : .primary)

            if isCurrentUser {
                Text("(You)").font(.caption).foregroundStyle(.secondary)
            }

            Spacer()

            Text("\(entry.points)")
                .font(.headline)
                .foregroundStyle(rankColor)

            if entry.rank == 1, let onKingTap {
                Button {
                    onKingTap()
                } label: {
                    Image(systemName: "crown.fill")
                        .foregroundStyle(.yellow)
                }
            }
        }
        .padding()
        .background(
            RoundedRectangle(cornerRadius: 12)
                .fill(entry.rank == 1 ? Color.yellow.opacity(0.1) : Color(.systemGray6))
                .overlay(
                    RoundedRectangle(cornerRadius: 12)
                        .stroke(entry.rank == 1 ? Color.yellow : Color.clear, lineWidth: 2)
                )
        )
    }

    private var rankBadge: some View {
        ZStack {
            Circle()
                .fill(rankColor.opacity(0.15))
                .frame(width: 36, height: 36)
            if entry.rank <= 3 {
                Text(rankEmoji).font(.subheadline)
            } else {
                Text("\(entry.rank)").font(.caption.bold()).foregroundStyle(rankColor)
            }
        }
    }

    private var rankEmoji: String {
        switch entry.rank {
        case 1: return "🥇"
        case 2: return "🥈"
        case 3: return "🥉"
        default: return "\(entry.rank)"
        }
    }

    private var rankColor: Color {
        switch entry.rank {
        case 1: return .yellow
        case 2: return Color(red: 0.75, green: 0.75, blue: 0.75)
        case 3: return Color(red: 0.8, green: 0.5, blue: 0.2)
        default: return .primary
        }
    }
}
