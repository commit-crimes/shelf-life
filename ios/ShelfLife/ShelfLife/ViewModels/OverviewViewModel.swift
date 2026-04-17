import Foundation
import SwiftUI
import Combine

@MainActor
class OverviewViewModel: ObservableObject {
    @Published var searchText = ""
    @Published var selectedCategory: FoodCategory?
    @Published var selectedItems: Set<String> = []
    @Published var isMultiSelectMode = false
    @Published var isShowingAddItem = false
    @Published var isShowingScanner = false
    @Published var isShowingHouseholdDrawer = false
    @Published var isShowingHouseholdCreation = false
    @Published var isShowingLeaderboard = false
    @Published var selectedFoodItem: FoodItem?
    @Published var errorMessage: String?
    @Published var isLoading = false

    // Mirror service state so view re-renders on changes
    @Published var foodItems: [FoodItem] = []
    @Published var currentHousehold: Household?
    @Published var households: [Household] = []

    private let foodItemService = FoodItemService.shared
    private let householdService = HouseholdService.shared
    private let userService = UserService.shared
    private var cancellables = Set<AnyCancellable>()

    init() {
        // Bridge service @Published arrays into this ViewModel's @Published props
        foodItemService.$foodItems
            .receive(on: RunLoop.main)
            .sink { [weak self] items in self?.foodItems = items }
            .store(in: &cancellables)

        householdService.$selectedHousehold
            .receive(on: RunLoop.main)
            .sink { [weak self] hh in self?.currentHousehold = hh }
            .store(in: &cancellables)

        householdService.$households
            .receive(on: RunLoop.main)
            .sink { [weak self] hhs in self?.households = hhs }
            .store(in: &cancellables)
    }

    var filteredItems: [FoodItem] {
        foodItemService.filteredItems(category: selectedCategory, search: searchText)
    }

    var hasNoHouseholds: Bool { households.isEmpty }

    func selectCategory(_ category: FoodCategory?) {
        selectedCategory = selectedCategory == category ? nil : category
    }

    func toggleItemSelection(_ itemID: String) {
        if selectedItems.contains(itemID) {
            selectedItems.remove(itemID)
        } else {
            selectedItems.insert(itemID)
        }
    }

    func deleteSelectedItems() async {
        guard let householdID = currentHousehold?.id else { return }
        isLoading = true
        do {
            try await foodItemService.deleteFoodItems(itemIDs: Array(selectedItems), from: householdID)
            selectedItems.removeAll()
            isMultiSelectMode = false
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func deleteItem(_ item: FoodItem) async {
        guard let householdID = currentHousehold?.id else { return }
        isLoading = true
        do {
            try await foodItemService.deleteFoodItem(itemID: item.id, from: householdID)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func selectHousehold(_ household: Household) async {
        householdService.selectHousehold(household)
        FoodItemService.shared.startListening(householdID: household.id)
        do {
            try await userService.updateSelectedHousehold(household.id)
        } catch {
            errorMessage = error.localizedDescription
        }
    }
}
