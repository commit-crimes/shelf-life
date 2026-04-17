import Foundation
import SwiftUI

@MainActor
class FoodItemViewModel: ObservableObject {
    @Published var foodFacts = FoodFacts()
    @Published var buyDate: Date = Date()
    @Published var hasBuyDate = false
    @Published var expiryDate: Date = Calendar.current.date(byAdding: .month, value: 1, to: Date()) ?? Date()
    @Published var hasExpiryDate = true
    @Published var openDate: Date = Date()
    @Published var hasOpenDate = false
    @Published var location: FoodStorageLocation = .pantry
    @Published var status: FoodStatus = .unopened
    @Published var isLoading = false
    @Published var errorMessage: String?

    private let foodItemService = FoodItemService.shared
    private let householdService = HouseholdService.shared
    private let userService = UserService.shared

    func populate(from item: FoodItem) {
        foodFacts = item.foodFacts
        if let d = item.buyDate { buyDate = d; hasBuyDate = true }
        if let d = item.expiryDate { expiryDate = d; hasExpiryDate = true }
        if let d = item.openDate { openDate = d; hasOpenDate = true }
        location = item.location
        status = item.status
    }

    func populate(from facts: FoodFacts) {
        foodFacts = facts
    }

    func save(existingID: String? = nil) async -> Bool {
        guard let householdID = householdService.selectedHousehold?.id,
              let ownerUID = userService.currentSLUser?.id else { return false }

        isLoading = true
        errorMessage = nil

        let item = FoodItem(
            id: existingID ?? UUID().uuidString,
            foodFacts: foodFacts,
            buyDate: hasBuyDate ? buyDate : nil,
            expiryDate: hasExpiryDate ? expiryDate : nil,
            openDate: hasOpenDate ? openDate : nil,
            location: location,
            status: status,
            owner: ownerUID
        )

        do {
            if existingID != nil {
                try await foodItemService.updateFoodItem(item, in: householdID)
            } else {
                try await foodItemService.addFoodItem(item, to: householdID)
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
        foodFacts = FoodFacts()
        buyDate = Date()
        hasBuyDate = false
        expiryDate = Calendar.current.date(byAdding: .month, value: 1, to: Date()) ?? Date()
        hasExpiryDate = true
        openDate = Date()
        hasOpenDate = false
        location = .pantry
        status = .unopened
        errorMessage = nil
    }
}
