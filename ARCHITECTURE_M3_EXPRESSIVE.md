# Material 3 Expressive KDE Connect / GSConnect — Architectural Specification & 5-Agent Blueprint

// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

## 1. System Overview & Architectural Pattern (RULE 3)

This specification defines the **Material 3 Expressive** fork of the KDE Connect Android application (`material-kdeconnect`). The system maintains 100% network protocol v8 compatibility with `kdeconnect-kde` (Linux, Windows, macOS) and `GSConnect` (GNOME Shell) while modernizing the user interface, concurrency model, and mobile SoC/NPU hardware execution paths.

### Architectural Pattern
The application uses a **Layered Reactive Unidirectional Data Flow (UDF)** architecture combined with an **Event-Driven Background Service + Hardware Abstraction Layer (HAL)**:
1. **UI Presentation Layer (`org.kde.kdeconnect.ui.compose`)**: Jetpack Compose Material 3 Expressive UI driven by immutable `StateFlow` states, dynamic spring physics (`Spring.DampingRatioMediumBouncy`, `Spring.StiffnessMediumLow`), morphing shape tokens, and tonal surface container hierarchy.
2. **Domain & Plugin Layer (`org.kde.kdeconnect.plugins`)**: Modular plugin controllers (`MprisPlugin`, `MousePadPlugin`, `SharePlugin`, `RunCommandPlugin`, `ClipboardPlugin`, `BatteryPlugin`, `ConnectivityReportPlugin`) communicating with `Device` state holders.
3. **Hardware & SoC Optimization Layer (`org.kde.kdeconnect.helpers.hardware`)**: Vendor-aware SoC profiler (`SocHardwareOptimizer`) and non-blocking coroutine/thread dispatchers (`ThreadHelper`) tuned for **Qualcomm Snapdragon (Hexagon NPU / Kryo)**, **MediaTek Dimensity & Helio (APU / CorePilot)**, **NVIDIA Tegra**, **Unisoc Tiger**, **Samsung Exynos**, and **Google Tensor** architectures.
4. **Transport & Security Layer (`org.kde.kdeconnect.backends`)**: Asynchronous TLS 1.3 / SSL socket links (`LanLink`, `BluetoothLink`), mDNS discovery, and buffered frame readers (`BoundedLineReader`).

### Directory & Module Tree
```text
material-connect/
├── ARCHITECTURE_M3_EXPRESSIVE.md             (NEW - Architectural Specification)
├── build.gradle.kts
├── gradle/
│   └── libs.versions.toml
├── proguard-rules.pro
├── README.md                                 (UPDATED - ASD-STE100 Documentation)
├── SECURITY.md                               (NEW - EU CRA Vulnerability Policy)
└── src/
    ├── main/
    │   ├── AndroidManifest.xml
    │   ├── java/org/kde/kdeconnect/
    │   │   ├── BackgroundService.kt          (UPDATED - Multi-Network & Volatile Hardening)
    │   │   ├── Device.kt
    │   │   ├── KdeConnect.kt
    │   │   ├── NetworkPacket.kt
    │   │   ├── backends/
    │   │   │   ├── lan/
    │   │   │   │   ├── LanLink.kt            (UPDATED - Dynamic Payload Buffer Sizing)
    │   │   │   │   ├── LanLinkProvider.kt    (UPDATED - Synchronized Lifecycle & Scope Cancel)
    │   │   │   │   └── MdnsDiscovery.kt
    │   │   │   └── bluetooth/
    │   │   ├── helpers/
    │   │   │   ├── BoundedLineReader.kt      (UPDATED - Mark/Reset Buffered Fast-Path)
    │   │   │   ├── ThreadHelper.kt           (UPDATED - Fixed Assertions & Safe Elastic Pool)
    │   │   │   └── hardware/
    │   │   │       └── SocHardwareOptimizer.kt (NEW - SoC Profiler & Kalman Motion Filter)
    │   │   ├── plugins/
    │   │   │   ├── mousepad/
    │   │   │   │   └── MousePadActivity.kt   (UPDATED - PredictiveMotionFilter Integration)
    │   │   │   ├── mpris/
    │   │   │   ├── presenter/
    │   │   │   │   └── PresenterActivity.kt  (UPDATED - PredictiveMotionFilter Integration)
    │   │   │   ├── runcommand/
    │   │   │   └── share/
    │   │   └── ui/
    │   │       ├── MainActivity.kt
    │   │       ├── DeviceFragment.kt
    │   │       ├── PairingFragment.kt
    │   │       └── compose/
    │   │           ├── KdeTheme.kt           (UPDATED - M3 Expressive Colors, Shapes, Typography)
    │   │           ├── KdeTopAppBar.kt       (UPDATED - M3 Expressive Tonal App Bar)
    │   │           ├── Buttons.kt            (UPDATED - Spring Morphing & Scale Feedback)
    │   │           ├── components/
    │   │           │   ├── Cards.kt          (UPDATED - Native Clickable Morphing KdeCard)
    │   │           │   ├── SectionHeader.kt  (UPDATED - Expressive Accent Pill Header)
    │   │           │   └── Texts.kt          (UPDATED - Fixed Clickable & Layout Semantics)
    │   │           └── screen/
    │   │               ├── about/AboutScreen.kt
    │   │               ├── device/
    │   │               │   ├── DeviceErrorScreen.kt
    │   │               │   ├── DevicePairingScreen.kt
    │   │               │   └── PluginsScreen.kt
    │   │               ├── pairing/
    │   │               │   ├── PairingScreen.kt
    │   │               │   └── PairingUiState.kt
    │   │               └── share/ShareScreen.kt
    │   └── res/
    └── test/
        └── java/org/kde/kdeconnect/
            └── helpers/
                ├── BoundedLineReaderTest.kt  (NEW - Hermetic Unit Tests)
                └── hardware/
                    └── SocHardwareOptimizerTest.kt (NEW - Hermetic Unit Tests)
```

---

## 2. Reconciled Engineering Contracts (Post-EGM Goldfish Audit)

### Contract 1: `SocHardwareOptimizer.kt` (Agent 5 + Goldfish Readiness)
* **Vendor Classification**:
  * `enum class SocVendor { QUALCOMM_SNAPDRAGON, MEDIATEK_DIMENSITY_HELIO, NVIDIA_TEGRA, UNISOC_TIGER_T, GOOGLE_TENSOR, SAMSUNG_EXYNOS, GENERIC_ARM }`
  * Pure deterministic function `fun classifySoc(manufacturer: String, model: String, hardware: String, board: String): SocVendor` for hermetic JVM testing.
  * Runtime detection guards `Build.SOC_MANUFACTURER` and `Build.SOC_MODEL` behind `Build.VERSION.SDK_INT >= Build.VERSION_CODES.S` (`API 31`) and falls back to `Build.HARDWARE` and `Build.BOARD` on API 23–30.
* **Adaptive Payload Buffer Sizing**:
  * `fun computeOptimalPayloadBufferSize(vendor: SocVendor, totalRamBytes: Long = Long.MAX_VALUE): Int` returns `128 * 1024` for `UNISOC_TIGER_T` or devices with `< 3 GB` RAM, `256 * 1024` for `GENERIC_ARM`, and `512 * 1024` for `QUALCOMM_SNAPDRAGON`, `MEDIATEK_DIMENSITY_HELIO`, `NVIDIA_TEGRA`, `GOOGLE_TENSOR`, and `SAMSUNG_EXYNOS`.
  * `fun selectTransferBufferSize(payloadSize: Long): Int` caps allocation to `maxOf(8 * 1024, payloadSize.toInt())` when `payloadSize in 1 until optimalPayloadBufferSize` so small payloads never allocate 512 KB in ART `LargeObjectSpace`.
* **Zero-Allocation 1D Kalman / Exponential `PredictiveMotionFilter`**:
  * `class PredictiveMotionFilter(private val processNoiseQ: Float = 0.08f, private val measurementNoiseR: Float = 0.18f, private val predictionLeadFactor: Float = 0.22f)`
  * `fun filter(dx: Float, dy: Float, outDelta: FloatArray)`: Updates internal scalar X/Y state and velocity estimates in-place without heap allocations and writes filtered + lead-predicted deltas into `outDelta[0]` and `outDelta[1]`.
  * `fun reset()`: Clears state estimates, error covariance, and velocity on gesture start/end (`MotionEvent.ACTION_DOWN`, `MotionEvent.ACTION_UP`, and sensor unregistration).

### Contract 2: `ThreadHelper.kt` & `BoundedLineReader.kt` (Agent 4 + Goldfish Design Critic)
* **`ThreadHelper.kt`**:
  * Fixes the inverted boolean assertions in `assertMainThread()` (`Thread.currentThread() != Looper.getMainLooper().thread`) and `assertNotMainThread()` (`Thread.currentThread() == Looper.getMainLooper().thread`).
  * Uses a `SynchronousQueue` elastic `ThreadPoolExecutor` (`corePoolSize = SocHardwareOptimizer.recommendedCoreThreads`, `maximumPoolSize = 256`, `keepAliveTime = 60L` seconds) with a named daemon `ThreadFactory` and an overflow fallback handler that spawns a dedicated daemon thread when saturated—guaranteeing that immortal socket listener loops (`LanLink.startListening`, `setupTcpListener`, `setupUdpListener`) never starve handshake or payload tasks and never run on the Main UI thread.
* **`BoundedLineReader.kt`**:
  * Preserves exact byte-boundary semantics. When `inputStream.markSupported()` is `true` (`BufferedInputStream`, `ByteArrayInputStream`), reads in chunks up to `min(1024, maxLineSize - buffer.size())` after calling `inputStream.mark(toRead)`. When `'\n'` is found at index `i`, writes `0..i` to `buffer`, calls `inputStream.reset()`, and advances `inputStream.skip((i + 1).toLong())` in a loop until all `i + 1` bytes are skipped. When `markSupported()` is `false` (unbuffered `SocketInputStream` during pre-TLS handshake), executes the single-byte read loop so zero post-newline TLS bytes are ever consumed.

### Contract 3: `LanLinkProvider.kt` & `BackgroundService.kt` Hardening (Agent 4 + Goldfish Design Critic)
* **`LanLinkProvider.kt`**:
  * Annotates `handleNetworkChange` and `onStop` with `@Synchronized` and calls `networkChangeJob?.cancel()` in `onStop()` so delayed network-change coroutines cannot reopen sockets or mDNS multicast locks after service shutdown.
* **`BackgroundService.kt`**:
  * Annotates `instance` and `initialized` with `@Volatile`.
  * Tracks active non-cellular `Network` handles in `ConcurrentHashMap.newKeySet<Network>()` inside `networkCallback` (`onAvailable` adds `network`, `onLost` removes `network`) and posts `activeNetworks.isNotEmpty()` to `isConnectedToNonCellularNetwork`.

### Contract 4: Material 3 Expressive Compose UI System (Agents 1 & 3 + Goldfish Codebase Details)
* **Strict Signature Compatibility**: All existing screen and component entry-point signatures (`KdeTheme`, `KdeTopAppBar`, `KdeButton`, `KdeTextButton`, `KdeCard`, `SectionHeader`, `PairingScreen`, `PluginsScreen`, `DevicePairingScreen`, `DeviceErrorScreen`, `ShareScreen`, `AboutScreen`) remain 100% source-compatible with existing Fragments and Activities.
* **No `material-icons-extended` Dependency**: Uses only `androidx.compose.material.icons.Icons` from `material-icons-core` (`ArrowBack`, `ArrowForward`, `Build`, `Check`, `Close`, `Info`, `Refresh`, `Settings`, `Warning`) and existing vector drawables (`R.drawable.*`).
* **Native Clickable Morphing**: `KdeCard` and `KdeButton` use Material 3's native `Card(onClick = ..., shape = ..., interactionSource = ...)` and `Button(onClick = ..., shape = ..., interactionSource = ...)` overloads with `Spring.DampingRatioMediumBouncy` corner-radius morphing and `graphicsLayer` scale feedback.
