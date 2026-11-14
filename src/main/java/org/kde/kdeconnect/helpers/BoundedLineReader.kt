// SPDX-FileCopyrightText: 2025 Albert Vaca Cintora <albertvaka@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Reads newline-delimited UTF-8 protocol lines from an InputStream with a strict byte limit.
 *
 * Architecture Position:
 * Transport & Protocol Framing Layer (`org.kde.kdeconnect.helpers`).
 * Upstream callers: `LanLinkProvider`, `LanLink`, and `BluetoothLink`.
 * Downstream dependencies: `java.io.InputStream` and `java.io.ByteArrayOutputStream`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Stateless function; callers must synchronize access if sharing the same `InputStream`
 * across threads.
 */
package org.kde.kdeconnect.helpers

import java.io.ByteArrayOutputStream
import java.io.IOException
import java.io.InputStream

/**
 * Signals that an incoming protocol line exceeded the maximum permitted byte length.
 */
class LineTooLongException : IOException("Line too long")

private const val INITIAL_BUFFER_CAPACITY_BYTES: Int = 1024
private const val CHUNK_READ_SIZE_BYTES: Int = 1024

/**
 * Reads bytes from [inputStream] until a `\n` terminator is found or [maxLineSize] is reached.
 *
 * When [inputStream] supports `mark()` and `reset()` (such as `BufferedInputStream` in `LanLink`
 * and `BluetoothLink`), this function reads in chunks and rewinds the stream cursor to the exact
 * byte following `\n` so subsequent packets are never lost. When `markSupported()` is `false`
 * (such as raw `SocketInputStream` during the pre-TLS handshake in `LanLinkProvider`), this
 * function reads single bytes so post-newline TLS handshake bytes are never over-consumed.
 *
 * @param inputStream Source byte stream.
 * @param maxLineSize Maximum allowed line length in bytes before throwing [LineTooLongException].
 * @return The UTF-8 decoded line including the trailing `\n` character.
 * @throws IOException If the stream closes before `\n` or an I/O error occurs.
 * @throws LineTooLongException If `maxLineSize` is reached before encountering `\n`.
 */
@Throws(IOException::class)
fun readLineBounded(inputStream: InputStream, maxLineSize: Int): String {
    if (maxLineSize <= 0) {
        throw LineTooLongException()
    }

    val buffer = ByteArrayOutputStream(minOf(INITIAL_BUFFER_CAPACITY_BYTES, maxLineSize))

    // Fast-path for mark-capable streams (BufferedInputStream, ByteArrayInputStream).
    if (inputStream.markSupported()) {
        val chunk = ByteArray(CHUNK_READ_SIZE_BYTES)
        while (true) {
            val remainingAllowed = maxLineSize - buffer.size()
            if (remainingAllowed <= 0) {
                throw LineTooLongException()
            }
            val toRead = minOf(chunk.size, remainingAllowed)
            inputStream.mark(toRead)
            val bytesRead = inputStream.read(chunk, 0, toRead)
            if (bytesRead == -1) {
                throw IOException("Stream closed")
            }

            var newlineIndex = -1
            for (i in 0 until bytesRead) {
                if (chunk[i] == '\n'.code.toByte()) {
                    newlineIndex = i
                    break
                }
            }

            if (newlineIndex != -1) {
                val consumedCount = newlineIndex + 1
                buffer.write(chunk, 0, consumedCount)
                inputStream.reset()
                var bytesToSkip = consumedCount.toLong()
                while (bytesToSkip > 0L) {
                    val skipped = inputStream.skip(bytesToSkip)
                    if (skipped > 0L) {
                        bytesToSkip -= skipped
                    } else {
                        // Fallback if skip() returns 0 on a buffered stream
                        if (inputStream.read() == -1) break
                        bytesToSkip--
                    }
                }
                return buffer.toString()
            }

            buffer.write(chunk, 0, bytesRead)
            if (buffer.size() >= maxLineSize) {
                throw LineTooLongException()
            }
        }
    }

    // Exact single-byte path for unbuffered streams (e.g., pre-TLS SocketInputStream).
    while (true) {
        val b = inputStream.read()
        if (b == -1) {
            throw IOException("Stream closed")
        }
        buffer.write(b)
        if (b == '\n'.code) {
            return buffer.toString()
        }
        if (buffer.size() >= maxLineSize) {
            throw LineTooLongException()
        }
    }
}
