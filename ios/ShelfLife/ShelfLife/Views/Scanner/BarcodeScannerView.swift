import SwiftUI
import AVFoundation

struct BarcodeScannerView: View {
    @StateObject private var vm = BarcodeScannerViewModel()
    @State private var showingAddItem = false
    @State private var selectedFoodFacts: FoodFacts?

    var body: some View {
        NavigationStack {
            Group {
                if !vm.permissionGranted {
                    cameraPermissionView
                } else if vm.isLoading {
                    loadingView
                } else if !vm.scannedProducts.isEmpty {
                    productSelectionView
                } else {
                    scannerView
                }
            }
            .navigationTitle("Scanner")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                if !vm.scannedProducts.isEmpty {
                    ToolbarItem(placement: .topBarLeading) {
                        Button("Back") { vm.reset() }
                    }
                }
                ToolbarItem(placement: .topBarTrailing) {
                    Button {
                        vm.showManualSearch.toggle()
                    } label: {
                        Image(systemName: "magnifyingglass")
                    }
                }
            }
            .sheet(isPresented: $vm.showManualSearch) {
                manualSearchSheet
            }
            .sheet(isPresented: $showingAddItem) {
                if let facts = selectedFoodFacts {
                    AddFoodItemView(prefillFoodFacts: facts)
                }
            }
        }
    }

    private var scannerView: some View {
        ZStack {
            CameraPreview(onBarcode: { barcode in
                Task { await vm.handleScannedBarcode(barcode) }
            })
            .ignoresSafeArea()

            VStack {
                Spacer()

                RoundedRectangle(cornerRadius: 12)
                    .stroke(.white, lineWidth: 3)
                    .frame(width: 280, height: 120)
                    .overlay(
                        Text("Point camera at barcode")
                            .font(.caption)
                            .foregroundStyle(.white)
                            .padding(.top, 130)
                    )

                Spacer()

                if let error = vm.errorMessage {
                    Text(error)
                        .font(.subheadline)
                        .foregroundStyle(.white)
                        .padding()
                        .background(.black.opacity(0.6))
                        .clipShape(RoundedRectangle(cornerRadius: 8))
                        .padding(.bottom, 20)
                }
            }
        }
    }

    private var productSelectionView: some View {
        List {
            Section("Select a product") {
                ForEach(vm.scannedProducts) { product in
                    ProductRow(product: product) {
                        selectedFoodFacts = product
                        showingAddItem = true
                        vm.scannedProducts = []
                    }
                }
            }
        }
    }

    private var loadingView: some View {
        VStack(spacing: 16) {
            ProgressView()
            Text("Looking up product...")
                .foregroundStyle(.secondary)
        }
    }

    private var cameraPermissionView: some View {
        VStack(spacing: 20) {
            Image(systemName: "camera.fill")
                .font(.system(size: 60))
                .foregroundStyle(.secondary)
            Text("Camera Access Required")
                .font(.title2.bold())
            Text("ShelfLife needs camera access to scan barcodes.")
                .foregroundStyle(.secondary)
                .multilineTextAlignment(.center)
            Button("Grant Permission") {
                vm.checkCameraPermission()
            }
            .buttonStyle(.borderedProminent)
            .tint(.green)

            Button("Open Settings") {
                if let url = URL(string: UIApplication.openSettingsURLString) {
                    UIApplication.shared.open(url)
                }
            }
            .foregroundStyle(.blue)
        }
        .padding()
    }

    private var manualSearchSheet: some View {
        NavigationStack {
            VStack(spacing: 16) {
                HStack {
                    Image(systemName: "magnifyingglass").foregroundStyle(.secondary)
                    TextField("Search by product name...", text: $vm.searchText)
                        .submitLabel(.search)
                        .onSubmit {
                            Task { await vm.searchByName(vm.searchText) }
                        }
                }
                .padding(10)
                .background(Color(.systemGray6))
                .clipShape(RoundedRectangle(cornerRadius: 10))
                .padding(.horizontal)

                Button("Search") {
                    Task {
                        await vm.searchByName(vm.searchText)
                        vm.showManualSearch = false
                    }
                }
                .buttonStyle(.borderedProminent)
                .tint(.green)
                .disabled(vm.searchText.isEmpty)

                Spacer()
            }
            .padding(.top)
            .navigationTitle("Search Products")
            .navigationBarTitleDisplayMode(.inline)
            .toolbar {
                ToolbarItem(placement: .cancellationAction) {
                    Button("Cancel") { vm.showManualSearch = false }
                }
            }
        }
        .presentationDetents([.medium])
    }
}

struct ProductRow: View {
    let product: FoodFacts
    let onSelect: () -> Void

    var body: some View {
        Button(action: onSelect) {
            HStack(spacing: 12) {
                AsyncImage(url: URL(string: product.imageUrl)) { img in
                    img.resizable().scaledToFill()
                } placeholder: {
                    Color(.systemGray5)
                    Text(product.category.icon)
                }
                .frame(width: 56, height: 56)
                .clipShape(RoundedRectangle(cornerRadius: 8))

                VStack(alignment: .leading, spacing: 4) {
                    Text(product.name).font(.subheadline.bold()).foregroundStyle(.primary)
                    Text(product.category.displayName).font(.caption).foregroundStyle(.secondary)
                    if product.quantity.amount > 0 {
                        Text(product.quantity.displayString).font(.caption).foregroundStyle(.secondary)
                    }
                }

                Spacer()
                Image(systemName: "plus.circle.fill").foregroundStyle(.green)
            }
        }
        .buttonStyle(.plain)
    }
}

// MARK: - Camera Preview (AVFoundation)
struct CameraPreview: UIViewRepresentable {
    let onBarcode: (String) -> Void

    func makeUIView(context: Context) -> CameraPreviewUIView {
        let view = CameraPreviewUIView()
        view.onBarcode = onBarcode
        return view
    }

    func updateUIView(_ uiView: CameraPreviewUIView, context: Context) {}
}

class CameraPreviewUIView: UIView, AVCaptureMetadataOutputObjectsDelegate {
    var onBarcode: ((String) -> Void)?
    private var captureSession: AVCaptureSession?
    private var previewLayer: AVCaptureVideoPreviewLayer?
    private var lastScannedCode: String?
    private var lastScanTime: Date = .distantPast

    override func layoutSubviews() {
        super.layoutSubviews()
        previewLayer?.frame = bounds
        if captureSession == nil { setupSession() }
    }

    private func setupSession() {
        let session = AVCaptureSession()
        captureSession = session

        guard let device = AVCaptureDevice.default(for: .video),
              let input = try? AVCaptureDeviceInput(device: device) else { return }

        session.addInput(input)

        let output = AVCaptureMetadataOutput()
        session.addOutput(output)
        output.setMetadataObjectsDelegate(self, queue: .main)
        output.metadataObjectTypes = [.ean8, .ean13, .upce, .qr, .code128, .code39]

        let layer = AVCaptureVideoPreviewLayer(session: session)
        layer.frame = bounds
        layer.videoGravity = .resizeAspectFill
        self.layer.addSublayer(layer)
        previewLayer = layer

        DispatchQueue.global(qos: .userInitiated).async {
            session.startRunning()
        }
    }

    func metadataOutput(_ output: AVCaptureMetadataOutput, didOutput metadataObjects: [AVMetadataObject], from connection: AVCaptureConnection) {
        guard let object = metadataObjects.first as? AVMetadataMachineReadableCodeObject,
              let code = object.stringValue else { return }

        let now = Date()
        guard code != lastScannedCode || now.timeIntervalSince(lastScanTime) > 3 else { return }

        lastScannedCode = code
        lastScanTime = now
        onBarcode?(code)
    }

    override func removeFromSuperview() {
        captureSession?.stopRunning()
        super.removeFromSuperview()
    }
}
