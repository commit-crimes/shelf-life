import Foundation
import AVFoundation

enum LeaderboardMode {
    case rat, stinky

    var title: String {
        switch self {
        case .rat: return "RAT Points"
        case .stinky: return "STINKY Points"
        }
    }

    var icon: String {
        switch self {
        case .rat: return "🐀"
        case .stinky: return "💀"
        }
    }
}

struct LeaderboardEntry: Identifiable {
    let id: String
    let username: String
    let points: Int
    let rank: Int
}

@MainActor
class LeaderboardViewModel: ObservableObject {
    @Published var mode: LeaderboardMode = .rat
    @Published var entries: [LeaderboardEntry] = []
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let householdService = HouseholdService.shared
    private var audioPlayer: AVAudioPlayer?

    init() {
        loadEntries()
    }

    func toggleMode() {
        mode = mode == .rat ? .stinky : .rat
        loadEntries()
    }

    func loadEntries() {
        guard let household = householdService.selectedHousehold else {
            entries = []
            return
        }

        let pointsMap = mode == .rat ? household.ratPoints : household.stinkyPoints
        isLoading = true

        Task {
            do {
                let users = try await UserService.shared.getUsers(byUIDs: Array(pointsMap.keys))
                let sorted = pointsMap.sorted { $0.value > $1.value }
                entries = sorted.prefix(5).enumerated().map { idx, pair in
                    let username = users.first(where: { $0.id == pair.key })?.username ?? "Unknown"
                    return LeaderboardEntry(id: pair.key, username: username, points: pair.value, rank: idx + 1)
                }
            } catch {
                errorMessage = error.localizedDescription
            }
            isLoading = false
        }
    }

    func playKingSound() {
        guard let url = Bundle.main.url(forResource: "king_sound", withExtension: "mp3") else { return }
        do {
            audioPlayer = try AVAudioPlayer(contentsOf: url)
            audioPlayer?.play()
        } catch {}
    }

    var currentUserRank: LeaderboardEntry? {
        guard let uid = UserService.shared.currentSLUser?.id else { return nil }
        return entries.first(where: { $0.id == uid })
    }

    var topEntry: LeaderboardEntry? { entries.first }
}
