// SPDX-FileCopyrightText: 2023 Dmitry Yudin <dgyudin@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Provides the Material 3 Expressive top application bar with tonal surface styling and subtitle support.
 *
 * Architecture Position:
 * UI Presentation Layer (`org.kde.kdeconnect.ui.compose`).
 * Upstream callers: `PresenterActivity`, `RunCommandScreen`, `CustomDevicesScreen`, `LicensesScreen`.
 * Downstream dependencies: `androidx.compose.material3.TopAppBar`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Composable UI component evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.RowScope
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarColors
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import org.kde.kdeconnect_tp.R

/**
 * Renders a Material 3 Expressive top app bar with optional subtitle and navigation icon.
 *
 * @param title Primary screen title text.
 * @param subTitle Optional secondary device or context subtitle.
 * @param navIcon Navigation icon vector (defaults to auto-mirrored back arrow).
 * @param navIconDescription Accessibility content description for the navigation icon.
 * @param navIconOnClick Callback invoked when the navigation button is pressed.
 * @param actions Trailing action icons or overflow menu content.
 * @param colors TopAppBar color tokens.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KdeTopAppBar(
    title: String = stringResource(R.string.kde_connect),
    subTitle: String? = null,
    navIcon: ImageVector = Icons.AutoMirrored.Filled.ArrowBack,
    navIconDescription: String = "",
    navIconOnClick: () -> Unit,
    actions: @Composable (RowScope.() -> Unit) = {},
    colors: TopAppBarColors = TopAppBarDefaults.topAppBarColors(
        containerColor = MaterialTheme.colorScheme.surface,
        scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer,
        navigationIconContentColor = MaterialTheme.colorScheme.onSurface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        actionIconContentColor = MaterialTheme.colorScheme.onSurfaceVariant,
    ),
) {
    TopAppBar(
        colors = colors,
        navigationIcon = {
            IconButton(
                onClick = navIconOnClick,
                content = { Icon(navIcon, navIconDescription) }
            )
        },
        title = {
            if (subTitle == null) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleLarge,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            } else {
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                    Text(
                        text = subTitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                    )
                }
            }
        },
        actions = actions,
    )
}
