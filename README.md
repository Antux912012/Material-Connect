# Material KDE Connect (Material 3 Expressive Edition)

// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

Material KDE Connect is an Android client for the KDE Connect and GSConnect cross-device protocol, built with Kotlin and Jetpack Compose Material 3 Expressive design. The application links Android devices with Linux, Windows, and macOS systems over local Wi-Fi and Bluetooth using mutually authenticated TLS sockets.

---

## Key Features

* **Material 3 Expressive Interface**: Uses Android 12+ dynamic wallpaper color extraction, Breeze-Expressive fallback tonal palettes, spring-physics shape morphing (`28.dp` to `16.dp` corner radius transitions), and a Bento grid for device plugin actions.
* **Full Protocol v8 Compatibility**: Connects with `kdeconnect-kde` (KDE Plasma, Windows, macOS) and `GSConnect` (GNOME Shell) without protocol modifications.
* **Mobile SoC Hardware Optimization**: Profiles Qualcomm Snapdragon, MediaTek Dimensity/Helio, NVIDIA Tegra, Unisoc Tiger, Google Tensor, and Samsung Exynos processors to tune payload socket buffers (`128 KB` to `512 KB`) and worker thread pools.
* **Predictive Motion Filtering**: Applies a zero-allocation 1D Kalman and exponential velocity lead filter to touchpad and gyroscope laser-pointer events to reduce latency on high-refresh-rate displays.
* **Comprehensive Plugin Suite**: Supports file and URL sharing, clipboard synchronization, multimedia remote control (MPRIS), remote touchpad and keyboard input, presentation pointer, shell command execution, SMS/MMS synchronization, and SFTP storage browsing.

---

## System Architecture

```text
+-------------------------------------------------------------------------+
| UI Presentation Layer (Jetpack Compose Material 3 Expressive)           |
| [PairingScreen]  [PluginsScreen]  [DevicePairingScreen]  [Buttons/Cards]|
+------------------------------------+------------------------------------+
                                     | StateFlow / UI Actions
                                     v
+-------------------------------------------------------------------------+
| Domain & Plugin Layer                                                   |
| [PairingViewModel]   [DeviceViewModel]   [Device]   [Plugin Registry]   |
+------------------------------------+------------------------------------+
                                     |
        +----------------------------+----------------------------+
        |                                                         |
        v                                                         v
+---------------------------------------+   +-----------------------------+
| Hardware & SoC Optimization Layer     |   | Transport & TLS Layer       |
| [SocHardwareOptimizer]                |   | [LanLinkProvider / LanLink] |
| - Vendor Detection (QCOM/MTK/NV/UNISOC|   | - Chunked BoundedLineReader |
| - 1D Kalman Touch/Gyro Motion Filter  |   | - Dynamic Socket Buffers    |
| - Elastic SynchronousQueue ThreadPool |   | - Mutual TLS Handshake      |
+---------------------------------------+   +-----------------------------+
```

For the full multi-agent architectural specification and Elephant-Goldfish (EGM) audit report, read [ARCHITECTURE_M3_EXPRESSIVE.md](ARCHITECTURE_M3_EXPRESSIVE.md).

---

## Prerequisites & Dependencies

* **Java Development Kit (JDK)**: JDK 17 or JDK 21 (configured to compile with JVM target 11 and Android core library desugaring).
* **Android SDK**: Android SDK Platform 37 (`compileSdk = 37`, `targetSdk = 37`, `minSdk = 23`).
* **Build System**: Gradle Wrapper (`./gradlew`) with Android Gradle Plugin `9.5.1` and Kotlin `2.4.21`.

---

## Build & Installation Instructions

Run the following commands from the repository root:

```bash
# 1. Run hermetic unit tests (BoundedLineReader, SocHardwareOptimizer, NetworkPacket, Plugins)
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/Android/Sdk
./gradlew testDebugUnitTest

# 2. Compile the Debug APK
./gradlew assembleDebug

# 3. Install the Debug APK onto a connected Android device or emulator
adb install -r build/outputs/apk/debug/kdeconnect-android-debug.apk
```

---

## Configuration & Usage

1. Connect the Android device and the desktop computer (`kdeconnect-kde` or `GSConnect`) to the same local network.
2. Launch **KDE Connect** on Android. Grant the **Local Network** and **Notifications** permissions using the Material 3 Expressive permission banners at the top of the pairing screen.
3. Select the discovered desktop device from the **Available devices** section and tap **Request pairing**.
4. Verify that the 6-digit hexadecimal SHA-256 verification key matches on both screens and tap **Accept**.

---

## Privacy, Compliance & License

* **GDPR & Local-First Processing**: All device discovery, pairing certificates, clipboard payloads, and file transfers execute locally over encrypted LAN or Bluetooth sockets. Zero personal data is transmitted to external servers.
* **Vulnerability Disclosure**: See [SECURITY.md](SECURITY.md) for EU Cyber Resilience Act (CRA) reporting procedures.
* **License**: Distributed under the GNU General Public License v2.0 or v3.0 (`GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL`). See [COPYING](COPYING) and [LICENSES](LICENSES) for full license texts.
