import SwiftUI

enum HouseholdCreationMode {
    case create
    case edit(Household)
}

struct HouseholdCreationView: View {
    @Environment(\.dismiss) private var dismiss
    @EnvironmentObject var overviewVM: OverviewViewModel
    @StateObject private var vm = HouseholdViewModel()
    let mode: HouseholdCreationMode

    private var isEdit: Bool {
        if case .edit = mode { return true }
        return false
    }

    private var existingHousehold: Household? {
        if case .edit(let h) = mode { return h }
        return nil
    }

    var body: some View {
        NavigationStack {
            Form {
                Section("Household Name") {
                    TextField("e.g. My Home, Family, Office...", text: $vm.householdName)
                        .textInputAutocapitalization(.words)
                }

                if isEdit, let household = existingHousehold {
                    Section("Members") {
                        ForEach(household.members, id: \.self) { memberUID in
                            HStack {
                                Image(systemName: "person.fill").foregroundStyle(.secondary)
                                Text(memberUID == UserService.shared.currentSLUser?.id ? "You" : memberUID)
                                    .foregroundStyle(memberUID == UserService.shared.currentSLUser?.id ? .primary : .secondary)
                            }
                        }
                    }
                }

                if let error = vm.errorMessage {
                    Section {
                        Text(error).foregroundStyle(.red).font(.caption)
                    }
                }
            }
            .navigationTitle(isEdit ? "Edit Household" : "New Household")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { dismiss() }
                }
                ToolbarItem(placement: .confirmationAction) {
                    Button(isEdit ? "Save" : "Create") {
                        Task {
                            let success: Bool
                            if let existing = existingHousehold {
                                success = await vm.updateHousehold(existing)
                            } else {
                                success = await vm.createHousehold()
                            }
                            if success { dismiss() }
                        }
                    }
                    .disabled(vm.householdName.isEmpty || vm.isLoading)
                }
            }
            .overlay {
                if vm.isLoading {
                    ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity).background(.ultraThinMaterial)
                }
            }
        }
        .onAppear {
            if let existing = existingHousehold { vm.populate(from: existing) }
        }
    }
}
