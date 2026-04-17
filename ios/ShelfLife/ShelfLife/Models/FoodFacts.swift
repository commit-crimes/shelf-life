import Foundation

enum FoodUnit: String, Codable, CaseIterable {
    case gram = "GRAM"
    case ml = "ML"
    case count = "COUNT"

    var displayName: String {
        switch self {
        case .gram: return "g"
        case .ml: return "ml"
        case .count: return "count"
        }
    }
}

enum FoodCategory: String, Codable, CaseIterable {
    case fruit = "FRUIT"
    case vegetable = "VEGETABLE"
    case meat = "MEAT"
    case dairy = "DAIRY"
    case grain = "GRAIN"
    case beverage = "BEVERAGE"
    case snack = "SNACK"
    case fish = "FISH"
    case other = "OTHER"

    var displayName: String {
        rawValue.capitalized
    }

    var icon: String {
        switch self {
        case .fruit: return "🍎"
        case .vegetable: return "🥦"
        case .meat: return "🥩"
        case .dairy: return "🧀"
        case .grain: return "🌾"
        case .beverage: return "🥤"
        case .snack: return "🍿"
        case .fish: return "🐟"
        case .other: return "📦"
        }
    }
}

struct Quantity: Codable, Equatable {
    var amount: Double
    var unit: FoodUnit

    init(amount: Double = 0, unit: FoodUnit = .gram) {
        self.amount = amount
        self.unit = unit
    }

    var displayString: String {
        "\(amount.formatted()) \(unit.displayName)"
    }
}

struct NutritionFacts: Codable, Equatable {
    var energyKcal: Double
    var fat: Double
    var saturatedFat: Double
    var carbohydrates: Double
    var sugars: Double
    var proteins: Double
    var salt: Double

    init(energyKcal: Double = 0, fat: Double = 0, saturatedFat: Double = 0,
         carbohydrates: Double = 0, sugars: Double = 0, proteins: Double = 0, salt: Double = 0) {
        self.energyKcal = energyKcal
        self.fat = fat
        self.saturatedFat = saturatedFat
        self.carbohydrates = carbohydrates
        self.sugars = sugars
        self.proteins = proteins
        self.salt = salt
    }

    static let empty = NutritionFacts()
}

struct FoodFacts: Codable, Identifiable, Equatable {
    var id: String { barcode.isEmpty ? name : barcode }
    var name: String
    var barcode: String
    var quantity: Quantity
    var category: FoodCategory
    var nutritionFacts: NutritionFacts
    var imageUrl: String

    init(name: String = "", barcode: String = "", quantity: Quantity = Quantity(),
         category: FoodCategory = .other, nutritionFacts: NutritionFacts = .empty,
         imageUrl: String = "") {
        self.name = name
        self.barcode = barcode
        self.quantity = quantity
        self.category = category
        self.nutritionFacts = nutritionFacts
        self.imageUrl = imageUrl
    }

    var firestoreData: [String: Any] {
        [
            "name": name,
            "barcode": barcode,
            "quantity": ["amount": quantity.amount, "unit": quantity.unit.rawValue],
            "category": category.rawValue,
            "nutritionFacts": [
                "energyKcal": nutritionFacts.energyKcal,
                "fat": nutritionFacts.fat,
                "saturatedFat": nutritionFacts.saturatedFat,
                "carbohydrates": nutritionFacts.carbohydrates,
                "sugars": nutritionFacts.sugars,
                "proteins": nutritionFacts.proteins,
                "salt": nutritionFacts.salt
            ],
            "imageUrl": imageUrl
        ]
    }

    static func fromFirestore(_ data: [String: Any]) -> FoodFacts {
        let quantityData = data["quantity"] as? [String: Any] ?? [:]
        let nutritionData = data["nutritionFacts"] as? [String: Any] ?? [:]

        return FoodFacts(
            name: data["name"] as? String ?? "",
            barcode: data["barcode"] as? String ?? "",
            quantity: Quantity(
                amount: quantityData["amount"] as? Double ?? 0,
                unit: FoodUnit(rawValue: quantityData["unit"] as? String ?? "") ?? .gram
            ),
            category: FoodCategory(rawValue: data["category"] as? String ?? "") ?? .other,
            nutritionFacts: NutritionFacts(
                energyKcal: nutritionData["energyKcal"] as? Double ?? 0,
                fat: nutritionData["fat"] as? Double ?? 0,
                saturatedFat: nutritionData["saturatedFat"] as? Double ?? 0,
                carbohydrates: nutritionData["carbohydrates"] as? Double ?? 0,
                sugars: nutritionData["sugars"] as? Double ?? 0,
                proteins: nutritionData["proteins"] as? Double ?? 0,
                salt: nutritionData["salt"] as? Double ?? 0
            ),
            imageUrl: data["imageUrl"] as? String ?? ""
        )
    }
}
