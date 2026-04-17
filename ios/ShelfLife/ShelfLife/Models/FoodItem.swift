import Foundation
import FirebaseFirestore

enum FoodStorageLocation: String, Codable, CaseIterable {
    case pantry = "PANTRY"
    case fridge = "FRIDGE"
    case freezer = "FREEZER"
    case other = "OTHER"

    var displayName: String { rawValue.capitalized }
    var icon: String {
        switch self {
        case .pantry: return "cabinet"
        case .fridge: return "thermometer.snowflake"
        case .freezer: return "snowflake"
        case .other: return "archivebox"
        }
    }
}

enum FoodStatus: String, Codable, CaseIterable {
    case unopened = "UNOPENED"
    case opened = "OPENED"
    case consumed = "CONSUMED"
    case expired = "EXPIRED"

    var displayName: String { rawValue.capitalized }
}

struct FoodItem: Identifiable, Codable, Equatable {
    var id: String
    var foodFacts: FoodFacts
    var buyDate: Date?
    var expiryDate: Date?
    var openDate: Date?
    var location: FoodStorageLocation
    var status: FoodStatus
    var owner: String

    init(id: String = UUID().uuidString,
         foodFacts: FoodFacts = FoodFacts(),
         buyDate: Date? = nil,
         expiryDate: Date? = nil,
         openDate: Date? = nil,
         location: FoodStorageLocation = .pantry,
         status: FoodStatus = .unopened,
         owner: String = "") {
        self.id = id
        self.foodFacts = foodFacts
        self.buyDate = buyDate
        self.expiryDate = expiryDate
        self.openDate = openDate
        self.location = location
        self.status = status
        self.owner = owner
    }

    var isExpired: Bool {
        guard let expiry = expiryDate else { return false }
        return expiry < Date()
    }

    var daysUntilExpiry: Int? {
        guard let expiry = expiryDate else { return nil }
        return Calendar.current.dateComponents([.day], from: Date(), to: expiry).day
    }

    var expiryColor: String {
        guard let days = daysUntilExpiry else { return "secondary" }
        if days < 0 { return "red" }
        if days <= 3 { return "orange" }
        if days <= 7 { return "yellow" }
        return "green"
    }

    var firestoreData: [String: Any] {
        var data: [String: Any] = [
            "uid": id,
            "foodFacts": foodFacts.firestoreData,
            "location": location.rawValue,
            "status": status.rawValue,
            "owner": owner
        ]
        if let buyDate { data["buyDate"] = Timestamp(date: buyDate) }
        if let expiryDate { data["expiryDate"] = Timestamp(date: expiryDate) }
        if let openDate { data["openDate"] = Timestamp(date: openDate) }
        return data
    }

    static func fromFirestore(_ data: [String: Any], id: String) -> FoodItem {
        let factsData = data["foodFacts"] as? [String: Any] ?? [:]

        func tsToDate(_ key: String) -> Date? {
            (data[key] as? Timestamp)?.dateValue()
        }

        return FoodItem(
            id: id,
            foodFacts: FoodFacts.fromFirestore(factsData),
            buyDate: tsToDate("buyDate"),
            expiryDate: tsToDate("expiryDate"),
            openDate: tsToDate("openDate"),
            location: FoodStorageLocation(rawValue: data["location"] as? String ?? "") ?? .pantry,
            status: FoodStatus(rawValue: data["status"] as? String ?? "") ?? .unopened,
            owner: data["owner"] as? String ?? ""
        )
    }
}
