import SwiftUI

struct OverviewView: View {
    @EnvironmentObject var vm: OverviewViewModel
    @ObservedObject private var foodItemService = FoodItemService.shared
    @State private var showingItemDetail: FoodItem?
    @State private var showingEditItem: FoodItem?

    var body: some View {
        NavigationStack {
            Group {
                if vm.hasNoHouseholds {
                    FirstTimeWelcomeView()
                        .environmentObject(vm)
                } else {
                    inventoryContent
                }
            }
            .navigationTitle(vm.currentHousehold?.name ?? "ShelfLife")
            .navigationBarTitleDisplayMode(.large)
            .toolbar { toolbarContent }
            .sheet(isPresented: $vm.isShowingHouseholdDrawer) {
                HouseholdDrawerView().environmentObject(vm)
            }
            .sheet(isPresented: $vm.isShowingAddItem) {
                AddFoodItemView()
            }
            .sheet(isPresented: $vm.isShowingLeaderboard) {
                LeaderboardView()
            }
            .sheet(item: $showingItemDetail) { item in
                FoodItemDetailView(item: item)
            }
            .sheet(item: $showingEditItem) { item in
                EditFoodItemView(item: item)
            }
        }
    }

    @ViewBuilder
    private var inventoryContent: some View {
        ZStack(alignment: .bottomTrailing) {
            VStack(spacing: 0) {
                searchAndFilterBar

                if vm.filteredItems.isEmpty {
                    emptyState
                } else {
                    foodItemList
                }
            }

            fabGroup
        }
    }

    private var searchAndFilterBar: some View {
        VStack(spacing: 8) {
            HStack {
                Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
                TextField("Search items...", text: $vm.searchText)
                if !vm.searchText.isEmpty {
                    Button { vm.searchText = "" } label: {
                        Image(systemName: "xmark.circle.fill").foregroundStyle(.secondary)
                    }
                }
            }
            .padding(10)
            .background(Color(.systemGray6))
            .clipShape(RoundedRectangle(cornerRadius: 10))
            .padding(.horizontal)

            ScrollView(.horizontal, showsIndicators: false) {
                HStack(spacing: 8) {
                    ForEach(FoodCategory.allCases, id: \.self) { cat in
                        CategoryChip(
                            category: cat,
                            isSelected: vm.selectedCategory == cat,
                            action: { vm.selectCategory(cat) }
                        )
                    }
                }
                .padding(.horizontal)
            }
        }
        .padding(.vertical, 8)
        .background(Color(.systemBackground))
    }

    private var foodItemList: some View {
        List {
            ForEach(vm.filteredItems) { item in
                FoodItemRow(
                    item: item,
                    isSelected: vm.selectedItems.contains(item.id),
                    isMultiSelect: vm.isMultiSelectMode,
                    onTap: {
                        if vm.isMultiSelectMode {
                            vm.toggleItemSelection(item.id)
                        } else {
                            showingItemDetail = item
                        }
                    },
                    onEdit: { showingEditItem = item },
                    onDelete: { Task { await vm.deleteItem(item) } }
                )
            }
        }
        .listStyle(.plain)
        .animation(.default, value: vm.filteredItems.count)
    }

    private var emptyState: some View {
        VStack(spacing: 16) {
            Spacer()
            Image(systemName: "cart.badge.plus")
                .font(.system(size: 60))
                .foregroundStyle(.secondary)
            Text("No items found")
                .font(.headline)
            Text("Add items using the barcode scanner\nor manually with the + button")
                .font(.subheadline)
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            Spacer()
        }
        .padding()
    }

    private var fabGroup: some View {
        VStack(spacing: 12) {
            if vm.isMultiSelectMode && !vm.selectedItems.isEmpty {
                Button {
                    Task { await vm.deleteSelectedItems() }
                } label: {
                    Label("Delete (\(vm.selectedItems.count))", systemImage: "trash")
                        .padding(.horizontal, 16)
                        .padding(.vertical, 12)
                        .background(.red)
                        .foregroundStyle(.white)
                        .clipShape(Capsule())
                }
                .shadow(radius: 4)
            }

            HStack(spacing: 12) {
                Button {
                    vm.isShowingLeaderboard = true
                } label: {
                    Image(systemName: "trophy.fill")
                        .frame(width: 44, height: 44)
                        .background(.orange)
                        .foregroundStyle(.white)
                        .clipShape(Circle())
                        .shadow(radius: 4)
                }

                Button {
                    vm.isShowingScanner = true
                } label: {
                    Image(systemName: "barcode.viewfinder")
                        .frame(width: 56, height: 56)
                        .background(.green)
                        .foregroundStyle(.white)
                        .clipShape(Circle())
                        .shadow(radius: 4)
                }

                Button {
                    vm.isShowingAddItem = true
                } label: {
                    Image(systemName: "plus")
                        .frame(width: 56, height: 56)
                        .background(.blue)
                        .foregroundStyle(.white)
                        .clipShape(Circle())
                        .shadow(radius: 4)
                }
            }
        }
        .padding(.trailing, 20)
        .padding(.bottom, 20)
    }

    @ToolbarContentBuilder
    private var toolbarContent: some ToolbarContent {
        ToolbarItem(placement: .topBarLeading) {
            Button {
                vm.isShowingHouseholdDrawer = true
            } label: {
                Image(systemName: "house.fill")
            }
        }

        ToolbarItem(placement: .topBarTrailing) {
            Button {
                vm.isMultiSelectMode.toggle()
                if !vm.isMultiSelectMode { vm.selectedItems.removeAll() }
            } label: {
                Image(systemName: vm.isMultiSelectMode ? "checkmark.circle.fill" : "checkmark.circle")
            }
        }
    }
}

struct CategoryChip: View {
    let category: FoodCategory
    let isSelected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(spacing: 4) {
                Text(category.icon)
                Text(category.displayName)
                    .font(.caption.bold())
            }
            .padding(.horizontal, 12)
            .padding(.vertical, 6)
            .background(isSelected ? Color.green : Color(.systemGray6))
            .foregroundStyle(isSelected ? .white : .primary)
            .clipShape(Capsule())
        }
        .buttonStyle(.plain)
    }
}

struct FoodItemRow: View {
    let item: FoodItem
    let isSelected: Bool
    let isMultiSelect: Bool
    let onTap: () -> Void
    let onEdit: () -> Void
    let onDelete: () -> Void

    var body: some View {
        HStack(spacing: 12) {
            if isMultiSelect {
                Image(systemName: isSelected ? "checkmark.circle.fill" : "circle")
                    .foregroundStyle(isSelected ? .green : .secondary)
            }

            ZStack {
                RoundedRectangle(cornerRadius: 8)
                    .fill(Color(.systemGray6))
                    .frame(width: 50, height: 50)
                if !item.foodFacts.imageUrl.isEmpty {
                    AsyncImage(url: URL(string: item.foodFacts.imageUrl)) { img in
                        img.resizable().scaledToFill()
                    } placeholder: {
                        Text(item.foodFacts.category.icon).font(.title2)
                    }
                    .frame(width: 50, height: 50)
                    .clipShape(RoundedRectangle(cornerRadius: 8))
                } else {
                    Text(item.foodFacts.category.icon).font(.title2)
                }
            }

            VStack(alignment: .leading, spacing: 2) {
                Text(item.foodFacts.name)
                    .font(.subheadline.bold())
                    .lineLimit(1)

                HStack(spacing: 8) {
                    Text(item.foodFacts.category.displayName)
                        .font(.caption)
                        .foregroundStyle(.secondary)

                    Text(item.location.displayName)
                        .font(.caption)
                        .foregroundStyle(.secondary)
                }

                if let expiry = item.expiryDate {
                    expiryLabel(expiry)
                }
            }

            Spacer()

            Text(item.status.displayName)
                .font(.caption)
                .padding(.horizontal, 8)
                .padding(.vertical, 4)
                .background(statusColor(item.status).opacity(0.15))
                .foregroundStyle(statusColor(item.status))
                .clipShape(Capsule())
        }
        .padding(.vertical, 4)
        .contentShape(Rectangle())
        .onTapGesture(perform: onTap)
        .swipeActions(edge: .trailing) {
            Button(role: .destructive, action: onDelete) {
                Label("Delete", systemImage: "trash")
            }
            Button(action: onEdit) {
                Label("Edit", systemImage: "pencil")
            }
            .tint(.blue)
        }
    }

    private func expiryLabel(_ date: Date) -> some View {
        let days = Calendar.current.dateComponents([.day], from: Date(), to: date).day ?? 0
        let color: Color = days < 0 ? .red : days <= 3 ? .orange : days <= 7 ? .yellow : .green
        let text: String = days < 0 ? "Expired" : days == 0 ? "Expires today" : "Expires in \(days)d"

        return Text(text)
            .font(.caption)
            .foregroundStyle(color)
    }

    private func statusColor(_ status: FoodStatus) -> Color {
        switch status {
        case .unopened: return .blue
        case .opened: return .orange
        case .consumed: return .gray
        case .expired: return .red
        }
    }
}

struct FirstTimeWelcomeView: View {
    @EnvironmentObject var vm: OverviewViewModel
    @StateObject private var householdVM = HouseholdViewModel()
    @State private var showingCreate = false

    var body: some View {
        VStack(spacing: 32) {
            Spacer()

            Image(systemName: "house.circle.fill")
                .font(.system(size: 80))
                .foregroundStyle(.green)

            VStack(spacing: 12) {
                Text("Welcome to ShelfLife!")
                    .font(.title.bold())

                Text("Start by creating your first household to manage your food inventory.")
                    .font(.subheadline)
                    .foregroundStyle(.secondary)
                    .multilineTextAlignment(.center)
                    .padding(.horizontal, 32)
            }

            Button {
                showingCreate = true
            } label: {
                Label("Create Household", systemImage: "plus")
                    .frame(maxWidth: .infinity)
                    .padding()
                    .background(.green)
                    .foregroundStyle(.white)
                    .clipShape(RoundedRectangle(cornerRadius: 12))
            }
            .padding(.horizontal, 32)

            Spacer()
        }
        .sheet(isPresented: $showingCreate) {
            HouseholdCreationView(mode: .create)
                .environmentObject(vm)
        }
    }
}
