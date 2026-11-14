// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Provides reusable Material 3 Expressive typography wrapper composables.
 *
 * Architecture Position:
 * UI Presentation Component Layer (`org.kde.kdeconnect.ui.compose.components`).
 * Upstream callers: `PairingScreen`, `DevicePairingScreen`, `DeviceErrorScreen`, `SectionHeader`.
 * Downstream dependencies: `androidx.compose.material3.Text`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Composable UI components evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kde.kdeconnect.ui.compose.KdeTheme

@Composable
private fun KdeText(
    text: String,
    style: TextStyle,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
) {
    Text(
        text = text,
        color = color,
        style = style,
        fontSize = fontSize,
        textAlign = textAlign,
        maxLines = maxLines,
        overflow = if (maxLines < Int.MAX_VALUE) TextOverflow.Ellipsis else TextOverflow.Clip,
        modifier = if (onClick != null) {
            modifier.clickable(onClick = onClick)
        } else {
            modifier
        }
    )
}

@Composable
fun KdeBodySmallText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
) = KdeText(
    text = text,
    style = MaterialTheme.typography.bodySmall,
    modifier = modifier,
    textAlign = textAlign,
    color = color,
    fontSize = fontSize,
    maxLines = maxLines,
    onClick = onClick
)

@Composable
fun KdeBodyMediumText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
) = KdeText(
    text = text,
    style = MaterialTheme.typography.bodyMedium,
    modifier = modifier,
    textAlign = textAlign,
    color = color,
    fontSize = fontSize,
    maxLines = maxLines,
    onClick = onClick
)

@Composable
fun KdeBodyLargeText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
) = KdeText(
    text = text,
    style = MaterialTheme.typography.bodyLarge,
    modifier = modifier,
    textAlign = textAlign,
    color = color,
    fontSize = fontSize,
    maxLines = maxLines,
    onClick = onClick
)

@Composable
fun KdeTitleMediumText(
    text: String,
    modifier: Modifier = Modifier,
    textAlign: TextAlign = TextAlign.Start,
    color: Color = MaterialTheme.colorScheme.onSurface,
    fontSize: TextUnit = TextUnit.Unspecified,
    maxLines: Int = Int.MAX_VALUE,
    onClick: (() -> Unit)? = null,
) = KdeText(
    text = text,
    style = MaterialTheme.typography.titleMedium,
    modifier = modifier,
    textAlign = textAlign,
    color = color,
    fontSize = fontSize,
    maxLines = maxLines,
    onClick = onClick
)

@KdePortraitThemePreviews
@Composable
private fun KdeTextsPreview() {
    KdeTheme(context = LocalContext.current) {
        Column(modifier = Modifier.fillMaxWidth()) {
            KdeBodySmallText(
                text = "KdeBodySmallText",
                color = MaterialTheme.colorScheme.error,
                fontSize = 14.sp,
                maxLines = 1,
                modifier = Modifier.padding(all = 16.dp),
            )
            KdeBodyMediumText(
                text = "KdeBodyMediumText",
                modifier = Modifier.padding(all = 16.dp),
            )
            KdeBodyLargeText(
                text = "KdeBodyLargeText",
                modifier = Modifier.padding(all = 16.dp),
            )
            KdeTitleMediumText(
                text = "KdeTitleMediumText",
                modifier = Modifier.padding(all = 16.dp),
            )
        }
    }
}
