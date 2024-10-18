figma link: https://www.figma.com/design/05FWkNY6XZhuBZN2ADIumq/Shelf-Life?m=auto&t=7fezM6lp3yu7CYM1-6

# ShelfLife

## Overview

ShelfLife is a mobile app designed to help users reduce food waste, save money, and plan meals more efficiently. It allows users to scan groceries, track expiration dates, and receive recipe suggestions based on the current inventory of their pantry and fridge. By integrating nutritional information from Open Food Facts, ShelfLife empowers individuals and families to make smarter food choices and reduce waste, whether online or offline.

## Features

### 1. Grocery Tracking & Expiration Date Monitoring
- Use the app's barcode scanning feature to easily add items to your inventory by scanning the product’s barcode.
- Automatically track expiration dates to ensure no food goes to waste.

### 2. Recipe Suggestions
- Get personalized recipe ideas based on the food you already have in your inventory.
- Plan meals efficiently by using ingredients that are about to expire.

### 3. Nutritional Information
- ShelfLife integrates with Open Food Facts to provide users with detailed nutritional information about the food items they scan.

### 4. Multi-User Support
- Each user can securely login with their Google account using Firebase Authentication.
- Multiple users within a household can share and manage a single inventory, ensuring everyone stays on the same page.

### 5. Offline Mode
- Manage your inventory and get recipe suggestions even when you're not connected to the internet.

## Architecture Diagram
![in docs/ArchitectureDiagram/ArchitectureDiagramPicture.png](https://github.com/commit-crimes/shelf-life/blob/main/docs/ArchitectureDiagram/ArchitectureDiagramPicture.png)

## Firebase Integration

ShelfLife uses several Google Firebase services to provide cloud functionalities:

- Firebase Authentication: Handles secure user log-ins with Google accounts, allowing personalized inventory across devices.
- Firestore: A real-time cloud database used for storing user inventories, expiration dates, and recipe preferences.
- Firebase Cloud Storage: Stores product images to enhance the user’s product library.
- Firebase Functions: Performs server-side tasks like generating recipe suggestions and sending notifications about soon-to-expire items.
- Firebase Analytics: Tracks user interactions to help improve the app based on usage data.

## Multi-User Collaboration

- Users can invite others to collaborate on a shared inventory, enabling multiple users in a household to manage groceries together.
- Each user has a unique profile and data, ensuring their preferences and saved recipes are kept separate, while still enabling collaboration when needed.

## Core Features Using the Camera Sensor

- Barcode Scanning: The app uses the phone's camera sensor to scan grocery barcodes, making it easy to add items to the inventory. The app pulls product data, such as expiration dates and nutritional information, from the Open Food Facts database.
- Convenient Inventory Management: With quick barcode scanning, users can effortlessly keep their inventory up to date.


## License

No current License.

---

ShelfLife is perfect for busy individuals and families who want to save time, money, and food by effectively managing their groceries and planning meals.
