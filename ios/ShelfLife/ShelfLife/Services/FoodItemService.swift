import Foundation
import FirebaseFirestore
import FirebaseStorage

@MainActor
class FoodItemService: ObservableObject {
    static let shared = FoodItemService()

    @Published var foodItems: [FoodItem] = []

    private let db = Firestore.firestore()
    private let storage = Storage.storage()
    private var listener: ListenerRegistration?
    private var currentHouseholdID: String?

    private init() {}

    func startListening(householdID: String) {
        guard householdID != currentHouseholdID else { return }
        listener?.remove()
        currentHouseholdID = householdID

        listener = db.collection("foodItems").document(householdID).collection("items")
            .addSnapshotListener { [weak self] snapshot, error in
                guard let docs = snapshot?.documents else { return }
                Task { @MainActor in
                    self?.foodItems = docs.compactMap { doc in
                        FoodItem.fromFirestore(doc.data(), id: doc.documentID)
                    }
                }
            }
    }

    func stopListening() {
        listener?.remove()
        listener = nil
        currentHouseholdID = nil
        foodItems = []
    }

    func addFoodItem(_ item: FoodItem, to householdID: String) async throws {
        let docRef = db.collection("foodItems").document(householdID).collection("items").document(item.id)
        try await docRef.setData(item.firestoreData)
    }

    func updateFoodItem(_ item: FoodItem, in householdID: String) async throws {
        let docRef = db.collection("foodItems").document(householdID).collection("items").document(item.id)
        try await docRef.setData(item.firestoreData)
    }

    func deleteFoodItem(itemID: String, from householdID: String) async throws {
        try await db.collection("foodItems").document(householdID).collection("items").document(itemID).delete()
    }

    func deleteFoodItems(itemIDs: [String], from householdID: String) async throws {
        let batch = db.batch()
        for itemID in itemIDs {
            let ref = db.collection("foodItems").document(householdID).collection("items").document(itemID)
            batch.deleteDocument(ref)
        }
        try await batch.commit()
    }

    func uploadImage(_ imageData: Data, itemID: String) async throws -> String {
        let ref = storage.reference().child("foodItems/\(itemID).jpg")
        let metadata = StorageMetadata()
        metadata.contentType = "image/jpeg"
        _ = try await ref.putDataAsync(imageData, metadata: metadata)
        let url = try await ref.downloadURL()
        return url.absoluteString
    }

    func filteredItems(category: FoodCategory? = nil, search: String = "", status: FoodStatus? = nil) -> [FoodItem] {
        foodItems.filter { item in
            let matchesCategory = category == nil || item.foodFacts.category == category
            let matchesSearch = search.isEmpty || item.foodFacts.name.localizedCaseInsensitiveContains(search)
            let matchesStatus = status == nil || item.status == status
            return matchesCategory && matchesSearch && matchesStatus
        }
        .sorted { lhs, rhs in
            switch (lhs.expiryDate, rhs.expiryDate) {
            case (let a?, let b?): return a < b
            case (nil, _?): return false
            case (_?, nil): return true
            case (nil, nil): return lhs.foodFacts.name < rhs.foodFacts.name
            }
        }
    }
}
