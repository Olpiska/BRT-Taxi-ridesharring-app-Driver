# 🚕 BRT Taxi - Driver App

The **BRT Taxi Driver App** is the companion Android application for the BRT Taxi ecosystem, built to empower drivers with real-time ride management, navigation, and earnings tracking.

---

## ✨ Features

- **Trip Management**  
  Accept, reject, and manage ride requests in real-time.

- **Live Navigation**  
  Integrated map services provide turn-by-turn navigation to pickup and destination points.

- **Driver Dashboard**  
  Track earnings, completed trips, and driver status in a clean interface.

- **Real-Time Updates**  
  Instant ride updates powered by backend synchronization.

- **Secure Platform**  
  Authentication and secure data handling ensure a safe driver experience.

---

## 🧠 Architecture Overview

- **MVVM Architecture**
- Separation of concerns (UI / Domain / Data)
- Lifecycle-aware components
- Scalable and maintainable structure

---

## 🌍 API & Backend Integration

- RESTful API communication
- JSON-based data exchange
- Authentication via token/session system
- Real-time updates (Firebase / WebSocket optional)

> ⚠️ Make sure to configure your API base URL inside the project before running.

---

## 📍 Maps & Location Services

- Google Maps SDK integration
- GPS-based real-time tracking
- Route optimization & navigation
- Location permission handling

---

## ⚙️ Setup Instructions

1. **Open in Android Studio**  
   Open the project in the latest stable version of Android Studio.

2. **Configure Environment**
   - Add your API base URL
   - Insert Google Maps API key in `local.properties` or `AndroidManifest.xml`

3. **Sync Gradle**
   ```bash
   ./gradlew build
   ```

4. **Run Application**
   - Connect a physical Android device (USB debugging enabled), or
   - Use an Android Emulator

---

## 💻 Development Stack

- **Android Platform**
- **Kotlin / Java**
- **Gradle Build System**
- **Google Maps SDK**
- **REST APIs / Firebase (optional)**

---

## 🔐 Environment Variables

Example configuration:

```
API_BASE_URL=https://api.example.com/
MAPS_API_KEY=your_google_maps_key
```

---

## 🧪 Testing

```bash
./gradlew test
./gradlew connectedAndroidTest
```

---

## 🤝 Contributing

To maintain high code quality:

```bash
./gradlew lint
./gradlew build
```

Guidelines:
- Follow **MVVM architecture**
- Write clean, readable, and modular code
- Use meaningful commit messages
- Test features before submitting PRs

---

## 📄 License

This project is licensed under the MIT License.
