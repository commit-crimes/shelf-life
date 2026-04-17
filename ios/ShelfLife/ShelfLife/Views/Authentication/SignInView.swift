import SwiftUI
import GoogleSignInSwift

struct SignInView: View {
    @EnvironmentObject var authViewModel: AuthViewModel

    var body: some View {
        ZStack {
            Color(.systemBackground).ignoresSafeArea()

            VStack(spacing: 40) {
                Spacer()

                VStack(spacing: 12) {
                    Image(systemName: "leaf.fill")
                        .resizable()
                        .scaledToFit()
                        .frame(width: 80, height: 80)
                        .foregroundStyle(.green)

                    Text("ShelfLife")
                        .font(.largeTitle.bold())

                    Text("Reduce food waste.\nEat smarter.")
                        .font(.subheadline)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                }

                VStack(spacing: 16) {
                    Text("Track expiry dates, get recipe ideas,\nand manage your household groceries.")
                        .font(.body)
                        .foregroundStyle(.secondary)
                        .multilineTextAlignment(.center)
                        .padding(.horizontal, 32)
                }

                Spacer()

                VStack(spacing: 16) {
                    if authViewModel.isLoading {
                        ProgressView("Signing in...")
                            .padding()
                    } else {
                        GoogleSignInButton(scheme: .dark, style: .wide, state: .normal) {
                            Task {
                                guard let windowScene = UIApplication.shared.connectedScenes.first as? UIWindowScene,
                                      let vc = windowScene.windows.first?.rootViewController else { return }
                                await authViewModel.signInWithGoogle(presenting: vc)
                            }
                        }
                        .frame(height: 50)
                        .padding(.horizontal, 32)
                    }

                    if let error = authViewModel.errorMessage {
                        Text(error)
                            .font(.caption)
                            .foregroundStyle(.red)
                            .multilineTextAlignment(.center)
                            .padding(.horizontal)
                    }
                }

                Spacer().frame(height: 40)
            }
        }
    }
}
