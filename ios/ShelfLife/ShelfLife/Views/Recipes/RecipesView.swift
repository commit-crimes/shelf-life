import SwiftUI

struct RecipesView: View {
    @EnvironmentObject var vm: RecipesViewModel
    @ObservedObject private var recipeService = RecipeService.shared
    @State private var selectedRecipe: Recipe?
    @State private var showingExecution = false

    var body: some View {
        NavigationStack {
            ZStack(alignment: .bottomTrailing) {
                VStack(spacing: 0) {
                    searchAndFilter

                    if vm.filteredRecipes.isEmpty {
                        emptyState
                    } else {
                        recipeList
                    }
                }

                fabGroup
            }
            .navigationTitle("Recipes")
            .sheet(isPresented: $vm.isShowingAddRecipe) {
                AddRecipeView()
                    .environmentObject(vm)
            }
            .sheet(isPresented: $vm.isShowingGenerateRecipe) {
                GenerateRecipeView()
                    .environmentObject(vm)
            }
            .sheet(item: $selectedRecipe) { recipe in
                RecipeDetailView(recipe: recipe)
                    .environmentObject(vm)
            }
        }
    }

    private var searchAndFilter: some View {
        VStack(spacing: 8) {
            HStack {
                Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
                TextField("Search recipes...", text: $vm.searchText)
                if !vm.searchText.isEmpty {
                    Button { vm.searchText = "" } label: {
                        Image(systemName: "xmark.circle.fill").foregroundStyle(.secondary)
                    }
                }
            }
            .padding(10)
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(RecipeType.allCases, id: \.self) { type in
                        Button {
                            vm.selectType(type)
                        } label: {
                            Text(type.displayName)
                                .font(.caption.bold())
                                .padding(.horizontal, 12)
                                .padding(.vertical, 6)
                                .background(vm.selectedType == type ? Color.green : Color(.systemGray6))
                                .foregroundStyle(vm.selectedType == type ? .white : .primary)
                                .clipShape(Capsule())
                        }
                        .buttonStyle(.plain)
                    }
                }
                .padding(.horizontal)
            }
        }
        .padding(.vertical, 8)
    }

    private var recipeList: some View {
        List {
            ForEach(vm.filteredRecipes) { recipe in
                RecipeRow(recipe: recipe)
                    .onTapGesture { selectedRecipe = recipe }
                    .swipeActions(edge: .trailing) {
                        Button(role: .destructive) {
                            Task { await vm.deleteRecipe(recipe) }
                        } label: {
                            Label("Delete", systemImage: "trash")
                        }
                    }
            }
        }
        .listStyle(.plain)
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "book.closed").font(.system(size: 60)).foregroundStyle(.secondary)
            Text("No recipes yet").font(.headline)
            Text("Add recipes manually or generate them with AI\nusing your pantry ingredients.")
                .font(.subheadline).foregroundStyle(.secondary).multilineTextAlignment(.center)
            Spacer()
        }
        .padding()
    }

    private var fabGroup: some View {
        VStack(spacing: 12) {
            Button {
                vm.isShowingGenerateRecipe = true
            } label: {
                HStack(spacing: 6) {
                    Image(systemName: "sparkles")
                    Text("Generate")
                }
                .padding(.horizontal, 16)
                .padding(.vertical, 12)
                .background(.purple)
                .foregroundStyle(.white)
                .clipShape(Capsule())
                .shadow(radius: 4)
            }

            Button {
                vm.isShowingAddRecipe = true
            } label: {
                Image(systemName: "plus")
                    .frame(width: 56, height: 56)
                    .background(.green)
                    .foregroundStyle(.white)
                    .clipShape(Circle())
                    .shadow(radius: 4)
            }
        }
        .padding(.trailing, 20)
        .padding(.bottom, 20)
    }
}

struct RecipeRow: View {
    let recipe: Recipe

    var body: some View {
        HStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 10)
                    .fill(recipeTypeColor(recipe.recipeType).opacity(0.15))
                    .frame(width: 52, height: 52)
                Image(systemName: recipeTypeIcon(recipe.recipeType))
                    .foregroundStyle(recipeTypeColor(recipe.recipeType))
                    .font(.title2)
            }

            VStack(alignment: .leading, spacing: 4) {
                Text(recipe.name).font(.subheadline.bold()).lineLimit(1)

                HStack(spacing: 12) {
                    Label("\(recipe.timeFormatted)", systemImage: "clock")
                    Label("\(Int(recipe.servings)) servings", systemImage: "person.2")
                }
                .font(.caption)
                .foregroundStyle(.secondary)

                Text(recipe.recipeType.displayName)
                    .font(.caption)
                    .padding(.horizontal, 8)
                    .padding(.vertical, 2)
                    .background(recipeTypeColor(recipe.recipeType).opacity(0.15))
                    .foregroundStyle(recipeTypeColor(recipe.recipeType))
                    .clipShape(Capsule())
            }

            Spacer()
        }
        .padding(.vertical, 4)
    }

    private func recipeTypeColor(_ type: RecipeType) -> Color {
        switch type {
        case .basic: return .blue
        case .highProtein: return .red
        case .lowCalorie: return .green
        case .personal: return .purple
        }
    }

    private func recipeTypeIcon(_ type: RecipeType) -> String {
        switch type {
        case .basic: return "fork.knife"
        case .highProtein: return "bolt.fill"
        case .lowCalorie: return "leaf.fill"
        case .personal: return "star.fill"
        }
    }
}
