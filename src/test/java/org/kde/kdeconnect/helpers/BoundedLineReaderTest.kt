// SPDX-FileCopyrightText: 2026 Albert Vaca Cintora <albertvaka@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

package org.kde.kdeconnect.helpers

import org.junit.Assert
import org.junit.Test
import java.io.BufferedInputStream
import java.io.ByteArrayInputStream
import java.io.FilterInputStream
import java.io.InputStream

class BoundedLineReaderTest {

    private fun stream(s: String) = ByteArrayInputStream(s.toByteArray())

    private fun unbufferedStream(s: String): InputStream = object : FilterInputStream(stream(s)) {
        override fun markSupported(): Boolean = false
    }

    @Test
    fun testReadsLine() {
        Assert.assertEquals("hello\n", readLineBounded(stream("hello\nworld\n"), 10))
    }

    @Test
    fun testReadsSequentialLinesFromSameBufferedStream() {
        val buffered = BufferedInputStream(stream("first_packet\nsecond_packet\nthird_packet\n"))
        Assert.assertEquals("first_packet\n", readLineBounded(buffered, 64))
        Assert.assertEquals("second_packet\n", readLineBounded(buffered, 64))
        Assert.assertEquals("third_packet\n", readLineBounded(buffered, 64))
    }

    @Test
    fun testReadsSequentialLinesFromUnbufferedStreamWithoutOverConsuming() {
        val raw = unbufferedStream("identity_handshake\ntls_payload_bytes\n")
        Assert.assertEquals("identity_handshake\n", readLineBounded(raw, 64))
        Assert.assertEquals("tls_payload_bytes\n", readLineBounded(raw, 64))
    }

    @Test
    fun testLineTooLong() {
        Assert.assertThrows(LineTooLongException::class.java) {
            readLineBounded(stream("hello world\n"), 5)
        }
        Assert.assertThrows(LineTooLongException::class.java) {
            readLineBounded(unbufferedStream("hello world\n"), 5)
        }
    }
}
