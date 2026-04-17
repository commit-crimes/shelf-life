import Foundation

class OpenAIService {
    static let shared = OpenAIService()

    private let baseURL = "https://api.openai.com/v1/chat/completions"
    private let session = URLSession.shared

    var apiKey: String = ""

    private init() {}

    func generateRecipe(from prompt: RecipePrompt) async throws -> Recipe {
        let systemMessage = buildSystemMessage(prompt)
        let userMessage = buildUserMessage(prompt)

        let requestBody: [String: Any] = [
            "model": "gpt-4o",
            "messages": [
                ["role": "system", "content": systemMessage],
                ["role": "user", "content": userMessage]
            ],
            "tools": [buildRecipeTool()],
            "tool_choice": ["type": "function", "function": ["name": "create_recipe"]]
        ]

        guard let url = URL(string: baseURL) else { throw OpenAIError.invalidURL }
        var request = URLRequest(url: url)
        request.httpMethod = "POST"
        request.setValue("Bearer \(apiKey)", forHTTPHeaderField: "Authorization")
        request.setValue("application/json", forHTTPHeaderField: "Content-Type")
        request.httpBody = try JSONSerialization.data(withJSONObject: requestBody)

        let (data, response) = try await session.data(for: request)
        guard let httpResponse = response as? HTTPURLResponse, httpResponse.statusCode == 200 else {
            throw OpenAIError.apiError
        }

        let decoded = try JSONDecoder().decode(OpenAIResponse.self, from: data)
        guard let toolCall = decoded.choices.first?.message.toolCalls?.first,
              let functionArgs = toolCall.function.arguments.data(using: .utf8) else {
            throw OpenAIError.invalidResponse
        }

        return try parseRecipe(from: functionArgs, prompt: prompt)
    }

    private func buildSystemMessage(_ prompt: RecipePrompt) -> String {
        var message = "You are a professional chef assistant. Generate a recipe based on the user's ingredients and preferences."
        if prompt.prioritiseSoonToExpire {
            message += " Prioritize ingredients that are about to expire."
        }
        if prompt.onlyHouseholdItems {
            message += " Only use the provided ingredients."
        }
        return message
    }

    private func buildUserMessage(_ prompt: RecipePrompt) -> String {
        var parts: [String] = []

        if !prompt.name.isEmpty { parts.append("Recipe name: \(prompt.name)") }
        parts.append("Type: \(prompt.recipeType.displayName)")
        parts.append("Servings: \(prompt.servings)")

        if prompt.shortDuration {
            parts.append("Preparation time: under 15 minutes")
        } else {
            parts.append("Preparation time: 30+ minutes")
        }

        if !prompt.specialInstructions.isEmpty {
            parts.append("Special instructions: \(prompt.specialInstructions)")
        }

        if !prompt.ingredients.isEmpty {
            let ingredientList = prompt.ingredients.map { "\($0.foodFacts.name) (\($0.foodFacts.quantity.displayString))" }.joined(separator: ", ")
            parts.append("Available ingredients: \(ingredientList)")
        }

        if !prompt.missingIngredients.isEmpty {
            parts.append("Additional ingredients allowed: \(prompt.missingIngredients.joined(separator: ", "))")
        }

        switch prompt.recipeType {
        case .highProtein: parts.append("Focus on high protein content.")
        case .lowCalorie: parts.append("Keep calories low.")
        default: break
        }

        return parts.joined(separator: "\n")
    }

    private func buildRecipeTool() -> [String: Any] {
        [
            "type": "function",
            "function": [
                "name": "create_recipe",
                "description": "Create a recipe with the given ingredients",
                "parameters": [
                    "type": "object",
                    "properties": [
                        "name": ["type": "string", "description": "Recipe name"],
                        "instructions": [
                            "type": "array",
                            "items": ["type": "string"],
                            "description": "Step-by-step cooking instructions"
                        ],
                        "servings": ["type": "number", "description": "Number of servings"],
                        "time_minutes": ["type": "number", "description": "Cooking time in minutes"],
                        "ingredients": [
                            "type": "array",
                            "items": [
                                "type": "object",
                                "properties": [
                                    "name": ["type": "string"],
                                    "amount": ["type": "number"],
                                    "unit": ["type": "string", "enum": ["GRAM", "ML", "COUNT"]]
                                ],
                                "required": ["name", "amount", "unit"]
                            ]
                        ]
                    ],
                    "required": ["name", "instructions", "servings", "time_minutes", "ingredients"]
                ]
            ]
        ]
    }

    private func parseRecipe(from data: Data, prompt: RecipePrompt) throws -> Recipe {
        let json = try JSONSerialization.jsonObject(with: data) as? [String: Any] ?? [:]

        let ingredientsList = json["ingredients"] as? [[String: Any]] ?? []
        let ingredients: [Ingredient] = ingredientsList.map { ingData in
            Ingredient(
                name: ingData["name"] as? String ?? "",
                quantity: Quantity(
                    amount: ingData["amount"] as? Double ?? 0,
                    unit: FoodUnit(rawValue: ingData["unit"] as? String ?? "") ?? .gram
                )
            )
        }

        let timeMinutes = json["time_minutes"] as? Double ?? 30

        return Recipe(
            id: UUID().uuidString,
            name: json["name"] as? String ?? prompt.name,
            instructions: json["instructions"] as? [String] ?? [],
            servings: json["servings"] as? Float ?? Float(prompt.servings),
            time: timeMinutes * 60,
            ingredients: ingredients,
            recipeType: prompt.recipeType
        )
    }

    enum OpenAIError: LocalizedError {
        case invalidURL
        case apiError
        case invalidResponse
        case missingAPIKey

        var errorDescription: String? {
            switch self {
            case .invalidURL: return "Invalid API URL"
            case .apiError: return "OpenAI API returned an error. Check your API key."
            case .invalidResponse: return "Could not parse recipe from AI response"
            case .missingAPIKey: return "OpenAI API key not configured"
            }
        }
    }
}

// MARK: - Response Models
private struct OpenAIResponse: Decodable {
    let choices: [Choice]

    struct Choice: Decodable {
        let message: Message
    }

    struct Message: Decodable {
        let toolCalls: [ToolCall]?

        enum CodingKeys: String, CodingKey {
            case toolCalls = "tool_calls"
        }
    }

    struct ToolCall: Decodable {
        let function: FunctionCall
    }

    struct FunctionCall: Decodable {
        let name: String
        let arguments: String
    }
}
