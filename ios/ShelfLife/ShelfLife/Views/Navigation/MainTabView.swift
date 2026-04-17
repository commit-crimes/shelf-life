import SwiftUI

struct MainTabView: View {
    @EnvironmentObject var authViewModel: AuthViewModel
    @StateObject private var overviewVM = OverviewViewModel()
    @StateObject private var recipesVM = RecipesViewModel()
    @StateObject private var profileVM = ProfileViewModel()

    var body: some View {
        TabView {
            OverviewView()
                .environmentObject(overviewVM)
                .tabItem {
                    Label("Inventory", systemImage: "list.bullet")
                }

            BarcodeScannerView()
                .tabItem {
                    Label("Scanner", systemImage: "barcode.viewfinder")
                }

            RecipesView()
                .environmentObject(recipesVM)
                .tabItem {
                    Label("Recipes", systemImage: "book.fill")
                }

            ProfileView()
                .environmentObject(profileVM)
                .tabItem {
                    Label("Profile", systemImage: "person.fill")
                }
        }
        .tint(.green)
    }
}
