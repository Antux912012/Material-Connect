// SPDX-FileCopyrightText: 2024 Albert Vaca Cintora <albertvaka@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Defines the Material 3 Expressive design system for KDE Connect, including dynamic color
 * schemes, fallback Breeze-Expressive tonal palettes, expressive shape tokens, and typography.
 *
 * Architecture Position:
 * UI Presentation Root (`org.kde.kdeconnect.ui.compose`).
 * Upstream callers: `DeviceFragment`, `PairingFragment`, `ShareActivity`, `PresenterActivity`,
 * `RunCommandScreen`, `AboutFragment`, `CustomDevicesActivity`, and `LicensesActivity`.
 * Downstream dependencies: `androidx.compose.material3`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Composable UI theme wrapper evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose

import android.content.Context
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Curated Material 3 Expressive light color scheme inspired by Google Pixel & KDE Breeze accents
 * for devices running Android 11 (API 30) and earlier.
 */
private val ExpressiveLightColorScheme: ColorScheme = lightColorScheme(
    primary = Color(0xFF006493),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFFCAE6FF),
    onPrimaryContainer = Color(0xFF001E30),
    secondary = Color(0xFF4F606E),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD2E5F5),
    onSecondaryContainer = Color(0xFF0B1D29),
    tertiary = Color(0xFF63597C),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFE9DDFF),
    onTertiaryContainer = Color(0xFF1F1635),
    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),
    background = Color(0xFFF6FAFE),
    onBackground = Color(0xFF181C20),
    surface = Color(0xFFF6FAFE),
    onSurface = Color(0xFF181C20),
    surfaceVariant = Color(0xFFDDE3EA),
    onSurfaceVariant = Color(0xFF41474D),
    outline = Color(0xFF71787E),
    outlineVariant = Color(0xFFC1C7CE),
    surfaceContainerLowest = Color(0xFFFFFFFF),
    surfaceContainerLow = Color(0xFFF0F4F8),
    surfaceContainer = Color(0xFFEAEEF3),
    surfaceContainerHigh = Color(0xFFE5E9ED),
    surfaceContainerHighest = Color(0xFFDFE3E7),
)

/**
 * Curated Material 3 Expressive dark color scheme with rich tonal surface containers.
 */
private val ExpressiveDarkColorScheme: ColorScheme = darkColorScheme(
    primary = Color(0xFF8ECDFF),
    onPrimary = Color(0xFF00344F),
    primaryContainer = Color(0xFF004B70),
    onPrimaryContainer = Color(0xFFCAE6FF),
    secondary = Color(0xFFB6C9D8),
    onSecondary = Color(0xFF21323F),
    secondaryContainer = Color(0xFF374956),
    onSecondaryContainer = Color(0xFFD2E5F5),
    tertiary = Color(0xFFCDC0E9),
    onTertiary = Color(0xFF342B4B),
    tertiaryContainer = Color(0xFF4B4163),
    onTertiaryContainer = Color(0xFFE9DDFF),
    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),
    background = Color(0xFF101417),
    onBackground = Color(0xFFE0E3E8),
    surface = Color(0xFF101417),
    onSurface = Color(0xFFE0E3E8),
    surfaceVariant = Color(0xFF41474D),
    onSurfaceVariant = Color(0xFFC1C7CE),
    outline = Color(0xFF8B9198),
    outlineVariant = Color(0xFF41474D),
    surfaceContainerLowest = Color(0xFF0B0F12),
    surfaceContainerLow = Color(0xFF181C20),
    surfaceContainer = Color(0xFF1C2024),
    surfaceContainerHigh = Color(0xFF262A2E),
    surfaceContainerHighest = Color(0xFF313539),
)

/**
 * Material 3 Expressive shape scale featuring generous squircle/pill geometry.
 */
val ExpressiveShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(14.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp),
    extraLarge = RoundedCornerShape(36.dp),
)

/**
 * Material 3 Expressive typography scale with clear weight contrast and scannable line heights.
 */
val ExpressiveTypography = Typography(
    displaySmall = TextStyle(
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp,
        lineHeight = 44.sp,
        letterSpacing = (-0.25).sp,
    ),
    headlineMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp,
    ),
    headlineSmall = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp,
        lineHeight = 32.sp,
        letterSpacing = 0.sp,
    ),
    titleLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp,
    ),
    titleMedium = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.25.sp,
    ),
    bodyMedium = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.2.sp,
    ),
    bodySmall = TextStyle(
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.3.sp,
    ),
    labelLarge = TextStyle(
        fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.4.sp,
    ),
)

/**
 * Applies the Material 3 Expressive theme to [content], selecting Android 12+ dynamic wallpaper
 * colors when available or the Breeze-Expressive fallback color scheme on older Android versions.
 *
 * @param context Android [Context] used to resolve dynamic system colors on API 31+.
 * @param content Composable hierarchy to wrap in [MaterialTheme].
 */
@Composable
fun KdeTheme(context: Context, content: @Composable () -> Unit) {
    val isDark = isSystemInDarkTheme()
    val colorScheme = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
    } else {
        if (isDark) ExpressiveDarkColorScheme else ExpressiveLightColorScheme
    }
    MaterialTheme(
        colorScheme = colorScheme,
        shapes = ExpressiveShapes,
        typography = ExpressiveTypography,
        content = content,
    )
}
