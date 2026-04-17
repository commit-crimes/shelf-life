import Foundation
import FirebaseFirestore

@MainActor
class HouseholdService: ObservableObject {
    static let shared = HouseholdService()

    @Published var households: [Household] = []
    @Published var selectedHousehold: Household?

    private let db = Firestore.firestore()
    private var listeners: [String: ListenerRegistration] = [:]

    private init() {}

    func loadHouseholds(for user: SLUser) async throws {
        guard !user.householdUIDs.isEmpty else {
            households = []
            selectedHousehold = nil
            return
        }

        var loaded: [Household] = []
        for uid in user.householdUIDs {
            let doc = try await db.collection("households").document(uid).getDocument()
            if let data = doc.data() {
                loaded.append(Household.fromFirestore(data, id: doc.documentID))
            }
        }
        households = loaded

        if let selectedUID = user.selectedHouseholdUID,
           let selected = loaded.first(where: { $0.id == selectedUID }) {
            selectedHousehold = selected
        } else {
            selectedHousehold = loaded.first
        }

        for household in loaded {
            startListening(householdID: household.id)
        }
    }

    func startListening(householdID: String) {
        guard listeners[householdID] == nil else { return }
        let listener = db.collection("households").document(householdID).addSnapshotListener { [weak self] snapshot, _ in
            guard let data = snapshot?.data() else { return }
            let updated = Household.fromFirestore(data, id: householdID)
            Task { @MainActor in
                guard let self else { return }
                if let idx = self.households.firstIndex(where: { $0.id == householdID }) {
                    self.households[idx] = updated
                }
                if self.selectedHousehold?.id == householdID {
                    self.selectedHousehold = updated
                }
            }
        }
        listeners[householdID] = listener
    }

    func stopListening() {
        listeners.values.forEach { $0.remove() }
        listeners.removeAll()
    }

    func createHousehold(name: String, ownerUID: String) async throws -> Household {
        let id = db.collection("households").document().documentID
        let household = Household(id: id, name: name, members: [ownerUID])
        try await db.collection("households").document(id).setData(household.firestoreData)
        households.append(household)
        if selectedHousehold == nil { selectedHousehold = household }
        startListening(householdID: id)
        return household
    }

    func updateHousehold(_ household: Household) async throws {
        try await db.collection("households").document(household.id).setData(household.firestoreData)
    }

    func deleteHousehold(_ householdID: String) async throws {
        try await db.collection("households").document(householdID).delete()
        households.removeAll { $0.id == householdID }
        if selectedHousehold?.id == householdID {
            selectedHousehold = households.first
        }
        listeners[householdID]?.remove()
        listeners.removeValue(forKey: householdID)
    }

    func addMember(userUID: String, to householdID: String) async throws {
        try await db.collection("households").document(householdID).updateData([
            "members": FieldValue.arrayUnion([userUID])
        ])
    }

    func removeMember(userUID: String, from householdID: String) async throws {
        try await db.collection("households").document(householdID).updateData([
            "members": FieldValue.arrayRemove([userUID])
        ])
    }

    func addPoints(userUID: String, householdID: String, type: PointType, amount: Int) async throws {
        let field = type == .rat ? "ratPoints.\(userUID)" : "stinkyPoints.\(userUID)"
        try await db.collection("households").document(householdID).updateData([
            field: FieldValue.increment(Int64(amount))
        ])
    }

    func selectHousehold(_ household: Household) {
        selectedHousehold = household
    }

    enum PointType { case rat, stinky }
}
