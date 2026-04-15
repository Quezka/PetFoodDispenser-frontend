# Pet Food Dispenser Controller

An Android application built with Jetpack Compose to remotely control and monitor an automated pet food dispenser.

## 📱 Features

- **Real-time Monitoring**: Fetch current status from the dispenser hardware.
- **Dual Mode Control**:
    - **Local Mode**: Manual physical control on the device.
    - **Remote Mode**: Full control via the app.
- **Precision Feeding**: Individual sliders for three different food selectors (CR1, CR2, CR3).
- **Setup Wizard**: Easy initial configuration for the dispenser's IP address.
- **Debug Interface**: View raw JSON responses and detailed state information for troubleshooting.

## 🛠 Tech Stack

- **UI**: [Jetpack Compose](https://developer.android.com/jetpack/compose) (Material 3)
- **Architecture**: MVVM with [Hilt](https://developer.android.com/training/dependency-injection/hilt-android) for Dependency Injection
- **Asynchronous Work**: Kotlin Coroutines & Flow
- **Networking**: [OkHttp](https://square.github.io/okhttp/)
- **Serialization**: [Gson](https://github.com/google/gson)
- **Storage**: [Jetpack DataStore](https://developer.android.com/topic/libraries/architecture/datastore) (Preferences) for settings
- **Minimum SDK**: 31 (Android 12)

## 🚀 Getting Started

### Prerequisites

- Android Studio Ladybug or newer.
- An ESP8266/ESP32 or similar hardware running the PetFoodDispenser firmware on your local network.

### Setup

1. **Clone the repository**:
   ```bash
   git clone https://github.com/yourusername/PetFoodDispenser-frontend.git
   ```
2. **Open in Android Studio**.
3. **Build and Run** on your device.
4. **Configure IP**: On first launch, the app will prompt you for the Server IP Address. Enter the IP of your dispenser (e.g., `192.168.1.50`).

## 📡 API Integration

The app communicates with the dispenser via a simple HTTP API:

- `GET /get`: Returns a JSON object representing the `DispenserState`.
- `GET /set?variable=value`: Updates parameters (e.g., `mode`, `cr1_r`, `cr2_r`, `cr3_r`).

## 📦 Building for Release

To generate a signed APK for distribution:

1. Go to **Build > Generate Signed Bundle / APK...**
2. Follow the wizard to create/use a keystore.
3. The release build includes R8 minification and resource shrinking to ensure a small footprint.

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.
