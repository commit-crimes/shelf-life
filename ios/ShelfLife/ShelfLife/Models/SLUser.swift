import Foundation

struct SLUser: Identifiable, Codable, Equatable {
    var id: String
    var username: String
    var email: String
    var photoUrl: String
    var selectedHouseholdUID: String?
    var householdUIDs: [String]
    var recipeUIDs: [String]
    var invitationUIDs: [String]

    init(id: String = "", username: String = "", email: String = "", photoUrl: String = "",
         selectedHouseholdUID: String? = nil, householdUIDs: [String] = [],
         recipeUIDs: [String] = [], invitationUIDs: [String] = []) {
        self.id = id
        self.username = username
        self.email = email
        self.photoUrl = photoUrl
        self.selectedHouseholdUID = selectedHouseholdUID
        self.householdUIDs = householdUIDs
        self.recipeUIDs = recipeUIDs
        self.invitationUIDs = invitationUIDs
    }

    var firestoreData: [String: Any] {
        var data: [String: Any] = [
            "uid": id,
            "username": username,
            "email": email,
            "photoUrl": photoUrl,
            "householdUIDs": householdUIDs,
            "recipeUIDs": recipeUIDs,
            "invitationUIDs": invitationUIDs
        ]
        if let selectedHouseholdUID {
            data["selectedHouseholdUID"] = selectedHouseholdUID
        }
        return data
    }

    static func fromFirestore(_ data: [String: Any], id: String) -> SLUser {
        SLUser(
            id: id,
            username: data["username"] as? String ?? "",
            email: data["email"] as? String ?? "",
            photoUrl: data["photoUrl"] as? String ?? "",
            selectedHouseholdUID: data["selectedHouseholdUID"] as? String,
            householdUIDs: data["householdUIDs"] as? [String] ?? [],
            recipeUIDs: data["recipeUIDs"] as? [String] ?? [],
            invitationUIDs: data["invitationUIDs"] as? [String] ?? []
        )
    }
}
