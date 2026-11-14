// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

package org.kde.kdeconnect.helpers.hardware

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.abs

class SocHardwareOptimizerTest {

    @Test
    fun testClassifySocAllVendors() {
        assertEquals(
            SocVendor.QUALCOMM_SNAPDRAGON,
            SocHardwareOptimizer.classifySoc("Qualcomm", "SM8650", "qcom", "kalama")
        )
        assertEquals(
            SocVendor.MEDIATEK_DIMENSITY_HELIO,
            SocHardwareOptimizer.classifySoc("MediaTek", "MT6989", "mt6989", "dimensity9300")
        )
        assertEquals(
            SocVendor.NVIDIA_TEGRA,
            SocHardwareOptimizer.classifySoc("NVIDIA", "T210", "tegra", "darcy")
        )
        assertEquals(
            SocVendor.UNISOC_TIGER_T,
            SocHardwareOptimizer.classifySoc("Unisoc", "UMS9230", "sp9863a", "t606")
        )
        assertEquals(
            SocVendor.GOOGLE_TENSOR,
            SocHardwareOptimizer.classifySoc("Google", "Tensor G4", "zuma", "husky")
        )
        assertEquals(
            SocVendor.SAMSUNG_EXYNOS,
            SocHardwareOptimizer.classifySoc("Samsung", "s5e9945", "exynos2400", "e2s")
        )
        assertEquals(
            SocVendor.GENERIC_ARM,
            SocHardwareOptimizer.classifySoc("Unknown", "Generic64", "ranchu", "goldfish")
        )
    }

    @Test
    fun testOptimalPayloadBufferSizing() {
        val highRam = 8L * 1024L * 1024L * 1024L
        val lowRam = 2L * 1024L * 1024L * 1024L

        assertEquals(
            512 * 1024,
            SocHardwareOptimizer.computeOptimalPayloadBufferSize(SocVendor.QUALCOMM_SNAPDRAGON, highRam)
        )
        assertEquals(
            512 * 1024,
            SocHardwareOptimizer.computeOptimalPayloadBufferSize(SocVendor.MEDIATEK_DIMENSITY_HELIO, highRam)
        )
        assertEquals(
            512 * 1024,
            SocHardwareOptimizer.computeOptimalPayloadBufferSize(SocVendor.NVIDIA_TEGRA, highRam)
        )
        assertEquals(
            128 * 1024,
            SocHardwareOptimizer.computeOptimalPayloadBufferSize(SocVendor.UNISOC_TIGER_T, highRam)
        )
        assertEquals(
            128 * 1024,
            SocHardwareOptimizer.computeOptimalPayloadBufferSize(SocVendor.QUALCOMM_SNAPDRAGON, lowRam)
        )
        assertEquals(
            256 * 1024,
            SocHardwareOptimizer.computeOptimalPayloadBufferSize(SocVendor.GENERIC_ARM, highRam)
        )
    }

    @Test
    fun testSelectTransferBufferSizeAvoidsLargeAllocationForSmallPayloads() {
        val maxBuffer = 512 * 1024
        assertEquals(8 * 1024, SocHardwareOptimizer.selectTransferBufferSize(1024L, maxBuffer))
        assertEquals(32 * 1024, SocHardwareOptimizer.selectTransferBufferSize(32 * 1024L, maxBuffer))
        assertEquals(maxBuffer, SocHardwareOptimizer.selectTransferBufferSize(2L * 1024L * 1024L, maxBuffer))
        assertEquals(maxBuffer, SocHardwareOptimizer.selectTransferBufferSize(-1L, maxBuffer))
    }

    @Test
    fun testPredictiveMotionFilterConvergenceAndReset() {
        val filter = PredictiveMotionFilter()
        val out = FloatArray(2)

        // First sample initializes directly without lag
        filter.filter(10f, -5f, out)
        assertEquals(10f, out[0], 0.001f)
        assertEquals(-5f, out[1], 0.001f)

        // Constant velocity input should converge smoothly
        for (i in 0 until 10) {
            filter.filter(10f, -5f, out)
        }
        assertTrue(abs(out[0] - 10f) < 1.5f)
        assertTrue(abs(out[1] - (-5f)) < 1.0f)

        // Reset clears residual velocity so next gesture starts cleanly at new delta
        filter.reset()
        filter.filter(1f, 2f, out)
        assertEquals(1f, out[0], 0.001f)
        assertEquals(2f, out[1], 0.001f)
    }
}
