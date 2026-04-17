import Foundation
import FirebaseFirestore
import FirebaseAuth

@MainActor
class UserService: ObservableObject {
    static let shared = UserService()

    @Published var currentSLUser: SLUser?

    private let db = Firestore.firestore()
    private var listener: ListenerRegistration?

    private init() {}

    func initializeUser(_ firebaseUser: FirebaseAuth.User) async throws {
        let uid = firebaseUser.uid
        let docRef = db.collection("users").document(uid)
        let snapshot = try await docRef.getDocument()

        if snapshot.exists, let data = snapshot.data() {
            currentSLUser = SLUser.fromFirestore(data, id: uid)
        } else {
            let newUser = SLUser(
                id: uid,
                username: firebaseUser.displayName ?? "User",
                email: firebaseUser.email ?? "",
                photoUrl: firebaseUser.photoURL?.absoluteString ?? ""
            )
            try await docRef.setData(newUser.firestoreData)
            currentSLUser = newUser
        }

        startListening(uid: uid)
    }

    func startListening(uid: String) {
        listener?.remove()
        listener = db.collection("users").document(uid).addSnapshotListener { [weak self] snapshot, _ in
            guard let data = snapshot?.data() else { return }
            Task { @MainActor in
                self?.currentSLUser = SLUser.fromFirestore(data, id: uid)
            }
        }
    }

    func stopListening() {
        listener?.remove()
        listener = nil
        currentSLUser = nil
    }

    func updateUsername(_ username: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData(["username": username])
    }

    func updateSelectedHousehold(_ householdUID: String?) async throws {
        guard let uid = currentSLUser?.id else { return }
        if let householdUID {
            try await db.collection("users").document(uid).updateData(["selectedHouseholdUID": householdUID])
        } else {
            try await db.collection("users").document(uid).updateData(["selectedHouseholdUID": FieldValue.delete()])
        }
    }

    func addHouseholdUID(_ householdUID: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData([
            "householdUIDs": FieldValue.arrayUnion([householdUID])
        ])
    }

    func removeHouseholdUID(_ householdUID: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData([
            "householdUIDs": FieldValue.arrayRemove([householdUID])
        ])
    }

    func addRecipeUID(_ recipeUID: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData([
            "recipeUIDs": FieldValue.arrayUnion([recipeUID])
        ])
    }

    func removeRecipeUID(_ recipeUID: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData([
            "recipeUIDs": FieldValue.arrayRemove([recipeUID])
        ])
    }

    func addInvitationUID(_ invitationUID: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData([
            "invitationUIDs": FieldValue.arrayUnion([invitationUID])
        ])
    }

    func removeInvitationUID(_ invitationUID: String) async throws {
        guard let uid = currentSLUser?.id else { return }
        try await db.collection("users").document(uid).updateData([
            "invitationUIDs": FieldValue.arrayRemove([invitationUID])
        ])
    }

    func getUserByEmail(_ email: String) async throws -> SLUser? {
        let snapshot = try await db.collection("users").whereField("email", isEqualTo: email).getDocuments()
        guard let doc = snapshot.documents.first else { return nil }
        return SLUser.fromFirestore(doc.data(), id: doc.documentID)
    }

    func getUsers(byUIDs uids: [String]) async throws -> [SLUser] {
        guard !uids.isEmpty else { return [] }
        var users: [SLUser] = []
        for uid in uids {
            let doc = try await db.collection("users").document(uid).getDocument()
            if let data = doc.data() {
                users.append(SLUser.fromFirestore(data, id: doc.documentID))
            }
        }
        return users
    }
}
