import SwiftUI

struct InvitationsView: View {
    @Environment(\.dismiss) private var dismiss
    @StateObject private var vm = InvitationViewModel()

    var body: some View {
        NavigationStack {
            List {
                sendInvitationSection

                if vm.invitations.isEmpty {
                    Section {
                        VStack(spacing: 12) {
                            Image(systemName: "envelope.badge")
                                .font(.system(size: 40))
                                .foregroundStyle(.secondary)
                            Text("No pending invitations")
                                .foregroundStyle(.secondary)
                        }
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 20)
                    }
                } else {
                    Section("Pending Invitations") {
                        ForEach(vm.invitations) { invitation in
                            InvitationRow(
                                invitation: invitation,
                                onAccept: { Task { await vm.acceptInvitation(invitation) } },
                                onDecline: { Task { await vm.declineInvitation(invitation) } }
                            )
                        }
                    }
                }

                if let error = vm.errorMessage {
                    Section { Text(error).foregroundStyle(.red).font(.caption) }
                }
                if let success = vm.successMessage {
                    Section { Text(success).foregroundStyle(.green).font(.caption) }
                }
            }
            .navigationTitle("Invitations")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Done") { dismiss() }
                }
            }
            .overlay {
                if vm.isLoading { ProgressView().frame(maxWidth: .infinity, maxHeight: .infinity).background(.ultraThinMaterial) }
            }
        }
    }

    private var sendInvitationSection: some View {
        Section("Invite to Household") {
            HStack {
                TextField("Email address", text: $vm.inviteEmail)
                    .keyboardType(.emailAddress)
                    .textInputAutocapitalization(.never)
                    .autocorrectionDisabled()

                Button("Send") {
                    Task { await vm.sendInvitation() }
                }
                .disabled(vm.inviteEmail.isEmpty || vm.isLoading)
                .foregroundStyle(.green)
            }
        }
    }
}

struct InvitationRow: View {
    let invitation: Invitation
    let onAccept: () -> Void
    let onDecline: () -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 8) {
            HStack {
                Image(systemName: "house.fill").foregroundStyle(.green)
                VStack(alignment: .leading) {
                    Text(invitation.householdName).font(.subheadline.bold())
                    Text("From \(invitation.inviterName)").font(.caption).foregroundStyle(.secondary)
                }
            }

            HStack(spacing: 12) {
                Button(action: onAccept) {
                    Text("Accept")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(.green)
                        .foregroundStyle(.white)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }

                Button(action: onDecline) {
                    Text("Decline")
                        .frame(maxWidth: .infinity)
                        .padding(.vertical, 8)
                        .background(Color(.systemGray6))
                        .foregroundStyle(.primary)
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                }
            }
            .buttonStyle(.plain)
        }
        .padding(.vertical, 4)
    }
}
