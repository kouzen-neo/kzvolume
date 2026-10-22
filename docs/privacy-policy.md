# Privacy Policy

Your privacy is paramount. **kzvolume** is designed with a strict privacy-first philosophy, ensuring that your data remains yours and yours alone.

---

## 1. Information Collection and Use

kzvolume does **not** collect, store, or transmit any personal information, usage data, or device-identifying information.
- **No Trackers:** Contains zero third-party tracking libraries, analytics, or telemetry.
- **No Ads:** The app is completely ad-free.
- **No Network Sync:** Settings and actions remain entirely on-device.

---

## 2. Permissions Explained

To provide its core functionality, kzvolume requests the following Android system permissions:

- **Display over other apps (`SYSTEM_ALERT_WINDOW`):** Required to display the floating volume button and detect edge swipe gestures over other applications.
- **Foreground Service (`FOREGROUND_SERVICE_SPECIAL_USE`):** Required to keep the overlay and gesture listener active and responsive in background.
- **Vibration (`VIBRATE`):** Used to deliver optional haptic feedback on taps and gestures.

---

## 3. Local Data Storage

All configurations (floating button position, size, opacity, active edge side, and haptic preferences) are stored strictly locally via Android SharedPreferences. This data is never shared and is cleared automatically upon uninstallation.

---

## 4. Open Source Transparency

kzvolume is 100% open-source under the Apache 2.0 license. You can audit the codebase and verify our privacy claims directly on GitHub.
