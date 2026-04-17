# ShelfLife iOS App

A fully-featured iPhone app that mirrors all functionality of the ShelfLife Android app.

## Features

- **Barcode Scanning** — Scan grocery barcodes using AVFoundation; product data fetched from Open Food Facts
- **Inventory Management** — Track food items with expiry dates, categories, storage locations, and statuses
- **Recipe Management** — Browse, add, and execute recipes step-by-step
- **AI Recipe Generation** — Generate recipes from your pantry using OpenAI GPT-4o
- **Multi-Household Support** — Create/join households, collaborate with family members
- **Invitations** — Invite household members by email
- **Leaderboard** — RAT and STINKY points gamification per household
- **Google Sign-In** — Secure authentication via Firebase Auth
- **Offline-first** — Firestore local cache keeps the app usable offline
- **Dark/Light/System theme** — Configurable in Profile

## Requirements

- Xcode 15+
- iOS 16.0+
- macOS Ventura or later (for building)
- Active Firebase project (same as the Android app)

## Setup

### 1. Firebase Configuration

1. Open [Firebase Console](https://console.firebase.google.com/) and select your ShelfLife project
2. Go to **Project Settings → General → Your apps**
3. Click **Add app → Apple (iOS)**
4. Enter bundle ID: `com.shelflife.app`
5. Download `GoogleService-Info.plist`
6. Copy it into `ShelfLife/ShelfLife/` (next to `ShelfLifeApp.swift`)
   - The file `GoogleService-Info.plist.template` in `Resources/` shows the expected format

### 2. Google Sign-In

The `REVERSED_CLIENT_ID` from your `GoogleService-Info.plist` must be registered as a URL scheme.
This is already wired in `Info.plist` via `$(REVERSED_CLIENT_ID)` — Xcode reads it automatically.

### 3. OpenAI API Key

Set your OpenAI API key before generating recipes. The app reads it from:

```swift
// In ShelfLifeApp.swift init(), add:
OpenAIService.shared.apiKey = "sk-your-key-here"
```

For production, store it in a `.xcconfig` file (not committed) and read via `Bundle.main.infoDictionary`.

### 4. Open in Xcode

```bash
open ios/ShelfLife/ShelfLife.xcodeproj
```

Xcode will automatically resolve the Swift Package Manager dependencies:
- `firebase-ios-sdk` (FirebaseAuth, FirebaseFirestore, FirebaseStorage, FirebaseCore)
- `GoogleSignIn-iOS` (GoogleSignIn, GoogleSignInSwift)

Wait for package resolution to complete, then select a simulator or device and press **Run**.

### 5. Code Signing

In Xcode, select the **ShelfLife** target → **Signing & Capabilities**:
- Set **Team** to your Apple Developer account
- The **Bundle Identifier** is `com.shelflife.app` — change if needed

## Project Structure

```
ShelfLife/
├── ShelfLifeApp.swift          # App entry point, Firebase init
├── Models/
│   ├── FoodFacts.swift         # FoodFacts, Quantity, NutritionFacts, enums
│   ├── FoodItem.swift          # FoodItem with expiry tracking
│   ├── Recipe.swift            # Recipe, Ingredient, RecipePrompt
│   ├── Household.swift         # Household with points maps
│   ├── SLUser.swift            # App user model
│   └── Invitation.swift        # Household invitation
├── Services/
│   ├── AuthService.swift       # Firebase Auth + Google Sign-In
│   ├── UserService.swift       # Firestore user CRUD + real-time listener
│   ├── HouseholdService.swift  # Household CRUD + real-time listeners
│   ├── FoodItemService.swift   # Food item CRUD + Firestore listener
│   ├── RecipeService.swift     # Recipe CRUD + Firestore listener
│   ├── InvitationService.swift # Invitation send/accept/decline
│   ├── OpenFoodFactsService.swift  # Barcode + name search API
│   └── OpenAIService.swift     # GPT-4o recipe generation
├── ViewModels/                 # MVVM ObservableObject classes
└── Views/
    ├── Authentication/         # SignInView (Google Sign-In)
    ├── Navigation/             # MainTabView, HouseholdDrawerView
    ├── Overview/               # Inventory list, Add/Edit/Detail food item, Household creation
    ├── Scanner/                # AVFoundation barcode scanner + product search
    ├── Recipes/                # Recipe list, detail, add, generate (AI), execution
    ├── Profile/                # Profile, theme picker, easter egg
    ├── Invitations/            # Send & accept invitations
    └── Leaderboard/            # RAT/STINKY points leaderboard
```

## Architecture

- **MVVM** with `ObservableObject` / `@Published` / `@StateObject` / `@EnvironmentObject`
- **async/await** throughout for all async operations
- **Firestore real-time listeners** keep data in sync across devices
- **SwiftUI** navigation with `TabView` + `NavigationStack` + sheets/full-screen covers
- **Single-source-of-truth** via shared service singletons

## Firestore Structure

Identical to the Android app — the iOS and Android apps share the same Firebase backend:

```
users/{uid}
households/{householdId}
foodItems/{householdId}/items/{itemId}
recipes/{recipeId}
invitations/{invitationId}
```
