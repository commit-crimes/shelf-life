import Foundation

class OpenFoodFactsService {
    static let shared = OpenFoodFactsService()
    private let baseURL = "https://world.openfoodfacts.org"
    private let session = URLSession.shared

    private init() {}

    func searchByBarcode(_ barcode: String) async throws -> [FoodFacts] {
        let urlString = "\(baseURL)/api/v0/product/\(barcode).json"
        guard let url = URL(string: urlString) else { throw OFFError.invalidURL }

        let (data, response) = try await session.data(from: url)
        guard (response as? HTTPURLResponse)?.statusCode == 200 else { throw OFFError.networkError }

        let decoded = try JSONDecoder().decode(OFFSingleProductResponse.self, from: data)
        guard let product = decoded.product, decoded.status == 1 else { return [] }
        return [mapProduct(product)]
    }

    func searchByQuery(_ query: String) async throws -> [FoodFacts] {
        var components = URLComponents(string: "\(baseURL)/cgi/search.pl")!
        components.queryItems = [
            URLQueryItem(name: "search_terms", value: query),
            URLQueryItem(name: "json", value: "1"),
            URLQueryItem(name: "action", value: "process"),
            URLQueryItem(name: "page_size", value: "7"),
            URLQueryItem(name: "fields", value: "product_name,code,quantity,nutriments,image_url,categories_tags")
        ]
        guard let url = components.url else { throw OFFError.invalidURL }

        let (data, response) = try await session.data(from: url)
        guard (response as? HTTPURLResponse)?.statusCode == 200 else { throw OFFError.networkError }

        let decoded = try JSONDecoder().decode(OFFSearchResponse.self, from: data)
        return decoded.products?.compactMap { p in
            guard p.imageURL != nil else { return nil }
            return mapProduct(p)
        }.prefix(7).map { $0 } ?? []
    }

    private func mapProduct(_ product: OFFProduct) -> FoodFacts {
        let nutriments = product.nutriments
        let nutrition = NutritionFacts(
            energyKcal: nutriments?.energyKcal100g ?? 0,
            fat: nutriments?.fat100g ?? 0,
            saturatedFat: nutriments?.saturatedFat100g ?? 0,
            carbohydrates: nutriments?.carbohydrates100g ?? 0,
            sugars: nutriments?.sugars100g ?? 0,
            proteins: nutriments?.proteins100g ?? 0,
            salt: nutriments?.salt100g ?? 0
        )

        let (amount, unit) = parseQuantity(product.quantity)
        let category = mapCategory(product.categoriesTags)

        return FoodFacts(
            name: product.productName ?? product.code ?? "",
            barcode: product.code ?? "",
            quantity: Quantity(amount: amount, unit: unit),
            category: category,
            nutritionFacts: nutrition,
            imageUrl: product.imageURL ?? ""
        )
    }

    private func parseQuantity(_ rawQuantity: String?) -> (Double, FoodUnit) {
        guard let raw = rawQuantity else { return (0, .gram) }
        let lowered = raw.lowercased()

        let pattern = #"(\d+\.?\d*)\s*(g|ml|kg|l|oz|cl)"#
        if let range = raw.range(of: pattern, options: .regularExpression) {
            let match = String(raw[range])
            let components = match.components(separatedBy: .whitespaces)
            let numStr = components.first?.filter { $0.isNumber || $0 == "." } ?? "0"
            let amount = Double(numStr) ?? 0

            if lowered.contains("ml") || lowered.contains("cl") || lowered.contains(" l") {
                let finalAmount = lowered.contains("cl") ? amount * 10 : (lowered.contains(" l") ? amount * 1000 : amount)
                return (finalAmount, .ml)
            } else if lowered.contains("kg") {
                return (amount * 1000, .gram)
            } else {
                return (amount, .gram)
            }
        }

        if let number = Double(raw.filter { $0.isNumber || $0 == "." }) {
            return (number, .gram)
        }

        return (0, .gram)
    }

    private func mapCategory(_ tags: [String]?) -> FoodCategory {
        guard let tags else { return .other }
        let joined = tags.joined(separator: " ").lowercased()

        if joined.contains("dairy") || joined.contains("milk") || joined.contains("cheese") || joined.contains("yogurt") { return .dairy }
        if joined.contains("meat") || joined.contains("beef") || joined.contains("pork") || joined.contains("chicken") { return .meat }
        if joined.contains("fish") || joined.contains("seafood") { return .fish }
        if joined.contains("fruit") { return .fruit }
        if joined.contains("vegetable") || joined.contains("veggie") { return .vegetable }
        if joined.contains("grain") || joined.contains("cereal") || joined.contains("bread") || joined.contains("pasta") { return .grain }
        if joined.contains("beverage") || joined.contains("drink") || joined.contains("juice") { return .beverage }
        if joined.contains("snack") || joined.contains("chip") || joined.contains("cookie") { return .snack }
        return .other
    }

    enum OFFError: LocalizedError {
        case invalidURL
        case networkError
        case noResults

        var errorDescription: String? {
            switch self {
            case .invalidURL: return "Invalid URL"
            case .networkError: return "Network error fetching product data"
            case .noResults: return "No products found"
            }
        }
    }
}

// MARK: - Response models
private struct OFFSingleProductResponse: Decodable {
    let status: Int?
    let product: OFFProduct?
}

private struct OFFSearchResponse: Decodable {
    let products: [OFFProduct]?
}

private struct OFFProduct: Decodable {
    let code: String?
    let productName: String?
    let quantity: String?
    let imageURL: String?
    let categoriesTags: [String]?
    let nutriments: OFFNutriments?

    enum CodingKeys: String, CodingKey {
        case code
        case productName = "product_name"
        case quantity
        case imageURL = "image_url"
        case categoriesTags = "categories_tags"
        case nutriments
    }
}

private struct OFFNutriments: Decodable {
    let energyKcal100g: Double?
    let fat100g: Double?
    let saturatedFat100g: Double?
    let carbohydrates100g: Double?
    let sugars100g: Double?
    let proteins100g: Double?
    let salt100g: Double?

    enum CodingKeys: String, CodingKey {
        case energyKcal100g = "energy-kcal_100g"
        case fat100g = "fat_100g"
        case saturatedFat100g = "saturated-fat_100g"
        case carbohydrates100g = "carbohydrates_100g"
        case sugars100g = "sugars_100g"
        case proteins100g = "proteins_100g"
        case salt100g = "salt_100g"
    }
}
