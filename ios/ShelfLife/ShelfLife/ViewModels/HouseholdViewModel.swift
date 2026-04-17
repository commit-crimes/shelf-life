import Foundation

@MainActor
class HouseholdViewModel: ObservableObject {
    @Published var householdName = ""
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var inviteEmail = ""

    private let householdService = HouseholdService.shared
    private let userService = UserService.shared

    func populate(from household: Household) {
        householdName = household.name
    }

    func createHousehold() async -> Bool {
        guard !householdName.trimmingCharacters(in: .whitespaces).isEmpty else {
            errorMessage = "Please enter a household name"
            return false
        }
        guard let ownerUID = userService.currentSLUser?.id else { return false }

        isLoading = true
        do {
            let household = try await householdService.createHousehold(
                name: householdName.trimmingCharacters(in: .whitespaces),
                ownerUID: ownerUID
            )
            try await userService.addHouseholdUID(household.id)
            try await userService.updateSelectedHousehold(household.id)
            FoodItemService.shared.startListening(householdID: household.id)
            isLoading = false
            return true
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
            return false
        }
    }

    func updateHousehold(_ household: Household) async -> Bool {
        guard !householdName.trimmingCharacters(in: .whitespaces).isEmpty else {
            errorMessage = "Please enter a household name"
            return false
        }
        isLoading = true
        var updated = household
        updated.name = householdName.trimmingCharacters(in: .whitespaces)
        do {
            try await householdService.updateHousehold(updated)
            isLoading = false
            return true
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
            return false
        }
    }

    func deleteHousehold(_ household: Household) async -> Bool {
        isLoading = true
        do {
            try await householdService.deleteHousehold(household.id)
            try await userService.removeHouseholdUID(household.id)
            if userService.currentSLUser?.selectedHouseholdUID == household.id {
                let next = householdService.households.first
                try await userService.updateSelectedHousehold(next?.id)
                if let next {
                    FoodItemService.shared.startListening(householdID: next.id)
                }
            }
            isLoading = false
            return true
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
            return false
        }
    }

    func reset() {
        householdName = ""
        inviteEmail = ""
        errorMessage = nil
    }
}
