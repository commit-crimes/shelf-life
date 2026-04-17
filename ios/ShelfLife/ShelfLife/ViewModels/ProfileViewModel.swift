import Foundation
import SwiftUI
import Combine

@MainActor
class ProfileViewModel: ObservableObject {
    @Published var isShowingEasterEgg = false
    @Published var easterEggTapCount = 0
    @Published var errorMessage: String?
    @Published var isEditingUsername = false
    @Published var editedUsername = ""
    @Published var colorScheme: ColorScheme? = nil
    @Published var currentUser: SLUser?
    @Published var invitationCount = 0

    private let userService = UserService.shared
    private var cancellables = Set<AnyCancellable>()

    init() {
        if let saved = UserDefaults.standard.string(forKey: "colorScheme") {
            colorScheme = saved == "dark" ? .dark : saved == "light" ? .light : nil
        }

        userService.$currentSLUser
            .receive(on: RunLoop.main)
            .sink { [weak self] user in self?.currentUser = user }
            .store(in: &cancellables)

        InvitationService.shared.$invitations
            .receive(on: RunLoop.main)
            .map { $0.count }
            .sink { [weak self] count in self?.invitationCount = count }
            .store(in: &cancellables)
    }

    func handleAvatarTap() {
        easterEggTapCount += 1
        if easterEggTapCount >= 5 {
            isShowingEasterEgg = true
            easterEggTapCount = 0
        }
    }

    func startEditingUsername() {
        editedUsername = currentUser?.username ?? ""
        isEditingUsername = true
    }

    func saveUsername() async {
        guard !editedUsername.trimmingCharacters(in: .whitespaces).isEmpty else { return }
        do {
            try await userService.updateUsername(editedUsername.trimmingCharacters(in: .whitespaces))
            isEditingUsername = false
        } catch {
            errorMessage = error.localizedDescription
        }
    }

    func setColorScheme(_ scheme: ColorScheme?) {
        colorScheme = scheme
        if let scheme {
            UserDefaults.standard.set(scheme == .dark ? "dark" : "light", forKey: "colorScheme")
        } else {
            UserDefaults.standard.removeObject(forKey: "colorScheme")
        }
    }

    func signOut() {
        do {
            try AuthService.shared.signOut()
            userService.stopListening()
            HouseholdService.shared.stopListening()
            FoodItemService.shared.stopListening()
            RecipeService.shared.stopListening()
            InvitationService.shared.stopListening()
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
