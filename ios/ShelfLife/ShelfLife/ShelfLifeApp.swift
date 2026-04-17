import SwiftUI
import FirebaseCore
import GoogleSignIn

@main
struct ShelfLifeApp: App {
    @StateObject private var authViewModel = AuthViewModel()
    @State private var colorScheme: ColorScheme? = nil

    init() {
        FirebaseApp.configure()
        if let saved = UserDefaults.standard.string(forKey: "colorScheme") {
            // Loaded by ProfileViewModel later; set on app start for immediate effect
        }
    }

    var body: some Scene {
        WindowGroup {
            Group {
                if authViewModel.isAuthenticated {
                    MainTabView()
                        .environmentObject(authViewModel)
                } else {
                    SignInView()
                        .environmentObject(authViewModel)
                }
            }
            .preferredColorScheme(resolvedColorScheme)
            .onOpenURL { url in
                GIDSignIn.sharedInstance.handle(url)
            }
        }
    }

    private var resolvedColorScheme: ColorScheme? {
        guard let saved = UserDefaults.standard.string(forKey: "colorScheme") else { return nil }
        return saved == "dark" ? .dark : saved == "light" ? .light : nil
    }
}
