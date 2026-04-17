import Foundation
import FirebaseFirestore
import FirebaseAuth

@MainActor
class InvitationService: ObservableObject {
    static let shared = InvitationService()

    @Published var invitations: [Invitation] = []

    private let db = Firestore.firestore()
    private var listener: ListenerRegistration?

    private init() {}

    func startListening(invitationUIDs: [String]) {
        listener?.remove()
        guard !invitationUIDs.isEmpty else {
            invitations = []
            return
        }

        listener = db.collection("invitations")
            .whereField(FieldPath.documentID(), in: invitationUIDs)
            .addSnapshotListener { [weak self] snapshot, _ in
                guard let docs = snapshot?.documents else { return }
                Task { @MainActor in
                    self?.invitations = docs.map { Invitation.fromFirestore($0.data(), id: $0.documentID) }
                }
            }
    }

    func stopListening() {
        listener?.remove()
        listener = nil
        invitations = []
    }

    func sendInvitation(to email: String, householdID: String, householdName: String) async throws {
        guard let currentUID = Auth.auth().currentUser?.uid else { throw InvitationError.notAuthenticated }

        let targetUser = try await UserService.shared.getUserByEmail(email)
        guard let targetUser else { throw InvitationError.userNotFound }
        guard targetUser.id != currentUID else { throw InvitationError.cannotInviteSelf }
        guard !targetUser.householdUIDs.contains(householdID) else { throw InvitationError.alreadyMember }

        let invitationID = db.collection("invitations").document().documentID
        let inviterName = UserService.shared.currentSLUser?.username ?? "Someone"

        let invitation = Invitation(
            id: invitationID,
            householdId: householdID,
            householdName: householdName,
            invitedUserId: targetUser.id,
            inviterUserId: currentUID,
            inviterName: inviterName,
            timestamp: Date()
        )

        try await db.collection("invitations").document(invitationID).setData(invitation.firestoreData)
        try await UserService.shared.addInvitationUID(invitationID)

        var updatedTarget = targetUser
        updatedTarget.invitationUIDs.append(invitationID)
        try await db.collection("users").document(targetUser.id).updateData([
            "invitationUIDs": FieldValue.arrayUnion([invitationID])
        ])
    }

    func acceptInvitation(_ invitation: Invitation) async throws {
        guard let currentUID = Auth.auth().currentUser?.uid else { throw InvitationError.notAuthenticated }

        try await HouseholdService.shared.addMember(userUID: currentUID, to: invitation.householdId)
        try await UserService.shared.addHouseholdUID(invitation.householdId)
        try await UserService.shared.removeInvitationUID(invitation.id)
        try await db.collection("invitations").document(invitation.id).delete()

        if HouseholdService.shared.selectedHousehold == nil {
            try await UserService.shared.updateSelectedHousehold(invitation.householdId)
        }
    }

    func declineInvitation(_ invitation: Invitation) async throws {
        try await UserService.shared.removeInvitationUID(invitation.id)
        try await db.collection("invitations").document(invitation.id).delete()
    }

    enum InvitationError: LocalizedError {
        case notAuthenticated
        case userNotFound
        case cannotInviteSelf
        case alreadyMember

        var errorDescription: String? {
            switch self {
            case .notAuthenticated: return "You must be signed in to send invitations"
            case .userNotFound: return "No user found with that email address"
            case .cannotInviteSelf: return "You cannot invite yourself"
            case .alreadyMember: return "This user is already a member of the household"
            }
        }
    }
}
