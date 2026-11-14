// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Renders the Material 3 Expressive device dashboard with a Bento plugin action grid and
 * actionable tonal permission cards.
 *
 * Architecture Position:
 * UI Presentation Screen Layer (`org.kde.kdeconnect.ui.compose.screen.device`).
 * Upstream callers: `DeviceFragment`.
 * Downstream dependencies: `Plugin`, `KdeTheme`, `SectionHeader`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Stateless Composable UI hierarchy evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.screen.device

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalResources
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.kde.kdeconnect.extensions.safeDrawingBottomPadding
import org.kde.kdeconnect.plugins.Plugin
import org.kde.kdeconnect.plugins.clipboard.ClipboardPlugin
import org.kde.kdeconnect.plugins.mpris.MprisPlugin
import org.kde.kdeconnect.plugins.ping.PingPlugin
import org.kde.kdeconnect.plugins.presenter.PresenterPlugin
import org.kde.kdeconnect.plugins.runcommand.RunCommandPlugin
import org.kde.kdeconnect.ui.compose.KdeTheme
import org.kde.kdeconnect.ui.compose.components.KdeThemePreviews
import org.kde.kdeconnect.ui.compose.components.SectionHeader
import org.kde.kdeconnect_tp.R

@Composable
fun PluginsScreen(
    pluginsWithButtons: List<Plugin.PluginUiButton>,
    pluginsNeedPermissions: List<Plugin>,
    pluginsNeedOptionalPermissions: List<Plugin>,
    onButtonClick: (Plugin.PluginUiButton) -> Unit,
    actionNeedPermissions: (plugin: Plugin) -> Unit,
    actionNeedOptionalPermissions: (plugin: Plugin) -> Unit,
) {
    PluginsScreenContent(
        pluginsWithButtons = pluginsWithButtons,
        pluginsNeedPermissions = pluginsNeedPermissions,
        pluginsNeedOptionalPermissions = pluginsNeedOptionalPermissions,
        onButtonClick = onButtonClick,
        actionNeedPermissions = actionNeedPermissions,
        actionNeedOptionalPermissions = actionNeedOptionalPermissions,
    )
}

@Composable
private fun PluginsScreenContent(
    pluginsWithButtons: List<Plugin.PluginUiButton>,
    pluginsNeedPermissions: List<Plugin>,
    pluginsNeedOptionalPermissions: List<Plugin>,
    onButtonClick: (Plugin.PluginUiButton) -> Unit,
    actionNeedPermissions: (plugin: Plugin) -> Unit,
    actionNeedOptionalPermissions: (plugin: Plugin) -> Unit,
) {
    val bottomPadding = safeDrawingBottomPadding().calculateBottomPadding()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(state = rememberScrollState())
                .padding(top = 12.dp, bottom = bottomPadding + 16.dp)
        ) {
            val numColumns = LocalResources.current.getInteger(R.integer.plugins_columns)

            PluginButtons(
                buttons = pluginsWithButtons,
                numColumns = numColumns,
                onButtonClick = onButtonClick
            )
            Spacer(modifier = Modifier.height(12.dp))
            if (pluginsNeedPermissions.isNotEmpty()) {
                PluginsWithoutPermissions(
                    title = stringResource(id = R.string.plugins_need_permission),
                    plugins = pluginsNeedPermissions,
                    isMandatory = true,
                    action = actionNeedPermissions,
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
            if (pluginsNeedOptionalPermissions.isNotEmpty()) {
                PluginsWithoutPermissions(
                    title = stringResource(id = R.string.plugins_need_optional_permission),
                    plugins = pluginsNeedOptionalPermissions,
                    isMandatory = false,
                    action = actionNeedOptionalPermissions
                )
            }
        }
    }
}

@Composable
private fun PluginButtons(
    buttons: List<Plugin.PluginUiButton>,
    numColumns: Int,
    onButtonClick: (Plugin.PluginUiButton) -> Unit
) {
    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        val buttonIter = buttons.iterator()

        while (buttonIter.hasNext()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                repeat(times = numColumns) {
                    if (buttonIter.hasNext()) {
                        val button = buttonIter.next()

                        PluginButton(
                            button = button,
                            modifier = Modifier.weight(1f),
                            onClick = { onButtonClick(button) }
                        )
                    } else {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PluginButton(
    button: Plugin.PluginUiButton,
    modifier: Modifier,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val cornerRadius by animateDpAsState(
        targetValue = if (isPressed) 16.dp else 28.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMediumLow
        ),
        label = "PluginTileCornerMorph"
    )
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.96f else 1.0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "PluginTileScale"
    )

    Card(
        shape = RoundedCornerShape(cornerRadius),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            contentColor = MaterialTheme.colorScheme.onSurface,
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 1.dp,
            pressedElevation = 4.dp,
        ),
        interactionSource = interactionSource,
        modifier = modifier
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
            .semantics { role = Role.Button },
        onClick = onClick
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = button.iconRes),
                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                    modifier = Modifier.size(24.dp),
                    contentDescription = null
                )
            }
            Text(
                text = button.name,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                minLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun PluginsWithoutPermissions(
    title: String,
    plugins: Collection<Plugin>,
    isMandatory: Boolean,
    action: (plugin: Plugin) -> Unit
) {
    val badgeContainer = if (isMandatory) {
        MaterialTheme.colorScheme.errorContainer
    } else {
        MaterialTheme.colorScheme.secondaryContainer
    }
    val badgeContent = if (isMandatory) {
        MaterialTheme.colorScheme.onErrorContainer
    } else {
        MaterialTheme.colorScheme.onSecondaryContainer
    }

    Column(modifier = Modifier.padding(horizontal = 16.dp)) {
        SectionHeader(
            title = title,
            modifier = Modifier.semantics { heading() }
        )
        plugins.forEach { plugin ->
            Surface(
                shape = MaterialTheme.shapes.large,
                color = MaterialTheme.colorScheme.surfaceContainer,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(MaterialTheme.shapes.large)
                    .clickable { action(plugin) }
                    .semantics { role = Role.Button }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(badgeContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Warning,
                            contentDescription = null,
                            tint = badgeContent,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Text(
                        text = plugin.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.weight(1f)
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@KdeThemePreviews
@Composable
private fun PluginsScreenPreview() {
    KdeTheme(context = LocalContext.current) {
        val mprisPlugin = MprisPlugin().also { it.setContext(LocalContext.current, null) }
        val runCommandPlugin = RunCommandPlugin().also { it.setContext(LocalContext.current, null) }
        val presenterPlugin = PresenterPlugin().also { it.setContext(LocalContext.current, null) }
        val pingPlugin = PingPlugin().also { it.setContext(LocalContext.current, null) }
        val clipboardPlugin = ClipboardPlugin().also { it.setContext(LocalContext.current, null) }

        val pluginsWithButtons = listOf(pingPlugin, mprisPlugin, presenterPlugin, runCommandPlugin)
        val pluginsWithoutPermissions = listOf(clipboardPlugin, presenterPlugin)
        val pluginsWithoutOptionalPermissions = listOf(mprisPlugin, presenterPlugin)

        PluginsScreenContent(
            pluginsWithButtons = pluginsWithButtons.flatMap { plugin -> plugin.getUiButtons() },
            pluginsNeedPermissions = pluginsWithoutPermissions,
            pluginsNeedOptionalPermissions = pluginsWithoutOptionalPermissions,
            onButtonClick = { /* Do nothing */ },
            actionNeedPermissions = { /* Do nothing */ },
            actionNeedOptionalPermissions = { /* Do nothing */ },
        )
    }
}
