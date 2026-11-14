// SPDX-FileCopyrightText: 2025 Albert Vaca Cintora <albertvaka@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Manages background thread execution and thread-confinement assertions across the application.
 *
 * Architecture Position:
 * Core Concurrency & Hardware Optimization Layer (`org.kde.kdeconnect.helpers`).
 * Upstream callers: `LanLinkProvider`, `LanLink`, `BluetoothLinkProvider`, and plugin workers.
 * Downstream dependencies: `SocHardwareOptimizer` and Android `Looper`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Thread-safe singleton. Uses a `SynchronousQueue` elastic `ThreadPoolExecutor` with a safe
 * overflow daemon fallback so long-lived socket listener loops never starve short-lived tasks
 * or block the Android Main UI thread.
 */
package org.kde.kdeconnect.helpers

import android.os.Looper
import android.util.Log
import org.kde.kdeconnect.helpers.hardware.SocHardwareOptimizer
import org.kde.kdeconnect_tp.BuildConfig
import java.util.concurrent.ExecutorService
import java.util.concurrent.SynchronousQueue
import java.util.concurrent.ThreadFactory
import java.util.concurrent.ThreadPoolExecutor
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger

/**
 * Provides SoC-tuned background thread execution and main-thread verification utilities.
 */
object ThreadHelper {

    private const val MAX_POOL_THREADS: Int = 256
    private const val KEEP_ALIVE_SECONDS: Long = 60L

    private val threadCounter = AtomicInteger(1)

    private val workerThreadFactory = ThreadFactory { runnable ->
        Thread(runnable, "KdeConnect-Worker-${threadCounter.getAndIncrement()}").apply {
            isDaemon = true
            priority = Thread.NORM_PRIORITY
        }
    }

    /**
     * Elastic thread pool sized according to the host SoC core count.
     * If `MAX_POOL_THREADS` is ever reached during an extreme network burst, the rejected
     * execution handler spawns a dedicated daemon thread rather than running blocking I/O
     * on the caller (which could be the Main UI thread or a socket accept loop).
     */
    private val executor: ExecutorService = ThreadPoolExecutor(
        SocHardwareOptimizer.recommendedCoreThreads,
        MAX_POOL_THREADS,
        KEEP_ALIVE_SECONDS,
        TimeUnit.SECONDS,
        SynchronousQueue(),
        workerThreadFactory
    ) { runnable, _ ->
        Thread(runnable, "KdeConnect-Overflow-${threadCounter.getAndIncrement()}").apply {
            isDaemon = true
            start()
        }
    }

    /**
     * Executes [command] asynchronously on a background worker thread.
     *
     * @param command Task to run off the Main UI thread.
     */
    fun runOnNewThread(command: Runnable) {
        executor.execute(command)
    }

    /**
     * Logs a warning in debug builds when the current thread is NOT the Android Main UI thread.
     */
    fun assertMainThread() {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() != Looper.getMainLooper().thread) {
                Log.w(
                    "ThreadHelper",
                    "This function must be called from the Main thread.",
                    Exception("assertMainThread")
                )
            }
        }
    }

    /**
     * Logs a warning in debug builds when the current thread IS the Android Main UI thread.
     */
    fun assertNotMainThread() {
        if (BuildConfig.DEBUG) {
            if (Thread.currentThread() == Looper.getMainLooper().thread) {
                Log.w(
                    "ThreadHelper",
                    "This function must NOT be called from the Main thread.",
                    Exception("assertNotMainThread")
                )
            }
        }
    }
}
