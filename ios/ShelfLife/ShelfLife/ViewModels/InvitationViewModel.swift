import Foundation

@MainActor
class InvitationViewModel: ObservableObject {
    @Published var inviteEmail = ""
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var successMessage: String?

    private let invitationService = InvitationService.shared
    private let householdService = HouseholdService.shared

    var invitations: [Invitation] { invitationService.invitations }

    func sendInvitation() async {
        guard let household = householdService.selectedHousehold else {
            errorMessage = "No household selected"
            return
        }
        guard !inviteEmail.trimmingCharacters(in: .whitespaces).isEmpty else {
            errorMessage = "Please enter an email address"
            return
        }

        isLoading = true
        errorMessage = nil
        successMessage = nil

        do {
            try await invitationService.sendInvitation(
                to: inviteEmail.trimmingCharacters(in: .whitespaces),
                householdID: household.id,
                householdName: household.name
            )
            successMessage = "Invitation sent to \(inviteEmail)"
            inviteEmail = ""
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func acceptInvitation(_ invitation: Invitation) async {
        isLoading = true
        errorMessage = nil
        do {
            try await invitationService.acceptInvitation(invitation)
            if let user = UserService.shared.currentSLUser {
                try await householdService.loadHouseholds(for: user)
            }
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }

    func declineInvitation(_ invitation: Invitation) async {
        isLoading = true
        do {
            try await invitationService.declineInvitation(invitation)
        } catch {
            errorMessage = error.localizedDescription
        }
        isLoading = false
    }
}
