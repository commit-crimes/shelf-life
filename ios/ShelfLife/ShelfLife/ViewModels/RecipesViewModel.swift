import Foundation

@MainActor
class RecipesViewModel: ObservableObject {
    @Published var searchText = ""
    @Published var selectedType: RecipeType?
    @Published var selectedRecipe: Recipe?
    @Published var isShowingAddRecipe = false
    @Published var isShowingGenerateRecipe = false
    @Published var isShowingExecution = false
    @Published var errorMessage: String?
    @Published var isLoading = false

    private let recipeService = RecipeService.shared
    private let userService = UserService.shared
    private let householdService = HouseholdService.shared

    var filteredRecipes: [Recipe] {
        recipeService.filteredRecipes(type: selectedType, search: searchText)
    }

    func selectType(_ type: RecipeType?) {
        selectedType = selectedType == type ? nil : type
    }

    func deleteRecipe(_ recipe: Recipe) async {
        isLoading = true
        do {
            try await recipeService.deleteRecipe(recipe.id)
            try await userService.removeRecipeUID(recipe.id)
            if let household = householdService.selectedHousehold,
               household.sharedRecipes.contains(recipe.id) {
                var updated = household
                updated.sharedRecipes.removeAll { $0 == recipe.id }
                try await HouseholdService.shared.updateHousehold(updated)
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func saveRecipe(_ recipe: Recipe) async -> Bool {
        isLoading = true
        do {
            try await recipeService.addRecipe(recipe)
            try await userService.addRecipeUID(recipe.id)
            isLoading = false
            return true
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
            return false
        }
    }

    func updateRecipe(_ recipe: Recipe) async -> Bool {
        isLoading = true
        do {
            try await recipeService.updateRecipe(recipe)
            isLoading = false
            return true
        } catch {
            errorMessage = error.localizedDescription
            isLoading = false
            return false
        }
    }
}
