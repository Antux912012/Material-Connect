# Material Connect

// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

**Material Connect** is a modern **Material 3 Expressive** Android client for the KDE Connect and GSConnect cross-device protocol, built with Kotlin and Jetpack Compose. It maintains full protocol v8 interoperability with Linux, GNOME, Windows, and macOS desktops while modernizing the user interface, concurrency model, and mobile SoC hardware execution paths.

---

## Acknowledgments & Upstream Attribution

This project stands on the shoulders of two foundational open-source ecosystems:

* **[KDE Connect](https://github.com/KDE/kdeconnect-kde)** (`kdeconnect-kde` & `kdeconnect-android`): Created by Albert Vaca Cintora and the KDE Community. KDE Connect established the secure, local-first cross-device packet protocol, mDNS/UDP discovery, mutual TLS pairing model, and plugin architecture that power this application.
* **[GSConnect](https://github.com/GSConnect/gnome-shell-extension-gsconnect)**: Maintained by the GNOME Shell extension contributors. GSConnect proved that the KDE Connect protocol scales cleanly across desktop environments through clean protocol-v8 interoperability.

**Material Connect** reimplements and modernizes the Android client experience using Material 3 Expressive design principles, hardware-aware socket buffering, and deterministic concurrency while preserving 100% compatibility with upstream `kdeconnect-kde` and `GSConnect` desktop hosts.

---

## Key Capabilities & Technical Innovations

### 1. Material 3 Expressive UI/UX System
* **Dynamic & Breeze-Expressive Color Hierarchy** (`KdeTheme.kt`): Uses Android 12+ (`API 31+`) wallpaper-derived dynamic color schemes (`dynamicDarkColorScheme`, `dynamicLightColorScheme`) and provides a curated Breeze-Expressive tonal surface hierarchy (`surfaceContainerLowest` through `surfaceContainerHighest`, `primaryContainer`, `secondaryContainer`, `tertiaryContainer`, `errorContainer`) for Android 6–11 (`API 23–30`).
* **Spring-Physics Shape Morphing** (`Buttons.kt`, `Cards.kt`, `PluginsScreen.kt`): Interactive cards and buttons use `MutableInteractionSource` with `Spring.DampingRatioMediumBouncy` animations, morphing corner radii (`28.dp`/`24.dp` resting $\rightarrow$ `16.dp` pressed) alongside `graphicsLayer` scale feedback.
* **Bento Action Grid & Tonal Banners** (`PluginsScreen.kt`, `PairingScreen.kt`, `DevicePairingScreen.kt`): Replaces flat legacy layouts with a Bento plugin grid, squircle tonal device avatars, monospace cryptographic verification key cards, and semantic permission/status banners.

### 2. Mobile SoC Hardware Profiling & Low-Latency Filtering (`SocHardwareOptimizer.kt`)
* **Vendor-Aware SoC Classification**: Inspects `Build.SOC_MANUFACTURER` and `Build.SOC_MODEL` on `API 31+` (falling back to `Build.HARDWARE` and `Build.BOARD` on `API 23–30`) to classify:
  * **Qualcomm Snapdragon** (`QUALCOMM_SNAPDRAGON`)
  * **MediaTek Dimensity & Helio** (`MEDIATEK_DIMENSITY_HELIO`)
  * **NVIDIA Tegra** (`NVIDIA_TEGRA`)
  * **Unisoc Tiger** (`UNISOC_TIGER_T`)
  * **Google Tensor** (`GOOGLE_TENSOR`)
  * **Samsung Exynos** (`SAMSUNG_EXYNOS`)
  * **Generic ARM** (`GENERIC_ARM`)
* **Adaptive Socket Payload Buffers** (`LanLink.kt`): Scales transfer buffers from `128 KB` on constrained Unisoc or `< 3 GB` RAM devices up to `512 KB` on flagship Snapdragon, Dimensity, Tegra, Tensor, and Exynos processors. For small known-size payloads, `selectTransferBufferSize()` caps allocations to prevent ART `LargeObjectSpace` GC churn.
* **Zero-Allocation 1D Kalman Motion Filter** (`PredictiveMotionFilter`): Smooths quantization noise and applies sub-frame velocity lead prediction to touch deltas (`MousePadActivity.kt`) and gyroscope pointer events (`MousePadActivity.kt`, `PresenterActivity.kt`) without per-event heap allocations, resetting state cleanly on `ACTION_DOWN`, `ACTION_UP`, `ACTION_CANCEL`, and sensor unregistration.

### 3. Concurrency, Framing & Network Hardening
* **Thread Assertion & Elastic Pool Fixes** (`ThreadHelper.kt`): Fixes inverted main-thread assertions from upstream (`assertMainThread()` and `assertNotMainThread()`) and replaces the unbounded `Executors.newCachedThreadPool()` with a `SynchronousQueue` elastic `ThreadPoolExecutor` (`corePoolSize` tuned per SoC, `maxPoolSize = 256`, `60s` keep-alive) plus an overflow daemon thread fallback so immortal socket reader loops never starve short-lived tasks or block the Main UI thread.
* **Zero-Overconsumption Packet Framing** (`BoundedLineReader.kt`): Fast-paths `BufferedInputStream` reads via `mark()`/`reset()` chunk scanning while preserving exact single-byte reads on unbuffered `SocketInputStream` instances during pre-TLS handshakes so zero TLS ClientHello/ServerHello bytes are lost.
* **Lifecycle & Multi-Network Reliability** (`LanLinkProvider.kt`, `BackgroundService.kt`): Synchronizes `handleNetworkChange()` and `onStop()` (cancelling pending debounced `networkChangeJob` coroutines), annotates shared service state with `@Volatile`, and tracks active non-cellular `Network` handles in a `ConcurrentHashMap.newKeySet()` so dropping a secondary VPN/Bluetooth link does not falsely trigger offline UI banners while Wi-Fi remains active.

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

### Key Module Breakdown

| Module | Path | Responsibility |
| ------ | ---- | -------------- |
| **Material 3 Theme** | `ui/compose/KdeTheme.kt` | Dynamic color, Breeze-Expressive fallback palettes, `ExpressiveShapes`, `ExpressiveTypography` |
| **Expressive Components** | `ui/compose/Buttons.kt`, `components/Cards.kt`, `KdeTopAppBar.kt` | Spring-morphing buttons/cards, tonal app bar, accessible `SectionHeader` and `Texts` |
| **Expressive Screens** | `ui/compose/screen/{pairing,device,share,about}/` | Bento plugin grid, discovery banners, cryptographic pairing key card, device sharing |
| **SoC & Motion HAL** | `helpers/hardware/SocHardwareOptimizer.kt` | SoC vendor profiling, adaptive socket buffer selection, 1D Kalman `PredictiveMotionFilter` |
| **Transport & Framing** | `backends/lan/LanLink*.kt`, `helpers/BoundedLineReader.kt` | TLS 1.2/1.3 mutual auth, mDNS/UDP discovery, mark/reset bounded line framing |
| **Service & Threading** | `BackgroundService.kt`, `helpers/ThreadHelper.kt` | Foreground service, multi-network tracking, elastic `SynchronousQueue` worker pool |

For the full multi-agent architectural specification and Elephant-Goldfish (EGM) audit report, read [ARCHITECTURE_M3_EXPRESSIVE.md](ARCHITECTURE_M3_EXPRESSIVE.md).

---

## Prerequisites & Dependencies

* **Java Development Kit (JDK)**: JDK 17 or JDK 21 (configured to compile with JVM target 11 and Android core library desugaring).
* **Android SDK**: Android SDK Platform 37 (`compileSdk = 37`, `targetSdk = 37`, `minSdk = 23`).
* **Build System**: Gradle Wrapper (`./gradlew`) with Android Gradle Plugin `9.5.1` and Kotlin `2.4.21`.

---

## Build & Verification Instructions

Run the following commands from the repository root:

```bash
# 1. Configure Java and Android SDK paths
export JAVA_HOME=/path/to/jdk-21
export ANDROID_HOME=/path/to/Android/Sdk

# 2. Run hermetic unit tests (BoundedLineReaderTest, SocHardwareOptimizerTest)
./gradlew testDebugUnitTest --tests "org.kde.kdeconnect.helpers.BoundedLineReaderTest" --tests "org.kde.kdeconnect.helpers.hardware.SocHardwareOptimizerTest"

# 3. Compile the Debug APK
./gradlew assembleDebug

# 4. Install the Debug APK onto a connected Android device or emulator
adb install -r build/outputs/apk/debug/kdeconnect-android-debug.apk
```

---

## Configuration & Usage

1. Connect the Android device and the desktop computer (`kdeconnect-kde` or `GSConnect`) to the same local network.
2. Launch **Material Connect** on Android. Grant the **Local Network** and **Notifications** permissions using the tonal permission banners on the pairing screen.
3. Select the discovered desktop host from **Available devices** and tap **Request pairing**.
4. Confirm that the 6-digit hexadecimal SHA-256 verification key matches on both devices and tap **Accept**.

---

## Privacy, Compliance & License

* **GDPR & Local-First Processing**: All device discovery, pairing certificates, clipboard payloads, and file transfers execute locally over encrypted LAN or Bluetooth sockets. The application collects zero telemetry and initiates zero outbound analytics connections.
* **Vulnerability Disclosure**: See [SECURITY.md](SECURITY.md) for EU Cyber Resilience Act (CRA) reporting procedures.
* **License**: Distributed under the GNU General Public License v2.0 or v3.0 (`GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL`). See [COPYING](COPYING) and [LICENSES](LICENSES) for full license texts.
