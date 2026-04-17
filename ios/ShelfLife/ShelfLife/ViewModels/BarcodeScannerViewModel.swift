import Foundation
import AVFoundation
import SwiftUI

@MainActor
class BarcodeScannerViewModel: ObservableObject {
    @Published var scannedProducts: [FoodFacts] = []
    @Published var isScanning = false
    @Published var isLoading = false
    @Published var errorMessage: String?
    @Published var selectedProduct: FoodFacts?
    @Published var permissionGranted = false
    @Published var searchText = ""
    @Published var showManualSearch = false

    private let offService = OpenFoodFactsService.shared

    init() {
        checkCameraPermission()
    }

    func checkCameraPermission() {
        switch AVCaptureDevice.authorizationStatus(for: .video) {
        case .authorized:
            permissionGranted = true
        case .notDetermined:
            AVCaptureDevice.requestAccess(for: .video) { [weak self] granted in
                Task { @MainActor in
                    self?.permissionGranted = granted
                }
            }
        default:
            permissionGranted = false
        }
    }

    func handleScannedBarcode(_ barcode: String) async {
        guard !isLoading else { return }
        isScanning = false
        isLoading = true
        errorMessage = nil

        do {
            let results = try await offService.searchByBarcode(barcode)
            scannedProducts = results
            if results.isEmpty {
                errorMessage = "No product found for barcode: \(barcode)"
            }
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func searchByName(_ query: String) async {
        guard !query.isEmpty else { return }
        isLoading = true
        errorMessage = nil

        do {
            scannedProducts = try await offService.searchByQuery(query)
            if scannedProducts.isEmpty {
                errorMessage = "No products found for '\(query)'"
            }
        } catch {
            errorMessage = error.localizedDescription
        }

        isLoading = false
    }

    func selectProduct(_ product: FoodFacts) {
        selectedProduct = product
    }

    func reset() {
        scannedProducts = []
        selectedProduct = nil
        errorMessage = nil
        isScanning = true
        showManualSearch = false
        searchText = ""
    }
}
