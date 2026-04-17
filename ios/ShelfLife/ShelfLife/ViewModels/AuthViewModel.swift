import Foundation
import FirebaseAuth
import SwiftUI
import Combine

@MainActor
class AuthViewModel: ObservableObject {
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var isAuthenticated = false

    private let authService = AuthService.shared
    private var cancellables = Set<AnyCancellable>()

    init() {
        authService.$isAuthenticated
            .receive(on: RunLoop.main)
            .sink { [weak self] authenticated in
                self?.isAuthenticated = authenticated
            }
            .store(in: &cancellables)
    }

    func signInWithGoogle(presenting viewController: UIViewController) async {
        isLoading = true
        errorMessage = nil

        do {
            let user = try await authService.signInWithGoogle(presenting: viewController)
            try await UserService.shared.initializeUser(user)

            if let slUser = UserService.shared.currentSLUser {
                try await HouseholdService.shared.loadHouseholds(for: slUser)

                if !slUser.invitationUIDs.isEmpty {
                    InvitationService.shared.startListening(invitationUIDs: slUser.invitationUIDs)
                }
                if !slUser.recipeUIDs.isEmpty {
                    RecipeService.shared.startListening(recipeUIDs: slUser.recipeUIDs)
                }

                let selectedID = slUser.selectedHouseholdUID ?? HouseholdService.shared.households.first?.id
                if let householdID = selectedID {
                    FoodItemService.shared.startListening(householdID: householdID)
                }
            }
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }
}
