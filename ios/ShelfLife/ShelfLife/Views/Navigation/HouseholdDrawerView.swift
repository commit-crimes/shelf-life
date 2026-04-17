import SwiftUI

struct HouseholdDrawerView: View {
    @EnvironmentObject var overviewVM: OverviewViewModel
    @StateObject private var householdVM = HouseholdViewModel()
    @State private var showingCreate = false
    @State private var editingHousehold: Household?
    @State private var deletingHousehold: Household?
    @State private var showingDeleteConfirm = false

    var body: some View {
        NavigationStack {
            List {
                Section("Your Households") {
                    ForEach(overviewVM.households) { household in
                        HStack {
                            Image(systemName: overviewVM.currentHousehold?.id == household.id
                                  ? "checkmark.circle.fill" : "circle")
                                .foregroundStyle(.green)

                            Text(household.name)
                                .fontWeight(overviewVM.currentHousehold?.id == household.id ? .semibold : .regular)

                            Spacer()

                            Menu {
                                Button("Edit") { editingHousehold = household }
                                Button("Delete", role: .destructive) {
                                    deletingHousehold = household
                                    showingDeleteConfirm = true
                                }
                            } label: {
                                Image(systemName: "ellipsis")
                                    .foregroundStyle(.secondary)
                            }
                        }
                        .contentShape(Rectangle())
                        .onTapGesture {
                            Task { await overviewVM.selectHousehold(household) }
                            overviewVM.isShowingHouseholdDrawer = false
                        }
                    }
                }

                Section {
                    Button {
                        showingCreate = true
                    } label: {
                        Label("Create New Household", systemImage: "plus.circle.fill")
                            .foregroundStyle(.green)
                    }
                }
            }
            .navigationTitle("Households")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { overviewVM.isShowingHouseholdDrawer = false }
                }
            }
            .sheet(isPresented: $showingCreate) {
                HouseholdCreationView(mode: .create)
                    .environmentObject(overviewVM)
            }
            .sheet(item: $editingHousehold) { household in
                HouseholdCreationView(mode: .edit(household))
                    .environmentObject(overviewVM)
            }
            .alert("Delete Household", isPresented: $showingDeleteConfirm) {
                Button("Delete", role: .destructive) {
                    if let h = deletingHousehold {
                        Task { await householdVM.deleteHousehold(h) }
                    }
                }
                Button("Cancel", role: .cancel) {}
            } message: {
                Text("Are you sure you want to delete '\(deletingHousehold?.name ?? "")'? This cannot be undone.")
            }
        }
    }
}
