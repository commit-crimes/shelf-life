import Foundation

struct Household: Identifiable, Codable, Equatable {
    var id: String
    var name: String
    var members: [String]
    var sharedRecipes: [String]
    var ratPoints: [String: Int]
    var stinkyPoints: [String: Int]

    init(id: String = UUID().uuidString, name: String = "", members: [String] = [],
         sharedRecipes: [String] = [], ratPoints: [String: Int] = [:], stinkyPoints: [String: Int] = [:]) {
        self.id = id
        self.name = name
        self.members = members
        self.sharedRecipes = sharedRecipes
        self.ratPoints = ratPoints
        self.stinkyPoints = stinkyPoints
    }

    var firestoreData: [String: Any] {
        [
            "uid": id,
            "name": name,
            "members": members,
            "sharedRecipes": sharedRecipes,
            "ratPoints": ratPoints,
            "stinkyPoints": stinkyPoints
        ]
    }

    static func fromFirestore(_ data: [String: Any], id: String) -> Household {
        Household(
            id: id,
            name: data["name"] as? String ?? "",
            members: data["members"] as? [String] ?? [],
            sharedRecipes: data["sharedRecipes"] as? [String] ?? [],
            ratPoints: (data["ratPoints"] as? [String: Any] ?? [:]).compactMapValues { $0 as? Int },
            stinkyPoints: (data["stinkyPoints"] as? [String: Any] ?? [:]).compactMapValues { $0 as? Int }
        )
    }
}
