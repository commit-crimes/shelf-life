import SwiftUI

struct ProfileView: View {
    @EnvironmentObject var vm: ProfileViewModel
    @Environment(\.colorScheme) var systemColorScheme
    @State private var showingInvitations = false

    var body: some View {
        NavigationStack {
            List {
                userSection
                themeSection
                invitationsSection
                signOutSection
            }
            .navigationTitle("Profile")
            .sheet(isPresented: $showingInvitations) {
                InvitationsView()
            }
            .sheet(isPresented: $vm.isShowingEasterEgg) {
                EasterEggView()
            }
        }
    }

    private var userSection: some View {
        Section {
            HStack(spacing: 16) {
                ZStack {
                    Circle()
                        .fill(Color.green.opacity(0.2))
                        .frame(width: 64, height: 64)

                    if let photoUrl = vm.currentUser?.photoUrl, !photoUrl.isEmpty, let url = URL(string: photoUrl) {
                        AsyncImage(url: url) { img in
                            img.resizable().scaledToFill()
                        } placeholder: {
                            userInitials
                        }
                        .frame(width: 64, height: 64)
                        .clipShape(Circle())
                    } else {
                        userInitials
                    }
                }
                .onTapGesture { vm.handleAvatarTap() }

                VStack(alignment: .leading, spacing: 4) {
                    if vm.isEditingUsername {
                        HStack {
                            TextField("Username", text: $vm.editedUsername)
                                .textInputAutocapitalization(.words)
                            Button("Save") { Task { await vm.saveUsername() } }
                                .foregroundStyle(.green)
                        }
                    } else {
                        Text(vm.currentUser?.username ?? "User")
                            .font(.headline)
                        Text(vm.currentUser?.email ?? "")
                            .font(.caption)
                            .foregroundStyle(.secondary)
                    }
                }

                Spacer()

                if !vm.isEditingUsername {
                    Button { vm.startEditingUsername() } label: {
                        Image(systemName: "pencil").foregroundStyle(.secondary)
                    }
                }
            }
            .padding(.vertical, 4)
        }
    }

    private var userInitials: some View {
        Text(vm.currentUser?.username.prefix(1).uppercased() ?? "?")
            .font(.title2.bold())
            .foregroundStyle(.green)
    }

    private var themeSection: some View {
        Section("Appearance") {
            Picker("Theme", selection: Binding(
                get: { vm.colorScheme },
                set: { vm.setColorScheme($0) }
            )) {
                Text("System Default").tag(Optional<ColorScheme>.none)
                Text("Light").tag(Optional<ColorScheme>.some(.light))
                Text("Dark").tag(Optional<ColorScheme>.some(.dark))
            }
            .pickerStyle(.segmented)
        }
    }

    private var invitationsSection: some View {
        Section {
            Button {
                showingInvitations = true
            } label: {
                HStack {
                    Label("Invitations", systemImage: "envelope.fill")
                    Spacer()
                    if vm.invitationCount > 0 {
                        Text("\(vm.invitationCount)")
                            .font(.caption.bold())
                            .padding(.horizontal, 8)
                            .padding(.vertical, 4)
                            .background(.red)
                            .foregroundStyle(.white)
                            .clipShape(Capsule())
                    }
                    Image(systemName: "chevron.right").foregroundStyle(.secondary)
                }
            }
            .foregroundStyle(.primary)
        }
    }

    private var signOutSection: some View {
        Section {
            Button(role: .destructive) {
                vm.signOut()
            } label: {
                Label("Sign Out", systemImage: "rectangle.portrait.and.arrow.right")
            }
        }
    }
}

struct EasterEggView: View {
    @Environment(\.dismiss) private var dismiss

    var body: some View {
        NavigationStack {
            ZStack {
                Color.black.ignoresSafeArea()
                VStack(spacing: 20) {
                    Text("🐀").font(.system(size: 100))
                    Text("You found the Easter Egg!")
                        .font(.title.bold())
                        .foregroundStyle(.white)
                    Text("Congratulations, you sneaky little rat!")
                        .foregroundStyle(.gray)
                        .multilineTextAlignment(.center)
                }
                .padding()
            }
            .navigationTitle("Secret!")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Close") { dismiss() }
                        .foregroundStyle(.white)
                }
            }
        }
    }
}
