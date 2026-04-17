import Foundation
import FirebaseFirestore

struct Invitation: Identifiable, Codable, Equatable {
    var id: String
    var householdId: String
    var householdName: String
    var invitedUserId: String
    var inviterUserId: String
    var inviterName: String
    var timestamp: Date?

    init(id: String = UUID().uuidString, householdId: String = "", householdName: String = "",
         invitedUserId: String = "", inviterUserId: String = "", inviterName: String = "",
         timestamp: Date? = nil) {
        self.id = id
        self.householdId = householdId
        self.householdName = householdName
        self.invitedUserId = invitedUserId
        self.inviterUserId = inviterUserId
        self.inviterName = inviterName
        self.timestamp = timestamp
    }

    var firestoreData: [String: Any] {
        var data: [String: Any] = [
            "invitationId": id,
            "householdId": householdId,
            "householdName": householdName,
            "invitedUserId": invitedUserId,
            "inviterUserId": inviterUserId,
            "inviterName": inviterName
        ]
        if let timestamp {
            data["timestamp"] = Timestamp(date: timestamp)
        }
        return data
    }

    static func fromFirestore(_ data: [String: Any], id: String) -> Invitation {
        Invitation(
            id: id,
            householdId: data["householdId"] as? String ?? "",
            householdName: data["householdName"] as? String ?? "",
            invitedUserId: data["invitedUserId"] as? String ?? "",
            inviterUserId: data["inviterUserId"] as? String ?? "",
            inviterName: data["inviterName"] as? String ?? "",
            timestamp: (data["timestamp"] as? Timestamp)?.dateValue()
        )
    }
}
