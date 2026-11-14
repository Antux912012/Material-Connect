// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Renders the Material 3 Expressive device target selector for outgoing file and URL sharing.
 *
 * Architecture Position:
 * UI Presentation Screen Layer (`org.kde.kdeconnect.ui.compose.screen.share`).
 * Upstream callers: `ShareActivity`.
 * Downstream dependencies: `KdeCard`, `SectionHeader`, `DeviceUiModel`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Stateless Composable UI hierarchy evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.screen.share

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.unit.dp
import org.kde.kdeconnect.extensions.safeDrawingBottomPadding
import org.kde.kdeconnect.ui.compose.KdeTheme
import org.kde.kdeconnect.ui.compose.components.KdeCard
import org.kde.kdeconnect.ui.compose.components.KdeThemePreviews
import org.kde.kdeconnect.ui.compose.components.SectionHeader
import org.kde.kdeconnect.ui.compose.model.device.DeviceUiModel
import org.kde.kdeconnect_tp.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShareScreen(
    devices: List<DeviceUiModel>,
    intentHasUrl: Boolean,
    isRefreshing: Boolean,
    onDeviceClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    val state = rememberLazyListState()
    val sectionTitle = if (intentHasUrl) {
        "${stringResource(id = R.string.unreachable_device_url_share_text)} ${stringResource(id = R.string.share_to)}"
    } else {
        stringResource(id = R.string.share_to)
    }

    ShareScreenContent(
        state = state,
        isRefreshing = isRefreshing,
        sectionTitle = sectionTitle,
        devices = devices,
        onDeviceClick = onDeviceClick,
        onRefresh = onRefresh
    )
}

@Composable
private fun ShareScreenContent(
    state: LazyListState,
    isRefreshing: Boolean,
    sectionTitle: String,
    devices: List<DeviceUiModel>,
    onDeviceClick: (String) -> Unit,
    onRefresh: () -> Unit
) {
    PullToRefreshBox(
        isRefreshing = isRefreshing,
        onRefresh = onRefresh,
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            contentPadding = safeDrawingBottomPadding(),
            state = state
        ) {
            item {
                SectionHeader(title = sectionTitle)
            }
            items(
                items = devices,
                key = { device -> device.id }
            ) { device ->
                KdeCard(
                    modifier = Modifier.fillMaxWidth(),
                    content = { ShareScreenCardContent(device = device) },
                    onClick = { onDeviceClick(device.id) }
                )
            }
        }
    }
}

@Composable
private fun ShareScreenCardContent(device: DeviceUiModel) {
    val avatarBgColor = if (device.isReachable) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val avatarIconColor = if (device.isReachable) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .defaultMinSize(minHeight = 68.dp)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (device.icon > 0) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(avatarBgColor),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    imageVector = ImageVector.vectorResource(id = device.icon),
                    contentDescription = null,
                    colorFilter = ColorFilter.tint(color = avatarIconColor),
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = device.name,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (!device.isReachable) {
                Text(
                    text = stringResource(id = R.string.runcommand_notreachable),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1
                )
            }
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(20.dp)
        )
    }
}

@KdeThemePreviews
@Composable
private fun ShareScreenPreview() {
    KdeTheme(context = LocalContext.current) {
        ShareScreenContent(
            state = rememberLazyListState(),
            isRefreshing = false,
            sectionTitle = stringResource(id = R.string.share_to),
            devices = listOf(
                DeviceUiModel(
                    id = "_2504584b_6aa2_3cd6_bd1b_5e958aa6cd23_",
                    icon = R.drawable.ic_device_laptop_32dp,
                    name = "Device 1",
                    summaryRes = 0,
                    isReachable = true,
                    isPaired = true,
                ),
                DeviceUiModel(
                    id = "_2504584b_6aa2_3cd6_bd1b_5e958aa6cd24_",
                    icon = R.drawable.ic_device_phone_32dp,
                    name = "Device 2",
                    summaryRes = 0,
                    isReachable = true,
                    isPaired = true,
                )
            ),
            onDeviceClick = { /* Do nothing */ },
            onRefresh = { /* Do nothing */ }
        )
    }
}
