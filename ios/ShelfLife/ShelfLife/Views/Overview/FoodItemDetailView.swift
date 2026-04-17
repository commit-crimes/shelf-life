import SwiftUI

struct FoodItemDetailView: View {
    let item: FoodItem
    @Environment(\.dismiss) private var dismiss
    @State private var showingEdit = false

    var body: some View {
        NavigationStack {
            ScrollView {
                VStack(spacing: 20) {
                    headerSection
                    infoGrid
                    datesSection
                    nutritionSection
                }
                .padding()
            }
            .navigationTitle(item.foodFacts.name)
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button("Edit") { showingEdit = true }
                }
            }
            .sheet(isPresented: $showingEdit) {
                EditFoodItemView(item: item)
            }
        }
    }

    private var headerSection: some View {
        VStack(spacing: 12) {
            ZStack {
                RoundedRectangle(cornerRadius: 16)
                    .fill(Color(.systemGray6))
                    .frame(width: 120, height: 120)

                if !item.foodFacts.imageUrl.isEmpty {
                    AsyncImage(url: URL(string: item.foodFacts.imageUrl)) { img in
                        img.resizable().scaledToFill()
                    } placeholder: {
                        Text(item.foodFacts.category.icon).font(.system(size: 48))
                    }
                    .frame(width: 120, height: 120)
                    .clipShape(RoundedRectangle(cornerRadius: 16))
                } else {
                    Text(item.foodFacts.category.icon).font(.system(size: 48))
                }
            }

            if !item.foodFacts.barcode.isEmpty {
                Text("Barcode: \(item.foodFacts.barcode)")
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
        }
    }

    private var infoGrid: some View {
        LazyVGrid(columns: [GridItem(.flexible()), GridItem(.flexible())], spacing: 12) {
            InfoCard(title: "Category", value: item.foodFacts.category.displayName, icon: item.foodFacts.category.icon)
            InfoCard(title: "Location", value: item.location.displayName, systemIcon: item.location.icon)
            InfoCard(title: "Status", value: item.status.displayName, systemIcon: "tag")
            InfoCard(title: "Quantity", value: item.foodFacts.quantity.displayString, systemIcon: "scalemass")
        }
    }

    private var datesSection: some View {
        VStack(alignment: .leading, spacing: 8) {
            Text("Dates").font(.headline)

            VStack(spacing: 6) {
                if let buyDate = item.buyDate {
                    DateRow(label: "Purchased", date: buyDate)
                }
                if let expiryDate = item.expiryDate {
                    DateRow(label: "Expires", date: expiryDate, highlight: true, isExpired: item.isExpired)
                }
                if let openDate = item.openDate {
                    DateRow(label: "Opened", date: openDate)
                }
            }
            .padding()
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }

    private var nutritionSection: some View {
        let n = item.foodFacts.nutritionFacts
        return VStack(alignment: .leading, spacing: 8) {
            Text("Nutrition (per 100g/ml)").font(.headline)
            VStack(spacing: 6) {
                NutritionDetailRow(label: "Calories", value: "\(Int(n.energyKcal)) kcal")
                NutritionDetailRow(label: "Fat", value: "\(n.fat.formatted())g")
                NutritionDetailRow(label: "Saturated Fat", value: "\(n.saturatedFat.formatted())g")
                NutritionDetailRow(label: "Carbohydrates", value: "\(n.carbohydrates.formatted())g")
                NutritionDetailRow(label: "Sugars", value: "\(n.sugars.formatted())g")
                NutritionDetailRow(label: "Proteins", value: "\(n.proteins.formatted())g")
                NutritionDetailRow(label: "Salt", value: "\(n.salt.formatted())g")
            }
            .padding()
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 12))
        }
    }
}

struct InfoCard: View {
    let title: String
    let value: String
    var icon: String? = nil
    var systemIcon: String? = nil

    var body: some View {
        VStack(spacing: 6) {
            if let icon { Text(icon).font(.title2) }
            else if let sysIcon { Image(systemName: sysIcon).font(.title2).foregroundStyle(.green) }
            Text(value).font(.subheadline.bold())
            Text(title).font(.caption).foregroundStyle(.secondary)
        }
        .frame(maxWidth: .infinity)
        .padding()
        .background(Color(.systemGray6))
        .clipShape(RoundedRectangle(cornerRadius: 12))
    }

    private var sysIcon: String? { systemIcon }
}

struct DateRow: View {
    let label: String
    let date: Date
    var highlight = false
    var isExpired = false

    private let formatter: DateFormatter = {
        let f = DateFormatter()
        f.dateStyle = .medium
        return f
    }()

    var body: some View {
        HStack {
            Text(label).foregroundStyle(.secondary)
            Spacer()
            Text(formatter.string(from: date))
                .fontWeight(highlight ? .semibold : .regular)
                .foregroundStyle(highlight ? (isExpired ? .red : .primary) : .primary)
        }
        .font(.subheadline)
    }
}

struct NutritionDetailRow: View {
    let label: String
    let value: String

    var body: some View {
        HStack {
            Text(label).foregroundStyle(.secondary)
            Spacer()
            Text(value).fontWeight(.medium)
        }
        .font(.subheadline)
    }
}
