# Notifii (Notification Alarm Engine) 🔔⚡

> **"Never miss the text that makes you money. 100% Offline. Zero Tracking."**

Notifii is a privacy-first, offline Android application that turns specific, high-priority notifications from any messaging or utility app into persistent, unignorable alarms.

Built specifically for freelancers (Upwork, Fiverr), gig delivery drivers (DoorDash, UberEats, Amazon Flex), day traders (TradingView, Telegram), on-call sysadmins (PagerDuty, Slack, Sentry), and VIP emergency contacts.

---

## 🛡️ Privacy Guarantee

* **NO `android.permission.INTERNET`:** The AndroidManifest does not declare network permissions. Android OS cryptographically prevents this app from sending or receiving data over the internet.
* **Zero User Accounts:** No sign-up, no login, no cloud database.
* **100% On-Device:** All rules, notification logs, and settings are stored locally in an encrypted Room SQLite database.

---

## ✨ Key Features & Workflows

1. **Persistent Alarm Player:** Loops alarm sound on `STREAM_ALARM`, bypassing silent and Do Not Disturb (DND) modes until explicitly dismissed.
2. **Behavioral Burst Trigger:** "If Contact X texts $\ge 3$ times in 2 minutes $\rightarrow$ Ring Siren." Perfect for separating real emergencies from casual pings.
3. **Screen State Awareness:** If screen is ON (you're using the phone), displays a non-intrusive floating heads-up banner. If screen is OFF (locked/nightstand), blares the full persistent alarm.
4. **Natural-Language Rule Builder:** Intuitive conditional filter: *"When [WhatsApp] sends a message from [Boss] containing [urgent] then [play Siren]"*.
5. **Inline Direct Reply HUD:** Reply to WhatsApp, Slack, or Telegram directly from the alarm screen using Android's `RemoteInput` API without ever opening the target app.
6. **Text-to-Speech Announcer:** Reads out the notification aloud (*"Urgent message from Alex: Can we hop on a call?"*).
7. **Flip-to-Silence & Shake-to-Snooze:** Hardware accelerometer sensor integration to silence alarms without looking at the screen.
8. **Deleted Message Recovery Vault:** Logs incoming notifications the millisecond they arrive; retains original message text even if the sender later unsends/deletes it.
9. **Regex Value Filters:** Extract dollar values or error codes (e.g., only alarm if DoorDash offer is `> $20`).

---

## 🏗️ Architecture & Tech Stack

* **Language:** Kotlin
* **UI Toolkit:** Jetpack Compose with Material 3 (Material You dynamic theming, dark-mode first)
* **Architecture:** Clean Architecture + MVVM (Model-View-ViewModel)
* **Dependency Injection:** Hilt (Dagger-Hilt)
* **Database:** Room (SQLite) with Flow reactivity
* **Background Processing:** `NotificationListenerService` + Foreground Alarm Service
* **Hardware Sensors:** Accelerometer (`SensorEventListener`) for flip-to-silence
* **Knowledge Graph:** Graphify integration (`graphify-out/`)

---

## 📂 Project Structure

```
notifi/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml          # Declares all services, receivers; NO INTERNET
│   │   └── java/com/notifii/app/
│   │       ├── data/                    # Room DB, Entities, DAOs, Repositories
│   │       ├── di/                      # Hilt DI modules (AppModule, RepositoryModule)
│   │       ├── domain/                  # Use Cases (EvaluateNotification, ManageRules), Models
│   │       ├── receiver/                # BootReceiver, AlarmDismissReceiver
│   │       ├── service/                 # NotifiiListenerService, AlarmService
│   │       ├── ui/                      # Jetpack Compose screens, components, theme, navigation
│   │       └── util/                    # BurstDetector, FlipToSilence, NotificationParser, TTS
│   └── build.gradle.kts
├── docs/                                # Complete specifications & architecture documents
│   ├── prd.md                           # Product Requirements Document
│   ├── technical_architecture.md        # Technical Architecture & System Diagrams
│   ├── ui_ux_spec.md                    # UI/UX Design System & Screen Specifications
│   ├── api_reference.md                 # Internal API & Interface Contracts
│   ├── implementation_roadmap.md        # 5-Phase Development Roadmap
│   └── play_store_listing.md            # Google Play Store Draft Copy & Assets
├── graphify-out/                        # Graphify Knowledge Graph
├── gradle/                              # Version catalog (libs.versions.toml) & wrapper
└── build.gradle.kts
```

---

## 📖 Detailed Documentation

* [Product Requirements Document (PRD)](docs/prd.md)
* [Technical Architecture Document](docs/technical_architecture.md)
* [UI/UX Specification & Design Guide](docs/ui_ux_spec.md)
* [Internal API Reference](docs/api_reference.md)
* [Implementation Roadmap](docs/implementation_roadmap.md)
* [Google Play Store Listing Draft](docs/play_store_listing.md)

---

## 🚀 Setup & Build

1. Open project in **Android Studio Ladybug / Meerkat** (or newer).
2. Ensure JDK 17 or JDK 21 is configured.
3. Sync Gradle and build the project:
   ```bash
   ./gradlew assembleDebug
   ```
4. Deploy to device and grant **Notification Listener Permission** during the onboarding wizard.
