import Foundation
import SwiftUI

enum GenerationStep: Int, CaseIterable {
    case input = 0
    case selectIngredients = 1
    case review = 2
    case complete = 3
}

@MainActor
class RecipeGenerationViewModel: ObservableObject {
    @Published var currentStep: GenerationStep = .input
    @Published var prompt = RecipePrompt()
    @Published var generatedRecipe: Recipe?
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var selectedFoodItems: Set<String> = []
    @Published var missingIngredientText = ""

    private let openAIService = OpenAIService.shared
    private let foodItemService = FoodItemService.shared

    var availableFoodItems: [FoodItem] {
        foodItemService.foodItems.filter { $0.status != .consumed && $0.status != .expired }
    }

    var selectedItems: [FoodItem] {
        availableFoodItems.filter { selectedFoodItems.contains($0.id) }
    }

    func nextStep() {
        switch currentStep {
        case .input:
            currentStep = .selectIngredients
        case .selectIngredients:
            Task { await generateRecipe() }
        case .review:
            currentStep = .complete
        case .complete:
            break
        }
    }

    func previousStep() {
        switch currentStep {
        case .input: break
        case .selectIngredients: currentStep = .input
        case .review: currentStep = .selectIngredients
        case .complete: currentStep = .review
        }
    }

    private func generateRecipe() async {
        isLoading = true
        errorMessage = nil

        var finalPrompt = prompt
        finalPrompt.ingredients = selectedItems
        if !missingIngredientText.isEmpty {
            finalPrompt.missingIngredients = missingIngredientText
                .components(separatedBy: ",")
                .map { $0.trimmingCharacters(in: .whitespaces) }
                .filter { !$0.isEmpty }
        }

        do {
            generatedRecipe = try await openAIService.generateRecipe(from: finalPrompt)
            currentStep = .review
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func toggleFoodItem(_ id: String) {
        if selectedFoodItems.contains(id) {
            selectedFoodItems.remove(id)
        } else {
            selectedFoodItems.insert(id)
        }
    }

    func reset() {
        currentStep = .input
        prompt = RecipePrompt()
        generatedRecipe = nil
        selectedFoodItems = []
        missingIngredientText = ""
        errorMessage = nil
    }
}
