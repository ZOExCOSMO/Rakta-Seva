# 🩸 Rakta-Seva Connect

> Saving lives, one alert at a time — A real-time blood donor emergency app for India

![Platform](https://img.shields.io/badge/Platform-Android-green) ![Kotlin](https://img.shields.io/badge/Kotlin-2.0-purple) ![Firebase](https://img.shields.io/badge/Firebase-Firestore%20%7C%20FCM%20%7C%20Auth-orange) ![Claude AI](https://img.shields.io/badge/AI-Claude%20Sonnet-blue)

---

## 📱 Features

- 🚨 **SOS Emergency Button** — one tap sends your location and alerts nearby donors
- 🔔 **FCM Push Notifications** — emergency alerts delivered in under 5 seconds
- 🤖 **Rakta AI Chatbot** — powered by Claude, answers donation eligibility, diet, compatibility
- 🪪 **Donor ID Card** — verified digital identity with QR code and barcode
- 🗺️ **Google Maps Navigation** — live route to hospital on acceptance
- 📊 **AI Urgency Scoring** — ranks blood requests by criticality
- 🔐 **Phone OTP Login** — Firebase Authentication
- ✅ **90-day Cooldown** — auto-enforced eligibility window

---

## 🛠️ Tech Stack

| Layer | Technology |
|---|---|
| Android UI | Jetpack Compose + Material 3 |
| Language | Kotlin |
| Auth | Firebase Phone OTP |
| Database | Cloud Firestore |
| Notifications | Firebase Cloud Messaging |
| AI Chatbot | Anthropic Claude Sonnet |
| Maps | Google Maps SDK + Maps Compose |
| Backend | Node.js + Express |
| Hosting | Render / Railway |
| DI | Hilt |

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Meerkat (2024.3.1) or later
- Node.js 20.x
- Firebase account
- Anthropic API key
- Google Cloud account (Maps API)

---

### 1. Clone the repo

```bash
git clone https://github.com/ZOExCOSMO/Rakta-Seva.git
cd Rakta-Seva
```

### 2. Firebase setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Create a project named `RaktaSeva`
3. Enable **Phone Authentication**, **Firestore**, and **Cloud Messaging**
4. Add an Android app with package name `com.raktaseva.connect`
5. Download `google-services.json` and place it in `app/`

### 3. Google Maps API key

1. Go to [Google Cloud Console](https://console.cloud.google.com)
2. Enable **Maps SDK for Android**
3. Create an API key
4. Add to `local.properties` (create this file in the project root):

```
MAPS_API_KEY=your_maps_api_key_here
```

### 4. Backend setup

```bash
cd rakta-seva-backend
npm install
```

Create `.env` in `rakta-seva-backend/`:

```
ANTHROPIC_API_KEY=your_anthropic_key_here
PORT=3000
```

Get your Firebase service account key:
1. Firebase Console → Project Settings → Service Accounts
2. Click **Generate new private key**
3. Save as `serviceAccountKey.json` inside `rakta-seva-backend/`

Run the backend:

```bash
node server.js
# Rakta-Seva backend on port 3000
```

### 5. Update the backend URL

Open `app/src/main/java/com/raktaseva/connect/utils/Constants.kt`:

```kotlin
const val BASE_URL = "https://your-deployed-backend-url/"
```

### 6. Build and run

Open the project in Android Studio, connect a device or start an emulator, and press ▶ Run.

---

## 📁 Project Structure

```
RaktaSeva/
├── app/src/main/java/com/raktaseva/connect/
│   ├── data/          # Models, repositories, API
│   ├── di/            # Hilt dependency injection
│   ├── service/       # FCM notification service
│   ├── ui/            # Compose screens
│   └── utils/         # Constants, location helpers
├── rakta-seva-backend/
│   └── server.js      # Express API (chat + FCM notify)
└── rakta_seva_v5_dark_id.html  # Interactive UI preview
```

---

## 🔑 Required Secret Files (not in repo)

| File | Where to get it |
|---|---|
| `app/google-services.json` | Firebase Console → Project Settings |
| `local.properties` | Create manually with your Maps API key |
| `rakta-seva-backend/.env` | Create manually with your Anthropic key |
| `rakta-seva-backend/serviceAccountKey.json` | Firebase Console → Service Accounts |


---

Built with ❤️ for blood donors across India 🇮🇳