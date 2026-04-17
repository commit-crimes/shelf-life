import SwiftUI

struct AddFoodItemView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = FoodItemViewModel()
    var prefillFoodFacts: FoodFacts?

    var body: some View {
        NavigationStack {
            FoodItemFormView(vm: vm, title: "Add Item")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { dismiss() }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Add") {
                            Task {
                                if await vm.save() { dismiss() }
                            }
                        }
                        .disabled(vm.foodFacts.name.isEmpty || vm.isLoading)
                    }
                }
        }
        .onAppear {
            if let facts = prefillFoodFacts { vm.populate(from: facts) }
        }
    }
}

struct EditFoodItemView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = FoodItemViewModel()
    let item: FoodItem

    var body: some View {
        NavigationStack {
            FoodItemFormView(vm: vm, title: "Edit Item")
                .toolbar {
                    ToolbarItem(placement: .cancellationAction) {
                        Button("Cancel") { dismiss() }
                    }
                    ToolbarItem(placement: .confirmationAction) {
                        Button("Save") {
                            Task {
                                if await vm.save(existingID: item.id) { dismiss() }
                            }
                        }
                        .disabled(vm.foodFacts.name.isEmpty || vm.isLoading)
                    }
                }
        }
        .onAppear { vm.populate(from: item) }
    }
}

struct FoodItemFormView: View {
    @ObservedObject var vm: FoodItemViewModel
    let title: String

    var body: some View {
        Form {
            Section("Product Info") {
                TextField("Name *", text: $vm.foodFacts.name)
                    .textInputAutocapitalization(.words)

                Picker("Category", selection: $vm.foodFacts.category) {
                    ForEach(FoodCategory.allCases, id: \.self) { cat in
                        Label(cat.displayName, systemImage: "circle.fill")
                            .tag(cat)
                    }
                }

                HStack {
                    TextField("Amount", value: $vm.foodFacts.quantity.amount, format: .number)
                        .keyboardType(.decimalPad)

                    Picker("Unit", selection: $vm.foodFacts.quantity.unit) {
                        ForEach(FoodUnit.allCases, id: \.self) { unit in
                            Text(unit.displayName).tag(unit)
                        }
                    }
                    .pickerStyle(.segmented)
                    .frame(maxWidth: 180)
                }
            }

            Section("Storage") {
                Picker("Location", selection: $vm.location) {
                    ForEach(FoodStorageLocation.allCases, id: \.self) { loc in
                        Label(loc.displayName, systemImage: loc.icon).tag(loc)
                    }
                }

                Picker("Status", selection: $vm.status) {
                    ForEach(FoodStatus.allCases, id: \.self) { s in
                        Text(s.displayName).tag(s)
                    }
                }
            }

            Section("Dates") {
                Toggle("Buy Date", isOn: $vm.hasBuyDate)
                if vm.hasBuyDate {
                    DatePicker("", selection: $vm.buyDate, displayedComponents: .date)
                        .labelsHidden()
                }

                Toggle("Expiry Date", isOn: $vm.hasExpiryDate)
                if vm.hasExpiryDate {
                    DatePicker("", selection: $vm.expiryDate, displayedComponents: .date)
                        .labelsHidden()
                }

                Toggle("Open Date", isOn: $vm.hasOpenDate)
                if vm.hasOpenDate {
                    DatePicker("", selection: $vm.openDate, displayedComponents: .date)
                        .labelsHidden()
                }
            }

            Section("Nutrition (per 100g/ml)") {
                NutritionRow(label: "Calories (kcal)", value: $vm.foodFacts.nutritionFacts.energyKcal)
                NutritionRow(label: "Fat (g)", value: $vm.foodFacts.nutritionFacts.fat)
                NutritionRow(label: "Saturated Fat (g)", value: $vm.foodFacts.nutritionFacts.saturatedFat)
                NutritionRow(label: "Carbohydrates (g)", value: $vm.foodFacts.nutritionFacts.carbohydrates)
                NutritionRow(label: "Sugars (g)", value: $vm.foodFacts.nutritionFacts.sugars)
                NutritionRow(label: "Proteins (g)", value: $vm.foodFacts.nutritionFacts.proteins)
                NutritionRow(label: "Salt (g)", value: $vm.foodFacts.nutritionFacts.salt)
            }

            if let error = vm.errorMessage {
                Section {
                    Text(error).foregroundStyle(.red).font(.caption)
                }
            }
        }
        .navigationTitle(title)
        .navigationBarTitleDisplayMode(.inline)
        .overlay {
            if vm.isLoading { ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity).background(.ultraThinMaterial) }
        }
    }
}

struct NutritionRow: View {
    let label: String
    @Binding var value: Double

    var body: some View {
        HStack {
            Text(label).foregroundStyle(.secondary)
            Spacer()
            TextField("0", value: $value, format: .number)
                .keyboardType(.decimalPad)
                .multilineTextAlignment(.trailing)
                .frame(width: 80)
        }
    }
}
