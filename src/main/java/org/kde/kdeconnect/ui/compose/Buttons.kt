// SPDX-FileCopyrightText: 2023 Dmitry Yudin <dgyudin@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Provides Material 3 Expressive buttons with spring-driven corner-radius morphing and press scale feedback.
 *
 * Architecture Position:
 * UI Presentation Layer (`org.kde.kdeconnect.ui.compose`).
 * Upstream callers: `PresenterActivity`, `CustomDevicesScreen`, and plugin screens.
 * Downstream dependencies: `androidx.compose.material3` and `androidx.compose.animation.core`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Composable UI components evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Build
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow.Companion.Ellipsis
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

/**
 * Renders a Material 3 Expressive text button with optional leading icon.
 */
@Composable
fun KdeTextButton(
    onClick: () -> Unit,
    modifier: Modifier,
    text: String,
    enabled: Boolean = true,
    contentPadding: PaddingValues = PaddingValues(16.dp),
    iconLeft: ImageVector? = null,
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = MaterialTheme.shapes.large,
        contentPadding = contentPadding,
        content = {
            iconLeft?.let {
                Icon(imageVector = it, contentDescription = null)
                Spacer(Modifier.width(12.dp))
            }
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    )
}

/**
 * Renders a Material 3 Expressive filled button whose corner radius morphs via bouncy spring
 * physics (`28.dp` resting to `16.dp` pressed) alongside a subtle press scale (`0.97f`).
 */
@Composable
fun KdeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    text: String? = null,
    contentDescription: String? = null,
    icon: ImageVector? = null,
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerSize by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "ExpressiveButtonCornerMorph"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.97f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "ExpressiveButtonScale"
    )

    Button(
        onClick = onClick,
        modifier = modifier.graphicsLayer {
            scaleX = scale
            scaleY = scale
        },
        shape = RoundedCornerShape(cornerSize),
        colors = colors,
        interactionSource = interactionSource,
        content = {
            icon?.let {
                Icon(imageVector = it, contentDescription = contentDescription ?: text)
                if (text != null) {
                    Spacer(Modifier.width(8.dp))
                }
            }
            text?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.labelLarge,
                    maxLines = 1,
                    overflow = Ellipsis
                )
            }
        }
    )
}

@Preview
@Composable
fun IconButtonPreview() {
    KdeButton(
        {},
        Modifier.width(140.dp),
        ButtonDefaults.buttonColors(Color.Gray, Color.DarkGray),
        "Button Text",
        null,
        Icons.Default.Build,
    )
}
