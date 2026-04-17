import Foundation

enum RecipeType: String, Codable, CaseIterable {
    case basic = "BASIC"
    case highProtein = "HIGH_PROTEIN"
    case lowCalorie = "LOW_CALORIE"
    case personal = "PERSONAL"

    var displayName: String {
        switch self {
        case .basic: return "Basic"
        case .highProtein: return "High Protein"
        case .lowCalorie: return "Low Calorie"
        case .personal: return "Personal"
        }
    }
}

struct Ingredient: Codable, Identifiable, Equatable {
    var id: String
    var name: String
    var quantity: Quantity
    var macros: NutritionFacts

    init(id: String = UUID().uuidString, name: String = "", quantity: Quantity = Quantity(), macros: NutritionFacts = .empty) {
        self.id = id
        self.name = name
        self.quantity = quantity
        self.macros = macros
    }

    var firestoreData: [String: Any] {
        [
            "id": id,
            "name": name,
            "quantity": ["amount": quantity.amount, "unit": quantity.unit.rawValue],
            "macros": [
                "energyKcal": macros.energyKcal,
                "fat": macros.fat,
                "saturatedFat": macros.saturatedFat,
                "carbohydrates": macros.carbohydrates,
                "sugars": macros.sugars,
                "proteins": macros.proteins,
                "salt": macros.salt
            ]
        ]
    }

    static func fromFirestore(_ data: [String: Any]) -> Ingredient {
        let quantityData = data["quantity"] as? [String: Any] ?? [:]
        let macrosData = data["macros"] as? [String: Any] ?? [:]
        return Ingredient(
            id: data["id"] as? String ?? UUID().uuidString,
            name: data["name"] as? String ?? "",
            quantity: Quantity(
                amount: quantityData["amount"] as? Double ?? 0,
                unit: FoodUnit(rawValue: quantityData["unit"] as? String ?? "") ?? .gram
            ),
            macros: NutritionFacts(
                energyKcal: macrosData["energyKcal"] as? Double ?? 0,
                fat: macrosData["fat"] as? Double ?? 0,
                saturatedFat: macrosData["saturatedFat"] as? Double ?? 0,
                carbohydrates: macrosData["carbohydrates"] as? Double ?? 0,
                sugars: macrosData["sugars"] as? Double ?? 0,
                proteins: macrosData["proteins"] as? Double ?? 0,
                salt: macrosData["salt"] as? Double ?? 0
            )
        )
    }
}

struct Recipe: Identifiable, Codable, Equatable {
    var id: String
    var name: String
    var instructions: [String]
    var servings: Float
    var time: TimeInterval
    var ingredients: [Ingredient]
    var recipeType: RecipeType
    var workInProgress: Bool

    init(id: String = UUID().uuidString, name: String = "", instructions: [String] = [],
         servings: Float = 2, time: TimeInterval = 1800, ingredients: [Ingredient] = [],
         recipeType: RecipeType = .basic, workInProgress: Bool = false) {
        self.id = id
        self.name = name
        self.instructions = instructions
        self.servings = servings
        self.time = time
        self.ingredients = ingredients
        self.recipeType = recipeType
        self.workInProgress = workInProgress
    }

    var timeFormatted: String {
        let minutes = Int(time / 60)
        if minutes < 60 { return "\(minutes) min" }
        let hours = minutes / 60
        let remainingMinutes = minutes % 60
        return remainingMinutes > 0 ? "\(hours)h \(remainingMinutes)m" : "\(hours)h"
    }

    var totalCalories: Double {
        ingredients.reduce(0) { $0 + $1.macros.energyKcal * $1.quantity.amount / 100 } / Double(servings)
    }

    var firestoreData: [String: Any] {
        [
            "uid": id,
            "name": name,
            "instructions": instructions,
            "servings": servings,
            "time": time,
            "ingredients": ingredients.map { $0.firestoreData },
            "recipeType": recipeType.rawValue,
            "workInProgress": workInProgress
        ]
    }

    static func fromFirestore(_ data: [String: Any], id: String) -> Recipe {
        let ingredientsData = data["ingredients"] as? [[String: Any]] ?? []
        return Recipe(
            id: id,
            name: data["name"] as? String ?? "",
            instructions: data["instructions"] as? [String] ?? [],
            servings: data["servings"] as? Float ?? 2,
            time: data["time"] as? TimeInterval ?? 1800,
            ingredients: ingredientsData.map { Ingredient.fromFirestore($0) },
            recipeType: RecipeType(rawValue: data["recipeType"] as? String ?? "") ?? .basic,
            workInProgress: data["workInProgress"] as? Bool ?? false
        )
    }
}

struct RecipePrompt {
    var name: String
    var recipeType: RecipeType
    var specialInstructions: String
    var ingredients: [FoodItem]
    var missingIngredients: [String]
    var servings: Int
    var shortDuration: Bool
    var onlyHouseholdItems: Bool
    var prioritiseSoonToExpire: Bool
    var macroPreferences: NutritionFacts

    init() {
        name = ""
        recipeType = .basic
        specialInstructions = ""
        ingredients = []
        missingIngredients = []
        servings = 2
        shortDuration = false
        onlyHouseholdItems = false
        prioritiseSoonToExpire = false
        macroPreferences = .empty
    }
}
