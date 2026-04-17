import Foundation

@MainActor
class RecipeExecutionViewModel: ObservableObject {
    @Published var currentStep = 0
    @Published var servings: Float = 2
    @Published var selectedIngredientItems: [String: FoodItem] = [:]
    @Published var isComplete = false
    @Published var errorMessage: String?

    private(set) var recipe: Recipe

    init(recipe: Recipe) {
        self.recipe = recipe
        self.servings = recipe.servings
    }

    var totalSteps: Int { recipe.instructions.count }

    var currentInstruction: String {
        guard currentStep < recipe.instructions.count else { return "" }
        return recipe.instructions[currentStep]
    }

    var progress: Float {
        guard totalSteps > 0 else { return 0 }
        return Float(currentStep + 1) / Float(totalSteps)
    }

    var scaledIngredients: [Ingredient] {
        let factor = Double(servings) / Double(recipe.servings)
        return recipe.ingredients.map { ing in
            var scaled = ing
            scaled.quantity.amount *= factor
            return scaled
        }
    }

    func nextStep() {
        if currentStep < recipe.instructions.count - 1 {
            currentStep += 1
        } else {
            isComplete = true
        }
    }

    func previousStep() {
        if currentStep > 0 {
            currentStep -= 1
        }
    }

    func availableItemsForIngredient(_ ingredient: Ingredient) -> [FoodItem] {
        FoodItemService.shared.foodItems.filter { item in
            item.foodFacts.name.localizedCaseInsensitiveContains(ingredient.name) &&
            item.status != .consumed && item.status != .expired
        }
    }

    func selectItem(_ item: FoodItem, for ingredient: Ingredient) {
        selectedIngredientItems[ingredient.id] = item
    }
}
