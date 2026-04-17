import Foundation
import FirebaseFirestore

@MainActor
class RecipeService: ObservableObject {
    static let shared = RecipeService()

    @Published var recipes: [Recipe] = []

    private let db = Firestore.firestore()
    private var listener: ListenerRegistration?

    private init() {}

    func startListening(recipeUIDs: [String]) {
        listener?.remove()
        guard !recipeUIDs.isEmpty else {
            recipes = []
            return
        }

        listener = db.collection("recipes")
            .whereField(FieldPath.documentID(), in: recipeUIDs)
            .addSnapshotListener { [weak self] snapshot, _ in
                guard let docs = snapshot?.documents else { return }
                Task { @MainActor in
                    self?.recipes = docs.map { Recipe.fromFirestore($0.data(), id: $0.documentID) }
                }
            }
    }

    func stopListening() {
        listener?.remove()
        listener = nil
        recipes = []
    }

    func addRecipe(_ recipe: Recipe) async throws {
        try await db.collection("recipes").document(recipe.id).setData(recipe.firestoreData)
    }

    func updateRecipe(_ recipe: Recipe) async throws {
        try await db.collection("recipes").document(recipe.id).setData(recipe.firestoreData)
    }

    func deleteRecipe(_ recipeID: String) async throws {
        try await db.collection("recipes").document(recipeID).delete()
    }

    func fetchRecipes(byUIDs uids: [String]) async throws -> [Recipe] {
        guard !uids.isEmpty else { return [] }
        var results: [Recipe] = []
        let chunks = stride(from: 0, to: uids.count, by: 10).map { Array(uids[$0..<min($0 + 10, uids.count)]) }
        for chunk in chunks {
            let snapshot = try await db.collection("recipes")
                .whereField(FieldPath.documentID(), in: chunk)
                .getDocuments()
            results += snapshot.documents.map { Recipe.fromFirestore($0.data(), id: $0.documentID) }
        }
        return results
    }

    func filteredRecipes(type: RecipeType? = nil, search: String = "") -> [Recipe] {
        recipes.filter { recipe in
            let matchesType = type == nil || recipe.recipeType == type
            let matchesSearch = search.isEmpty || recipe.name.localizedCaseInsensitiveContains(search)
            return matchesType && matchesSearch
        }
    }
}
