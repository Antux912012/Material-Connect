// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Provides the Material 3 Expressive interactive card container (`KdeCard`) with spring-morphing
 * corner geometry, tonal surface elevation, and press scale feedback.
 *
 * Architecture Position:
 * UI Presentation Component Layer (`org.kde.kdeconnect.ui.compose.components`).
 * Upstream callers: `PairingScreen`, `ShareScreen`, and device list views.
 * Downstream dependencies: `androidx.compose.material3.Card` and `androidx.compose.animation.core`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Composable UI component evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardColors
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.PreviewLightDark
import androidx.compose.ui.unit.dp
import org.kde.kdeconnect.ui.compose.KdeTheme

/**
 * Renders a Material 3 Expressive clickable card that morphs its corner radius (`24.dp` resting
 * to `16.dp` pressed) and scales (`0.98f`) using spring physics.
 *
 * @param modifier Outer layout [Modifier].
 * @param content Card body composable lambda.
 * @param onClick Action invoked when the user taps the card.
 * @param colors Material 3 [CardColors] (defaults to `surfaceContainer` tonal surface).
 */
@Composable
fun KdeCard(
    modifier: Modifier = Modifier,
    content: @Composable (ColumnScope.() -> Unit),
    onClick: () -> Unit,
    colors: CardColors = CardDefaults.cardColors(
        containerColor = MaterialTheme.colorScheme.surfaceContainer,
        contentColor = MaterialTheme.colorScheme.onSurface,
    ),
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 24.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ExpressiveCardCornerMorph"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ExpressiveCardScale"
    )

    Card(
        onClick = onClick,
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            },
        shape = RoundedCornerShape(cornerRadius),
        colors = colors,
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp,
        ),
        interactionSource = interactionSource,
        content = content
    )
}

@PreviewLightDark
@Composable
private fun KdeCardPreview() {
    KdeTheme(context = LocalContext.current) {
        KdeCard(
            modifier = Modifier.fillMaxWidth(),
            content = {
                Text(
                    text = "A very long device name that might wrap into multiple lines",
                    modifier = Modifier.padding(all = 16.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            onClick = { /* Do nothing */ }
        )
    }
}
