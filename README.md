# kzvolume

[![GitHub Release](https://img.shields.io/github/v/release/kouzen-neo/kzvolume?style=for-the-badge&logo=github)](https://github.com/kouzen-neo/kzvolume/releases/latest)
[![License](https://img.shields.io/github/license/kouzen-neo/kzvolume?style=for-the-badge&logo=apache)](LICENSE)

kzvolume is a lightweight Android utility designed to restore on-screen volume controls on devices with broken, worn out, or unresponsive physical volume buttons.

Provides system volume access via an on-screen Floating Button, Edge Swipe gestures, persistent Notification Actions, and a Quick Settings Tile.

---

## Features

- **Floating Button**: On-screen overlay button with configurable size (36dp to 64dp), opacity, custom icons, and independent portrait and landscape position memory.
- **Edge Swipe**: Trigger native volume dialog with an inward swipe from the left or right edge of the display.
- **Media Volume Card**: Live volume level indicator, slider, increment/decrement steps, and instant mute toggle.
- **Notification Controls**: Volume adjustment (Vol -, Mute, Vol +) directly from status bar and lock screen.
- **Quick Settings Tile**: Fast access tile located directly inside the notification shade.
- **Material 3 Design**: Clean interface styled with Inter typography and adaptive system colors.
- **Privacy First**: Zero trackers, no analytics, no network requests, and 100% open-source under Apache-2.0.

---

## Project Structure

```
kzvolume/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── kotlin/io/github/kouzen/kzvolume/
│   │   │   ├── core/
│   │   │   │   ├── block/         # Feature availability conditions & evaluation
│   │   │   │   ├── feature/       # Feature registration, specs, and controllers
│   │   │   │   ├── permission/    # Overlay and notification permission policy
│   │   │   │   └── preferences/   # Local persistent state via SharedPreferences
│   │   │   ├── features/
│   │   │   │   ├── edge/          # Edge swipe detection and gesture overlay service
│   │   │   │   ├── floating/      # Movable overlay button and config activity
│   │   │   │   └── quicktile/     # Quick Settings Tile implementation
│   │   │   ├── home/              # Main dashboard activity and UI adapters
│   │   │   ├── permission/        # Permission hub and onboarding activities
│   │   │   ├── BootReceiver.kt    # Automatic service restoration on reboot
│   │   │   ├── Constants.kt       # Global constants, keys, and identifiers
│   │   │   └── KZVolumeApp.kt     # Application class with Dynamic Colors
│   │   └── res/
│   │       ├── drawable/          # Vector drawables and UI assets
│   │       ├── font/              # Inter font family definitions
│   │       ├── layout/            # Activity and dialog layouts
│   │       ├── mipmap-*/          # Adaptive application launcher icons
│   │       └── values/            # Theme, color tokens, and string definitions
│   ├── build.gradle.kts           # Application build config and ABI splits
│   └── proguard-rules.pro         # Minification and R8 optimization rules
├── docs/
│   └── privacy-policy.md          # Privacy policy and permissions disclosure
├── fastlane/                      # App metadata and release descriptions
├── gradle/
│   └── libs.versions.toml         # Version catalog for dependencies and plugins
├── .github/workflows/
│   ├── debug-build.yml            # CI validation on pull requests
│   └── release-build.yml          # Automated multi-ABI APK build and GitHub release
├── CHANGELOG.md                   # Chronological project history
├── CONTRIBUTING.md                # Contribution workflow
├── LICENSE                        # Apache License 2.0
├── README.md                      # Project documentation
└── settings.gradle.kts            # Project settings and plugin repositories
```

---

## Downloads

Compiled APKs for various architectures are available under [GitHub Releases](https://github.com/kouzen-neo/kzvolume/releases/latest):

| File | Target Architecture |
|---|---|
| `kzvolume-arm64-v8a.apk` | Modern 64-bit ARM devices (recommended) |
| `kzvolume-armeabi-v7a.apk` | Legacy 32-bit ARM devices |
| `kzvolume-universal.apk` | Universal package supporting all platforms |

---

## Build Requirements

- Android SDK 35+ (build-tools 36.0.0+)
- JDK 17
- Gradle 9.x (via included wrapper `./gradlew`)

To build locally:

```bash
# Debug build
./gradlew assembleDebug

# Release build (splits)
./gradlew assembleRelease
```

---

## License

Licensed under the [Apache License 2.0](LICENSE).
