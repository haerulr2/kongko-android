# Kongko Android Native

Aplikasi chat dan VoIP calling modern untuk Android, dibangun menggunakan 100% Kotlin dan Jetpack Compose.

## 🚀 Fitur Utama

- **Arsitektur Modern**: Clean Architecture (Modular/Feature-first) + MVVM + Jetpack Compose.
- **Offline-First Chat**: Room Database + Flow untuk streaming pesan offline & sinkronisasi otomatis.
- **Realtime Messaging**: WebSocket client (OkHttp) dengan reconnection otomatis, indikator mengetik, status terkirim & terbaca.
- **VoIP Voice & Video Calling**:
  - Didukung oleh official Agora RTC Native SDK (`io.agora.rtc:full-sdk`).
  - Android **Foreground Service** (`phoneCall|microphone`) untuk menjaga panggilan tetap hidup saat aplikasi di latar belakang atau layar terkunci.
  - **Incoming Call Activity** instan yang menyala di atas layar terkunci (*Turn Screen On & Show When Locked*) tanpa delay.
- **Sinkronisasi Kontak**: Menggunakan Android `ContactsContract.CommonDataKinds.Phone` langsung untuk deteksi teman pengguna Kongko.
- **Modern Theme System**: Material 3 dengan palet khas Kongko (*Warm Teal* & *Raspberry Accent*).

## 🛠️ Tech Stack

- **UI**: Jetpack Compose & Material 3
- **Dependency Injection**: Dagger Hilt
- **Local Database**: Room Database
- **Session & Storage**: Jetpack DataStore Preferences
- **Networking**: Retrofit 2 + Kotlinx Serialization + OkHttp
- **Push Notification**: Firebase Cloud Messaging (FCM)
- **Calling Engine**: Agora RTC Engine Native Android SDK
- **Image Loading**: Coil Compose

## ⚙️ Persyaratan Sistem

- Android 10+ (`minSdk = 29`)
- Target Android 15 (`targetSdk = 35`, `compileSdk = 35`)
- JDK 17 (Temurin / Adoptium)

## 📦 Menjalankan Project

```bash
# Clone repository
git clone https://github.com/haerulr2/kongko-android.git
cd kongko-android

# Build Debug APK
./gradlew assembleDebug

# Install ke perangkat/emulator
./gradlew installDebug
```
