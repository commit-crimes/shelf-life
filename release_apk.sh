#!/bin/bash

# Define the APK path and Gradle task
APK_PATH="app/build/outputs/apk/debug/app-debug.apk"
GRADLE_TASK="assembleDebug"

# Function to check if ADB and Gradle are installed
function check_dependencies() {
    if ! command -v adb &> /dev/null; then
        echo "ADB not found! Please install ADB."
        exit 1
    fi

    if ! command -v gradle &> /dev/null; then
        echo "Gradle not found! Please install Gradle."
        exit 1
    fi
}

# Function to build the APK using Gradle
function build_apk() {
    echo "Building APK..."
    ./gradlew $GRADLE_TASK
    if [ $? -ne 0 ]; then
        echo "Gradle build failed!"
        exit 1
    fi
}

# Function to check if the APK exists
function check_apk() {
    if [ ! -f "$APK_PATH" ]; then
        echo "APK not found at $APK_PATH. Make sure the build was successful."
        exit 1
    fi
}

# Function to install the APK on the connected Android device
function install_apk() {
    echo "Installing APK on the connected Android device..."
    adb devices
    adb install -r $APK_PATH
    if [ $? -eq 0 ]; then
        echo "APK installed successfully!"
    else
        echo "Failed to install APK!"
        exit 1
    fi
}

# Main script execution
check_dependencies
build_apk
check_apk
install_apk