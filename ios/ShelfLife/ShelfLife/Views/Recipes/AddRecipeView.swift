import SwiftUI

struct AddRecipeView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var recipesVM: RecipesViewModel

    @State private var name = ""
    @State private var recipeType = RecipeType.basic
    @State private var servings: Float = 2
    @State private var timeMinutes: Double = 30
    @State private var instructions: [String] = [""]
    @State private var ingredients: [Ingredient] = []
    @State private var isLoading = false
    @State private var errorMessage: String?
    @State private var showingAddIngredient = false
    @State private var newIngredientName = ""
    @State private var newIngredientAmount: Double = 100
    @State private var newIngredientUnit = FoodUnit.gram

    var body: some View {
        NavigationStack {
            Form {
                Section("Recipe Info") {
                    TextField("Recipe name *", text: $name).textInputAutocapitalization(.words)

                    Picker("Type", selection: $recipeType) {
                        ForEach(RecipeType.allCases, id: \.self) { t in
                            Text(t.displayName).tag(t)
                        }
                    }

                    Stepper("Servings: \(Int(servings))", value: $servings, in: 1...20)

                    VStack(alignment: .leading) {
                        Text("Cooking time: \(Int(timeMinutes)) min")
                        Slider(value: $timeMinutes, in: 5...240, step: 5)
                    }
                }

                Section("Ingredients") {
                    ForEach(ingredients) { ing in
                        HStack {
                            Text(ing.name)
                            Spacer()
                            Text(ing.quantity.displayString).foregroundStyle(.secondary)
                        }
                    }
                    .onDelete { indices in
                        ingredients.remove(atOffsets: indices)
                    }

                    Button {
                        showingAddIngredient = true
                    } label: {
                        Label("Add Ingredient", systemImage: "plus")
                    }
                }

                Section("Instructions") {
                    ForEach(instructions.indices, id: \.self) { idx in
                        HStack(alignment: .top) {
                            Text("\(idx + 1).")
                                .foregroundStyle(.secondary)
                                .frame(width: 24)
                            TextField("Step \(idx + 1)", text: $instructions[idx], axis: .vertical)
                                .lineLimit(3...6)
                        }
                    }
                    .onDelete { indices in
                        instructions.remove(atOffsets: indices)
                    }

                    Button {
                        instructions.append("")
                    } label: {
                        Label("Add Step", systemImage: "plus")
                    }
                }

                if let error = errorMessage {
                    Section { Text(error).foregroundStyle(.red).font(.caption) }
                }
            }
            .navigationTitle("New Recipe")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Save") {
                        Task {
                            let filteredInstructions = instructions.filter { !$0.trimmingCharacters(in: .whitespaces).isEmpty }
                            let recipe = Recipe(
                                name: name,
                                instructions: filteredInstructions,
                                servings: servings,
                                time: timeMinutes * 60,
                                ingredients: ingredients,
                                recipeType: recipeType
                            )
                            if await recipesVM.saveRecipe(recipe) { dismiss() }
                        }
                    }
                    .disabled(name.isEmpty || isLoading)
                }
            }
            .sheet(isPresented: $showingAddIngredient) {
                addIngredientSheet
            }
        }
    }

    private var addIngredientSheet: some View {
        NavigationStack {
            Form {
                TextField("Ingredient name", text: $newIngredientName).textInputAutocapitalization(.words)
                HStack {
                    TextField("Amount", value: $newIngredientAmount, format: .number).keyboardType(.decimalPad)
                    Picker("Unit", selection: $newIngredientUnit) {
                        ForEach(FoodUnit.allCases, id: \.self) { u in Text(u.displayName).tag(u) }
                    }
                    .pickerStyle(.segmented)
                }
            }
            .navigationTitle("Add Ingredient")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { showingAddIngredient = false }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button("Add") {
                        ingredients.append(Ingredient(
                            name: newIngredientName,
                            quantity: Quantity(amount: newIngredientAmount, unit: newIngredientUnit)
                        ))
                        newIngredientName = ""
                        newIngredientAmount = 100
                        newIngredientUnit = .gram
                        showingAddIngredient = false
                    }
                    .disabled(newIngredientName.isEmpty)
                }
            }
        }
        .presentationDetents([.medium])
    }
}
