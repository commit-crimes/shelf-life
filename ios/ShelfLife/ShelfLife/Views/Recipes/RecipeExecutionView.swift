import SwiftUI

struct RecipeExecutionView: View {
    let recipe: Recipe
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm: RecipeExecutionViewModel

    init(recipe: Recipe) {
        self.recipe = recipe
        _vm = StateObject(wrappedValue: RecipeExecutionViewModel(recipe: recipe))
    }

    var body: some View {
        NavigationStack {
            if vm.isComplete {
                completionView
            } else {
                executionView
            }
        }
    }

    private var executionView: some View {
        VStack(spacing: 0) {
            progressBar
            ScrollView {
                VStack(spacing: 24) {
                    servingsSection
                    if vm.currentStep == 0 { ingredientsSection }
                    currentStepSection
                }
                .padding()
            }
            navigationButtons
        }
        .navigationTitle(recipe.name)
        .navigationBarTitleDisplayMode(.inline)
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Exit") { dismiss() }
            }
        }
    }

    private var progressBar: some View {
        VStack(spacing: 4) {
            ProgressView(value: vm.progress)
                .tint(.green)
                .padding(.horizontal)

            Text("Step \(vm.currentStep + 1) of \(vm.totalSteps)")
                .font(.caption)
                .foregroundStyle(.secondary)
        }
        .padding(.vertical, 8)
        .background(Color(.systemBackground))
    }

    private var servingsSection: some View {
        HStack {
            Text("Servings")
            Spacer()
            Stepper("\(Int(vm.servings))", value: $vm.servings, in: 1...20)
        }
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Ingredients").font(.headline)
            ForEach(vm.scaledIngredients) { ingredient in
                HStack {
                    Text(ingredient.name)
                    Spacer()
                    Text(ingredient.quantity.displayString).foregroundStyle(.secondary)

                    let available = vm.availableItemsForIngredient(ingredient)
                    if !available.isEmpty {
                        Menu {
                            ForEach(available) { item in
                                Button(item.foodFacts.name) {
                                    vm.selectItem(item, for: ingredient)
                                }
                            }
                        } label: {
                            Image(systemName: vm.selectedIngredientItems[ingredient.id] != nil
                                  ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(vm.selectedIngredientItems[ingredient.id] != nil ? .green : .secondary)
                        }
                    }
                }
                .font(.subheadline)
                Divider()
            }
        }
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var currentStepSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            HStack {
                Text("Step \(vm.currentStep + 1)")
                    .font(.headline)
                    .foregroundStyle(.green)
                Spacer()
            }
            Text(vm.currentInstruction)
                .font(.body)
                .fixedSize(horizontal: false, vertical: true)
                .padding(.vertical, 8)
        }
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var navigationButtons: some View {
        HStack(spacing: 12) {
            Button {
                vm.previousStep()
            } label: {
                Label("Back", systemImage: "chevron.left")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(Color(.systemGray6))
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .disabled(vm.currentStep == 0)

            Button {
                vm.nextStep()
            } label: {
                Label(vm.currentStep == vm.totalSteps - 1 ? "Finish" : "Next",
                      systemImage: vm.currentStep == vm.totalSteps - 1 ? "checkmark" : "chevron.right")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(vm.currentStep == vm.totalSteps - 1 ? Color.green : Color.blue)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
        }
        .padding()
        .background(Color(.systemBackground))
    }

    private var completionView: some View {
        VStack(spacing: 24) {
            Spacer()
            Image(systemName: "checkmark.circle.fill")
                .font(.system(size: 80))
                .foregroundStyle(.green)

            Text("Bon Appétit!")
                .font(.largeTitle.bold())

            Text("You've completed \(recipe.name)!\nEnjoy your meal.")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)

            Button("Done") { dismiss() }
                .buttonStyle(.borderedProminent)
                .tint(.green)
                .frame(minWidth: 160)

            Spacer()
        }
        .padding()
        .navigationTitle("Done!")
        .toolbar {
            ToolbarItem(placement: .cancellationAction) {
                Button("Close") { dismiss() }
            }
        }
    }
}
