// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Renders the Material 3 Expressive device discovery and pairing dashboard (`PairingScreen`).
 *
 * Architecture Position:
 * UI Presentation Screen Layer (`org.kde.kdeconnect.ui.compose.screen.pairing`).
 * Upstream callers: `PairingFragment`.
 * Downstream dependencies: `PairingUiState`, `DeviceUiModel`, `KdeCard`, `SectionHeader`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Stateless Composable UI hierarchy driven by immutable `PairingUiState`.
 */
package org.kde.kdeconnect.ui.compose.screen.pairing

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshState
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.kde.kdeconnect.extensions.safeDrawingBottomPadding
import org.kde.kdeconnect.ui.compose.KdeTheme
import org.kde.kdeconnect.ui.compose.components.KdeBodyLargeText
import org.kde.kdeconnect.ui.compose.components.KdeBodyMediumText
import org.kde.kdeconnect.ui.compose.components.KdeBodySmallText
import org.kde.kdeconnect.ui.compose.components.KdeCard
import org.kde.kdeconnect.ui.compose.components.KdeThemePreviews
import org.kde.kdeconnect.ui.compose.components.SectionHeader
import org.kde.kdeconnect.ui.compose.model.device.DeviceUiModel
import org.kde.kdeconnect_tp.R

@Composable
fun PairingScreen(
    uiState: PairingUiState,
    onClick: (String) -> Unit,
    onWifiSettingsClick: () -> Unit = {},
    onNotificationSettingsClick: () -> Unit = {},
    onLocalNetworkPermissionClick: () -> Unit = {},
    onRefresh: () -> Unit = {}
) {
    val lazyListState = rememberLazyListState()
    val pullRefreshState = rememberPullToRefreshState()

    PairingScreenContent(
        lazyListState = lazyListState,
        pullRefreshState = pullRefreshState,
        uiState = uiState,
        onClick = onClick,
        onWifiSettingsClick = onWifiSettingsClick,
        onNotificationSettingsClick = onNotificationSettingsClick,
        onLocalNetworkPermissionClick = onLocalNetworkPermissionClick,
        onRefresh = onRefresh
    )
}

@Composable
private fun PairingScreenContent(
    lazyListState: LazyListState,
    pullRefreshState: PullToRefreshState,
    uiState: PairingUiState,
    onClick: (String) -> Unit,
    onWifiSettingsClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onLocalNetworkPermissionClick: () -> Unit,
    onRefresh: () -> Unit = {}
) {
    PullToRefreshBox(
        isRefreshing = uiState.isRefreshing,
        onRefresh = onRefresh,
        state = pullRefreshState
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            contentPadding = safeDrawingBottomPadding(),
            state = lazyListState
        ) {
            // Explanations & Permission Banners
            item {
                Spacer(modifier = Modifier.height(8.dp))
                PairingExplanations(
                    uiState = uiState,
                    onWifiSettingsClick = onWifiSettingsClick,
                    onNotificationSettingsClick = onNotificationSettingsClick,
                    onLocalNetworkPermissionClick = onLocalNetworkPermissionClick
                )
            }

            // Connected devices
            item {
                SectionHeader(title = stringResource(id = R.string.category_connected_devices))
            }
            if (uiState.connected.isEmpty()) {
                item {
                    EmptyPlaceholder()
                }
            } else {
                itemsIndexed(
                    items = uiState.connected,
                    key = { _, connectedDevice -> connectedDevice.id }
                ) { _, connectedDevice ->
                    CardContent(
                        device = connectedDevice,
                        onClick = onClick
                    )
                }
            }

            // Available devices
            if (uiState.available.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(id = R.string.category_not_paired_devices))
                }
                itemsIndexed(
                    items = uiState.available,
                    key = { _, availableDevice -> availableDevice.id }
                ) { _, availableDevice ->
                    CardContent(
                        device = availableDevice,
                        onClick = onClick
                    )
                }
            }

            // Remembered devices
            if (uiState.remembered.isNotEmpty()) {
                item {
                    SectionHeader(title = stringResource(id = R.string.category_remembered_devices))
                }
                itemsIndexed(
                    items = uiState.remembered,
                    key = { _, rememberedDevice -> rememberedDevice.id }
                ) { _, rememberedDevice ->
                    CardContent(
                        device = rememberedDevice,
                        onClick = onClick
                    )
                }
            }
        }
    }
}

@Composable
private fun PairingExplanations(
    uiState: PairingUiState,
    onWifiSettingsClick: () -> Unit,
    onNotificationSettingsClick: () -> Unit,
    onLocalNetworkPermissionClick: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        if (!uiState.hasLocalNetworkPermission) {
            PairingExplanationRow(
                text = stringResource(id = R.string.pairing_missing_local_network_permission),
                icon = R.drawable.ic_warning,
                onClick = onLocalNetworkPermissionClick
            )
        }

        if (!uiState.hasNotificationsPermission) {
            PairingExplanationRow(
                text = stringResource(R.string.no_notifications),
                icon = R.drawable.ic_warning,
                onClick = onNotificationSettingsClick
            )
        }

        if (!uiState.hasLocalNetworkPermission || !uiState.hasNotificationsPermission) {
            return
        }

        if (uiState.hasDuplicateNames) {
            DuplicateNamesWarning()
        }

        val someDevicesReachable = uiState.available.isNotEmpty() || uiState.connected.isNotEmpty()

        if (someDevicesReachable || uiState.isWifiAvailable) {
            if (uiState.isTrustedNetwork) {
                PairingExplanationRow(text = stringResource(R.string.pairing_description))
            } else {
                PairingExplanationRow(
                    text = stringResource(R.string.on_non_trusted_message),
                    icon = R.drawable.ic_warning
                )
            }
        } else {
            PairingExplanationRow(
                text = stringResource(R.string.no_wifi),
                icon = R.drawable.ic_wifi,
                onClick = onWifiSettingsClick
            )
        }
    }
}

@Composable
fun PairingExplanationRow(
    text: String,
    icon: Int? = null,
    onClick: (() -> Unit)? = null
) {
    val isWarning = icon == R.drawable.ic_warning
    val containerColor = when {
        isWarning && onClick != null -> MaterialTheme.colorScheme.errorContainer
        isWarning -> MaterialTheme.colorScheme.tertiaryContainer
        onClick != null -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceContainerLow
    }
    val contentColor = when {
        isWarning && onClick != null -> MaterialTheme.colorScheme.onErrorContainer
        isWarning -> MaterialTheme.colorScheme.onTertiaryContainer
        onClick != null -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Surface(
        shape = MaterialTheme.shapes.large,
        color = containerColor,
        contentColor = contentColor,
        modifier = Modifier
            .fillMaxWidth()
            .clip(MaterialTheme.shapes.large)
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (icon != null) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .clip(CircleShape)
                        .background(contentColor.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = icon),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        colorFilter = ColorFilter.tint(color = contentColor)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
            }
            KdeBodyMediumText(
                text = text,
                color = contentColor,
                modifier = Modifier.weight(1f)
            )
            if (onClick != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = contentColor,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

@Composable
fun DuplicateNamesWarning() {
    PairingExplanationRow(
        text = stringResource(id = R.string.pairing_duplicate_names),
        icon = R.drawable.ic_warning,
    )
}

@Composable
fun EmptyPlaceholder() {
    Surface(
        shape = MaterialTheme.shapes.large,
        color = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
    ) {
        KdeBodySmallText(
            text = stringResource(R.string.device_list_empty),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 20.dp),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun CardContent(
    device: DeviceUiModel,
    onClick: (String) -> Unit
) {
    val cardColors = if (device.isReachable && device.isPaired) {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    } else {
        CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
            contentColor = MaterialTheme.colorScheme.onSurface,
        )
    }
    KdeCard(
        modifier = Modifier.fillMaxWidth(),
        colors = cardColors,
        content = {
            PairingScreenCardContent(device = device)
        },
        onClick = { onClick(device.id) }
    )
}

@Composable
private fun PairingScreenCardContent(device: DeviceUiModel) {
    val isConnected = device.isReachable && device.isPaired
    val avatarBgColor = when {
        isConnected -> MaterialTheme.colorScheme.primaryContainer
        device.isReachable -> MaterialTheme.colorScheme.secondaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }
    val avatarIconColor = when {
        isConnected -> MaterialTheme.colorScheme.onPrimaryContainer
        device.isReachable -> MaterialTheme.colorScheme.onSecondaryContainer
        else -> MaterialTheme.colorScheme.onSurfaceVariant
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
            KdeBodyLargeText(
                text = device.name,
                color = MaterialTheme.colorScheme.onSurface,
            )
            if (device.summaryRes > 0) {
                Text(
                    text = stringResource(id = device.summaryRes),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    maxLines = 1,
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
private fun PreviewCompose() {
    KdeTheme(context = LocalContext.current) {
        val lazyListState = rememberLazyListState()
        val pullRefreshState = rememberPullToRefreshState()

        PairingScreenContent(
            lazyListState = lazyListState,
            pullRefreshState = pullRefreshState,
            uiState = PairingUiState(
                isWifiAvailable = true,
                hasNotificationsPermission = true,
                hasLocalNetworkPermission = true,
                isTrustedNetwork = true,
                hasDuplicateNames = true,
                connected = emptyList(),
                available = listOf(
                    DeviceUiModel(
                        id = "_2504584b_6aa2_3cd6_bd1b_5e958aa6cd23_",
                        icon = R.drawable.ic_device_laptop_32dp,
                        name = "Device 1",
                        summaryRes = 0,
                        isReachable = true,
                        isPaired = false
                    ),
                    DeviceUiModel(
                        id = "_2504584b_6aa2_3cd6_bd1b_5e958aa6cd24_",
                        icon = R.drawable.ic_device_desktop_32dp,
                        name = "Device 2",
                        summaryRes = R.string.protocol_version_newer,
                        isReachable = true,
                        isPaired = false
                    )
                ),
                remembered = emptyList(),
                isRefreshing = false
            ),
            onClick = { /* Do nothing */ },
            onWifiSettingsClick = { /* Do nothing */ },
            onNotificationSettingsClick = { /* Do nothing */ },
            onLocalNetworkPermissionClick = { /* Do nothing */ },
            onRefresh = { /* Do nothing */ }
        )
    }
}
