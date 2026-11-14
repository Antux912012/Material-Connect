// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Renders Material 3 Expressive section headers with a leading primary accent pill bar.
 *
 * Architecture Position:
 * UI Presentation Component Layer (`org.kde.kdeconnect.ui.compose.components`).
 * Upstream callers: `PairingScreen`, `ShareScreen`.
 * Downstream dependencies: `androidx.compose.material3`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Composable UI component evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import org.kde.kdeconnect.ui.compose.KdeTheme
import org.kde.kdeconnect_tp.R

@Composable
fun SectionHeader(title: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
            .padding(top = 20.dp, bottom = 8.dp, start = 8.dp, end = 8.dp)
            .semantics { heading() },
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(18.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Spacer(modifier = Modifier.width(10.dp))
        KdeTitleMediumText(
            text = title,
            color = MaterialTheme.colorScheme.primary,
        )
    }
}

@KdePortraitThemePreviews
@Composable
private fun SectionHeaderPreview() {
    KdeTheme(context = LocalContext.current) {
        SectionHeader(title = stringResource(id = R.string.category_connected_devices))
    }
}
