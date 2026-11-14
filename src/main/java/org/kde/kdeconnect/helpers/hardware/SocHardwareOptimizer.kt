// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Provides vendor-aware mobile SoC classification, adaptive network payload buffer sizing,
 * and a zero-allocation 1D Kalman predictive motion filter for touchpad and gyroscope input.
 *
 * Architecture Position:
 * Hardware Abstraction & Optimization Layer (`org.kde.kdeconnect.helpers.hardware`).
 * Upstream callers: `ThreadHelper`, `LanLink`, `MousePadActivity`, and `PresenterActivity`.
 * Downstream dependencies: Android `Build` metadata and standard Kotlin math primitives.
 *
 * Concurrency & Thread-Safety Constraints:
 * `SocHardwareOptimizer` properties are immutable and thread-safe across all threads.
 * `PredictiveMotionFilter` instances maintain per-stream state and must be confined to
 * the UI/Sensor callback thread (Main thread) or synchronized externally.
 */
package org.kde.kdeconnect.helpers.hardware

import android.os.Build

/**
 * Enumerates supported mobile System-on-Chip (SoC) vendor families for hardware tuning.
 */
enum class SocVendor {
    QUALCOMM_SNAPDRAGON,
    MEDIATEK_DIMENSITY_HELIO,
    NVIDIA_TEGRA,
    UNISOC_TIGER_T,
    GOOGLE_TENSOR,
    SAMSUNG_EXYNOS,
    GENERIC_ARM
}

/**
 * Profiles the host SoC hardware and computes optimal buffer sizes and thread pool parameters.
 */
object SocHardwareOptimizer {

    private const val BUFFER_SIZE_CONSTRAINED_BYTES: Int = 128 * 1024
    private const val BUFFER_SIZE_STANDARD_BYTES: Int = 256 * 1024
    private const val BUFFER_SIZE_FLAGSHIP_BYTES: Int = 512 * 1024
    private const val MIN_SMALL_PAYLOAD_BUFFER_BYTES: Int = 8 * 1024
    private const val THREE_GIGABYTES_BYTES: Long = 3L * 1024L * 1024L * 1024L

    /**
     * Detected SoC vendor family for the current Android device.
     */
    val currentVendor: SocVendor by lazy {
        val manufacturer = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MANUFACTURER ?: ""
        } else {
            ""
        }
        val model = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            Build.SOC_MODEL ?: ""
        } else {
            ""
        }
        val hardware = Build.HARDWARE ?: ""
        val board = Build.BOARD ?: ""
        classifySoc(
            manufacturer = manufacturer,
            model = model,
            hardware = hardware,
            board = board
        )
    }

    /**
     * Number of logical CPU cores available to the Android Runtime.
     */
    val availableCores: Int by lazy {
        Runtime.getRuntime().availableProcessors().coerceAtLeast(2)
    }

    /**
     * Recommended core thread count for the background I/O and packet executor pool.
     */
    val recommendedCoreThreads: Int by lazy {
        when (currentVendor) {
            SocVendor.UNISOC_TIGER_T -> availableCores.coerceIn(4, 6)
            SocVendor.QUALCOMM_SNAPDRAGON,
            SocVendor.MEDIATEK_DIMENSITY_HELIO,
            SocVendor.NVIDIA_TEGRA,
            SocVendor.GOOGLE_TENSOR,
            SocVendor.SAMSUNG_EXYNOS -> (availableCores * 2).coerceIn(8, 16)
            SocVendor.GENERIC_ARM -> availableCores.coerceIn(4, 12)
        }
    }

    /**
     * Maximum socket payload transfer buffer size in bytes for the detected SoC.
     */
    val optimalPayloadBufferSize: Int by lazy {
        val maxHeapBytes = Runtime.getRuntime().maxMemory()
        // If the app heap limit indicates a constrained low-RAM device (< 128 MB heap), cap buffer size.
        val estimatedRamBytes = if (maxHeapBytes < 128L * 1024L * 1024L) {
            THREE_GIGABYTES_BYTES - 1L
        } else {
            THREE_GIGABYTES_BYTES + 1L
        }
        computeOptimalPayloadBufferSize(currentVendor, estimatedRamBytes)
    }

    /**
     * Classifies the SoC vendor from raw hardware identification strings.
     *
     * @param manufacturer Value of `Build.SOC_MANUFACTURER` (or empty on API < 31).
     * @param model Value of `Build.SOC_MODEL` (or empty on API < 31).
     * @param hardware Value of `Build.HARDWARE`.
     * @param board Value of `Build.BOARD`.
     * @return The matched [SocVendor] enum constant.
     */
    fun classifySoc(
        manufacturer: String,
        model: String,
        hardware: String,
        board: String
    ): SocVendor {
        val combined = "$manufacturer $model $hardware $board".lowercase()
        return when {
            combined.contains("qualcomm") ||
                combined.contains("qcom") ||
                combined.contains("sm8") ||
                combined.contains("sm7") ||
                combined.contains("sdm") ||
                combined.contains("lahaina") ||
                combined.contains("kalama") ||
                combined.contains("pineapple") ||
                combined.contains("taro") -> SocVendor.QUALCOMM_SNAPDRAGON

            combined.contains("mediatek") ||
                combined.contains("mtk") ||
                combined.contains("mt6") ||
                combined.contains("mt8") ||
                combined.contains("dimensity") ||
                combined.contains("helio") -> SocVendor.MEDIATEK_DIMENSITY_HELIO

            combined.contains("nvidia") ||
                combined.contains("tegra") ||
                combined.contains("t210") ||
                combined.contains("t186") ||
                combined.contains("t194") ||
                combined.contains("t234") -> SocVendor.NVIDIA_TEGRA

            combined.contains("unisoc") ||
                combined.contains("spreadtrum") ||
                combined.contains("ums") ||
                combined.contains("sp98") ||
                combined.contains("t606") ||
                combined.contains("t610") ||
                combined.contains("t612") ||
                combined.contains("t616") ||
                combined.contains("t618") ||
                combined.contains("t700") ||
                combined.contains("t760") ||
                combined.contains("t820") -> SocVendor.UNISOC_TIGER_T

            combined.contains("google") ||
                combined.contains("tensor") ||
                combined.contains("gs101") ||
                combined.contains("gs201") ||
                combined.contains("zuma") ||
                combined.contains("laguna") -> SocVendor.GOOGLE_TENSOR

            combined.contains("samsung") ||
                combined.contains("exynos") ||
                combined.contains("s5e") -> SocVendor.SAMSUNG_EXYNOS

            else -> SocVendor.GENERIC_ARM
        }
    }

    /**
     * Computes the optimal payload buffer size in bytes for a given [SocVendor] and system memory limit.
     *
     * @param vendor Target [SocVendor] architecture.
     * @param totalRamBytes Total system RAM in bytes.
     * @return Optimal payload buffer size in bytes (`128 KB`, `256 KB`, or `512 KB`).
     */
    fun computeOptimalPayloadBufferSize(
        vendor: SocVendor,
        totalRamBytes: Long = Long.MAX_VALUE
    ): Int {
        if (totalRamBytes < THREE_GIGABYTES_BYTES || vendor == SocVendor.UNISOC_TIGER_T) {
            return BUFFER_SIZE_CONSTRAINED_BYTES
        }
        return when (vendor) {
            SocVendor.QUALCOMM_SNAPDRAGON,
            SocVendor.MEDIATEK_DIMENSITY_HELIO,
            SocVendor.NVIDIA_TEGRA,
            SocVendor.GOOGLE_TENSOR,
            SocVendor.SAMSUNG_EXYNOS -> BUFFER_SIZE_FLAGSHIP_BYTES
            SocVendor.GENERIC_ARM -> BUFFER_SIZE_STANDARD_BYTES
            SocVendor.UNISOC_TIGER_T -> BUFFER_SIZE_CONSTRAINED_BYTES
        }
    }

    /**
     * Selects a transfer buffer size tailored to the known payload size to avoid allocating
     * large arrays in ART LargeObjectSpace for small payloads.
     *
     * @param payloadSize Size of the outgoing payload in bytes (or `<= 0` if unknown).
     * @param maxBufferSize Upper bound buffer size in bytes.
     * @return Selected buffer size in bytes.
     */
    fun selectTransferBufferSize(
        payloadSize: Long,
        maxBufferSize: Int = optimalPayloadBufferSize
    ): Int {
        if (payloadSize in 1L until maxBufferSize.toLong()) {
            return payloadSize.toInt().coerceAtLeast(MIN_SMALL_PAYLOAD_BUFFER_BYTES)
        }
        return maxBufferSize
    }
}

/**
 * Zero-allocation 1D Kalman and exponential velocity lead filter for 2D pointer/gyroscope deltas.
 *
 * Smooths high-frequency sensor quantization noise while predicting sub-frame pointer movement
 * to reduce perceived latency on 90 Hz, 120 Hz, and 144 Hz displays.
 *
 * @property processNoiseQ Process noise covariance scalar (`Q > 0f`).
 * @property measurementNoiseR Measurement noise covariance scalar (`R > 0f`).
 * @property predictionLeadFactor Sub-frame velocity lookahead factor (`>= 0f`).
 */
class PredictiveMotionFilter(
    private val processNoiseQ: Float = 0.12f,
    private val measurementNoiseR: Float = 0.18f,
    private val predictionLeadFactor: Float = 0.15f
) {
    private var initialized: Boolean = false
    private var estimateX: Float = 0f
    private var estimateY: Float = 0f
    private var errorCovX: Float = 1f
    private var errorCovY: Float = 1f
    private var velocityX: Float = 0f
    private var velocityY: Float = 0f

    /**
     * Filters raw 2D motion deltas and writes the denoised, lead-compensated output into [outDelta].
     *
     * @param dx Raw horizontal delta.
     * @param dy Raw vertical delta.
     * @param outDelta Pre-allocated [FloatArray] of length `>= 2` receiving `[filteredDx, filteredDy]`.
     * @throws IllegalArgumentException If [outDelta] has fewer than 2 elements.
     */
    fun filter(dx: Float, dy: Float, outDelta: FloatArray) {
        require(outDelta.size >= 2) { "outDelta must have at least 2 elements" }

        if (!dx.isFinite() || !dy.isFinite()) {
            outDelta[0] = 0f
            outDelta[1] = 0f
            return
        }

        // Transition state: first sample initializes filter state directly without lag.
        if (!initialized) {
            initialized = true
            estimateX = dx
            estimateY = dy
            errorCovX = 1f
            errorCovY = 1f
            velocityX = 0f
            velocityY = 0f
            outDelta[0] = dx
            outDelta[1] = dy
            return
        }

        // 1D Kalman time update (prediction step) using prior velocity trend.
        val predictedX = estimateX + velocityX * 0.5f
        val predictedY = estimateY + velocityY * 0.5f
        val priorCovX = errorCovX + processNoiseQ
        val priorCovY = errorCovY + processNoiseQ

        // Measurement update (Kalman gain calculation).
        val kalmanGainX = priorCovX / (priorCovX + measurementNoiseR)
        val kalmanGainY = priorCovY / (priorCovY + measurementNoiseR)

        val newEstimateX = predictedX + kalmanGainX * (dx - predictedX)
        val newEstimateY = predictedY + kalmanGainY * (dy - predictedY)

        velocityX = 0.6f * velocityX + 0.4f * (newEstimateX - estimateX)
        velocityY = 0.6f * velocityY + 0.4f * (newEstimateY - estimateY)

        estimateX = newEstimateX
        estimateY = newEstimateY
        errorCovX = (1f - kalmanGainX) * priorCovX
        errorCovY = (1f - kalmanGainY) * priorCovY

        outDelta[0] = newEstimateX + velocityX * predictionLeadFactor
        outDelta[1] = newEstimateY + velocityY * predictionLeadFactor
    }

    /**
     * Resets all internal Kalman estimates, covariances, and velocity vectors.
     * Call this when a touch gesture ends or a gyroscope listener unregisters.
     */
    fun reset() {
        initialized = false
        estimateX = 0f
        estimateY = 0f
        errorCovX = 1f
        errorCovY = 1f
        velocityX = 0f
        velocityY = 0f
    }
}
