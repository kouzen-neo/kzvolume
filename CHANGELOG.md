# Changelog

All notable changes to kzvolume are documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.1.0/), and this project adheres to [Semantic Versioning](https://semver.org/spec/v2.0.0.html).

## [v1.4.0] - 2026-10-22

### Added

- **Dedicated Floating Power Button**:
  - Independent movable on-screen power button with separate portrait and landscape coordinates.
  - Single tap triggers instant screen lock via `GLOBAL_ACTION_LOCK_SCREEN`.
  - Hold or double-tap opens the system power/restart/shutdown dialog via `GLOBAL_ACTION_POWER_DIALOG`.
  - Integrated `KZVolumeAccessibilityService` for secure, root-free system actions.
  - Interactive status banner with one-tap navigation to Accessibility Settings.
- **Multi-ABI APK Splits**:
  - Automated release builds producing `arm64-v8a`, `armeabi-v7a`, and `universal` APK packages.

## [v1.3.0] - 2026-10-22

### Added

- **Initial Release of kzvolume**:
  - Restores effortless system volume controls on Android devices without relying on physical hardware keys.
  - Interactive live media volume slider with percentage readout and instant mute toggle.
  - Movable on-screen Floating Button with custom icons, colors, opacity, size (36dp - 64dp), and orientation persistence.
  - Edge Swipe gesture trigger customizable for left or right screen edge.
  - Native Android Quick Settings volume panel tile.
  - Status bar and lock screen notification media volume actions (`Vol -`, `Mute`, `Vol +`).
  - Native Inter font typography with Material 3 styling.
  - Configurable haptic feedback and safe insets handling across all screens.
