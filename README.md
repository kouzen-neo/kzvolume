<div id="top" align="center">

# <img src="fastlane/metadata/android/en-US/images/icon.png" alt="kzvolume icon" width="48" height="48"> kzvolume

[![GitHub Release](https://img.shields.io/github/v/release/kouzen-neo/kzvolume?style=for-the-badge&logo=github)](https://github.com/kouzen-neo/kzvolume/releases/latest)
[![License](https://img.shields.io/github/license/kouzen-neo/kzvolume?style=for-the-badge&logo=apache)](LICENSE)

**kzvolume** is a lightweight Android utility designed to restore volume controls on devices with broken or unresponsive physical volume buttons.

Access volume controls anywhere via an on-screen **Floating Button**, **Edge Swipe Gestures**, **Notification Actions**, or a **Quick Settings Tile**.

</div>

---

## Features

- **🔘 Floating Button**: Movable on-screen trigger with custom icons, size (36dp–64dp), colors, opacity, and independent portrait/landscape memory.
- **↔️ Edge Swipe Gestures**: Swipe inward from your preferred screen edge (left or right) to trigger the system volume panel.
- **🔊 Live Volume Dashboard**: Interactive media volume card with slider, volume down/up, and instant mute toggle.
- **🔔 Notification Controls**: Adjust volume or toggle mute directly from your notification drawer and lock screen.
- **🚀 Quick Settings Tile**: Native tile for instant volume access from anywhere.
- **🎨 Material 3 & Inter Font**: Expressive design with typography matching the kzkt family.
- **🔒 Privacy-First**: 100% open-source, no analytics, no ads, no trackers, and zero network calls.

---

## Downloads

Download the latest APK for your device architecture from [GitHub Releases](https://github.com/kouzen-neo/kzvolume/releases/latest):
- `kzvolume-arm64-v8a.apk` (modern 64-bit devices)
- `kzvolume-armeabi-v7a.apk` (legacy 32-bit devices)
- `kzvolume-universal.apk` (all architectures)

---

## Tech Stack

- **Language**: Kotlin 2.1
- **Target SDK**: Android 15 (API 35+) / minSdk 29 (Android 10+)
- **Build System**: Gradle 9 with Version Catalogs (`libs.versions.toml`)
- **Optimization**: R8 Full Mode minification and resource shrinking

---

## License

Licensed under the [Apache License 2.0](LICENSE).
