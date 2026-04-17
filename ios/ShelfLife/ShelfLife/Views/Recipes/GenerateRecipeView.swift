import SwiftUI

struct GenerateRecipeView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var recipesVM: RecipesViewModel
    @StateObject private var vm = RecipeGenerationViewModel()

    var body: some View {
        NavigationStack {
            Group {
                switch vm.currentStep {
                case .input: inputStep
                case .selectIngredients: selectIngredientsStep
                case .review: reviewStep
                case .complete: completeStep
                }
            }
            .navigationTitle(stepTitle)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
            }
            .overlay {
                if vm.isLoading {
                    ZStack {
                        Color.black.opacity(0.3).ignoresSafeArea()
                        VStack(spacing: 16) {
                            ProgressView()
                            Text("Generating recipe with AI...")
                                .font(.subheadline)
                                .foregroundStyle(.white)
                        }
                        .padding(24)
                        .background(.ultraThinMaterial)
                        .clipShape(RoundedRectangle(cornerRadius: 16))
                    }
                }
            }
        }
    }

    private var stepTitle: String {
        switch vm.currentStep {
        case .input: return "Recipe Preferences"
        case .selectIngredients: return "Select Ingredients"
        case .review: return "Review Recipe"
        case .complete: return "Recipe Generated!"
        }
    }

    private var inputStep: some View {
        Form {
            Section("Recipe Details") {
                TextField("Recipe name (optional)", text: $vm.prompt.name).textInputAutocapitalization(.words)

                Picker("Type", selection: $vm.prompt.recipeType) {
                    ForEach(RecipeType.allCases, id: \.self) { t in
                        Text(t.displayName).tag(t)
                    }
                }

                Stepper("Servings: \(vm.prompt.servings)", value: $vm.prompt.servings, in: 1...20)

                Toggle("Quick Recipe (< 15 min)", isOn: $vm.prompt.shortDuration)

                TextField("Special instructions...", text: $vm.prompt.specialInstructions, axis: .vertical)
                    .lineLimit(3...5)
            }

            Section("Ingredient Preferences") {
                Toggle("Only use pantry items", isOn: $vm.prompt.onlyHouseholdItems)
                Toggle("Prioritize soon-to-expire items", isOn: $vm.prompt.prioritiseSoonToExpire)
            }

            if let error = vm.errorMessage {
                Section { Text(error).foregroundStyle(.red).font(.caption) }
            }

            Section {
                Button("Next: Select Ingredients") {
                    vm.nextStep()
                }
                .frame(maxWidth: .infinity)
                .foregroundStyle(.white)
                .listRowBackground(Color.purple)
            }
        }
    }

    private var selectIngredientsStep: some View {
        VStack(spacing: 0) {
            List {
                Section("Your Pantry") {
                    ForEach(vm.availableFoodItems) { item in
                        HStack {
                            Image(systemName: vm.selectedFoodItems.contains(item.id)
                                  ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(vm.selectedFoodItems.contains(item.id) ? .green : .secondary)

                            Text(item.foodFacts.category.icon)

                            VStack(alignment: .leading) {
                                Text(item.foodFacts.name).font(.subheadline)
                                if let days = item.daysUntilExpiry {
                                    Text(days < 0 ? "Expired" : "Expires in \(days)d")
                                        .font(.caption)
                                        .foregroundStyle(days <= 3 ? .orange : .secondary)
                                }
                            }

                            Spacer()
                        }
                        .contentShape(Rectangle())
                        .onTapGesture { vm.toggleFoodItem(item.id) }
                    }
                }

                Section("Missing Ingredients (optional)") {
                    TextField("e.g. olive oil, garlic, salt", text: $vm.missingIngredientText)
                    Text("Comma-separated list of additional ingredients you're willing to buy")
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }
            }

            HStack {
                Button("Back") { vm.previousStep() }
                    .foregroundStyle(.secondary)
                    .frame(maxWidth: .infinity)

                Button("Generate Recipe ✨") {
                    vm.nextStep()
                }
                .buttonStyle(.borderedProminent)
                .tint(.purple)
                .frame(maxWidth: .infinity)
            }
            .padding()
            .background(Color(.systemBackground))
        }
    }

    private var reviewStep: some View {
        ScrollView {
            if let recipe = vm.generatedRecipe {
                VStack(alignment: .leading, spacing: 20) {
                    VStack(alignment: .leading, spacing: 8) {
                        Text(recipe.name).font(.title2.bold())
                        HStack(spacing: 16) {
                            Label(recipe.timeFormatted, systemImage: "clock")
                            Label("\(Int(recipe.servings)) servings", systemImage: "person.2")
                        }
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                    }

                    Divider()

                    VStack(alignment: .leading, spacing: 8) {
                        Text("Ingredients").font(.headline)
                        ForEach(recipe.ingredients) { ing in
                            HStack {
                                Circle().fill(.green).frame(width: 6, height: 6)
                                Text(ing.name)
                                Spacer()
                                Text(ing.quantity.displayString).foregroundStyle(.secondary)
                            }
                            .font(.subheadline)
                        }
                    }

                    Divider()

                    VStack(alignment: .leading, spacing: 10) {
                        Text("Instructions").font(.headline)
                        ForEach(Array(recipe.instructions.enumerated()), id: \.offset) { idx, step in
                            HStack(alignment: .top, spacing: 12) {
                                Text("\(idx + 1)")
                                    .font(.caption.bold())
                                    .frame(width: 24, height: 24)
                                    .background(.green)
                                    .foregroundStyle(.white)
                                    .clipShape(Circle())
                                Text(step).font(.subheadline).fixedSize(horizontal: false, vertical: true)
                                Spacer()
                            }
                        }
                    }

                    HStack {
                        Button("Regenerate") {
                            vm.previousStep()
                        }
                        .foregroundStyle(.secondary)
                        .frame(maxWidth: .infinity)

                        Button("Save Recipe") {
                            Task {
                                if await recipesVM.saveRecipe(recipe) {
                                    vm.currentStep = .complete
                                }
                            }
                        }
                        .buttonStyle(.borderedProminent)
                        .tint(.green)
                        .frame(maxWidth: .infinity)
                    }
                    .padding(.top)
                }
                .padding()
            }
        }
    }

    private var completeStep: some View {
        VStack(spacing: 24) {
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 80))
                .foregroundStyle(.green)

            Text("Recipe Saved!")
                .font(.title.bold())

            Text("Your recipe has been added to your collection.")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Button("Done") { dismiss() }
                .buttonStyle(.borderedProminent)
                .tint(.green)

            Spacer()
        }
        .padding()
    }
}
