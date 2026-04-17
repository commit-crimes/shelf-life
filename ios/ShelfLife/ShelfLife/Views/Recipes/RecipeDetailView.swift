import SwiftUI

struct RecipeDetailView: View {
    let recipe: Recipe
    @EnvironmentObject var recipesVM: RecipesViewModel
    @Environment(\.dismiss) private var dismiss
    @State private var showingExecution = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(alignment: .leading, spacing: 20) {
                    headerSection
                    ingredientsSection
                    instructionsSection
                }
                .padding()
            }
            .navigationTitle(recipe.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        showingExecution = true
                    } label: {
                        Label("Cook", systemImage: "flame.fill")
                    }
                    .buttonStyle(.borderedProminent)
                    .tint(.orange)
                }
            }
            .fullScreenCover(isPresented: $showingExecution) {
                RecipeExecutionView(recipe: recipe)
            }
        }
    }

    private var headerSection: some View {
        VStack(spacing: 12) {
            HStack(spacing: 20) {
                statBubble(icon: "clock", text: recipe.timeFormatted)
                statBubble(icon: "person.2", text: "\(Int(recipe.servings)) servings")
                statBubble(icon: "flame", text: "\(Int(recipe.totalCalories)) kcal")
            }

            Text(recipe.recipeType.displayName)
                .font(.caption.bold())
                .padding(.horizontal, 12)
                .padding(.vertical, 6)
                .background(Color.green.opacity(0.15))
                .foregroundStyle(.green)
                .clipShape(Capsule())
        }
        .frame(maxWidth: .infinity)
    }

    private func statBubble(icon: String, text: String) -> some View {
        VStack(spacing: 4) {
            Image(systemName: icon).foregroundStyle(.green)
            Text(text).font(.caption.bold())
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var ingredientsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Ingredients").font(.title3.bold())
            ForEach(recipe.ingredients) { ingredient in
                HStack {
                    Circle().fill(.green).frame(width: 8, height: 8)
                    Text(ingredient.name)
                    Spacer()
                    Text(ingredient.quantity.displayString).foregroundStyle(.secondary)
                }
                .font(.subheadline)
                Divider()
            }
        }
    }

    private var instructionsSection: some View {
        VStack(alignment: .leading, spacing: 12) {
            Text("Instructions").font(.title3.bold())
            ForEach(Array(recipe.instructions.enumerated()), id: \.offset) { idx, instruction in
                HStack(alignment: .top, spacing: 12) {
                    Text("\(idx + 1)")
                        .font(.subheadline.bold())
                        .frame(width: 28, height: 28)
                        .background(.green)
                        .foregroundStyle(.white)
                        .clipShape(Circle())

                    Text(instruction)
                        .font(.subheadline)
                        .fixedSize(horizontal: false, vertical: true)

                    Spacer()
                }
                .padding(.vertical, 4)
            }
        }
    }
}
