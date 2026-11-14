// SPDX-FileCopyrightText: 2026 Tanish Ranjan <tanishranjan4@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Renders the Material 3 Expressive About screen with hero branding, tonal quick-action pills,
 * and contributor cards.
 *
 * Architecture Position:
 * UI Presentation Screen Layer (`org.kde.kdeconnect.ui.compose.screen.about`).
 * Upstream callers: `AboutFragment`.
 * Downstream dependencies: `AboutData`, `KdeTheme`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Stateless Composable UI hierarchy evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.screen.about

import android.content.ClipData
import android.content.ClipboardManager
import android.os.SystemClock
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.getSystemService
import org.kde.kdeconnect.extensions.safeDrawingBottomPadding
import org.kde.kdeconnect.ui.about.AboutData
import org.kde.kdeconnect.ui.about.AboutPerson
import org.kde.kdeconnect.ui.about.applicationAboutData
import org.kde.kdeconnect.ui.compose.KdeTheme
import org.kde.kdeconnect.ui.compose.components.KdeThemePreviews
import org.kde.kdeconnect_tp.R

@Composable
fun AboutScreen(
    aboutData: AboutData,
    onEasterEggTriggered: () -> Unit,
    onReportBugClicked: () -> Unit,
    onDonateClicked: () -> Unit,
    onSourceCodeClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    onAboutKdeClicked: () -> Unit,
    onWebsiteClicked: () -> Unit
) {
    val bottomPadding = safeDrawingBottomPadding()

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(16.dp),
        contentPadding = PaddingValues(
            start = 16.dp,
            end = 16.dp,
            top = 16.dp,
            bottom = bottomPadding.calculateBottomPadding() + 16.dp
        )
    ) {
        item {
            AppInfoCard(
                aboutData = aboutData,
                onEasterEggTriggered = onEasterEggTriggered
            )
        }

        item {
            ActionButtons(
                aboutData = aboutData,
                onReportBugClicked = onReportBugClicked,
                onDonateClicked = onDonateClicked,
                onSourceCodeClicked = onSourceCodeClicked,
                onLicensesClicked = onLicensesClicked,
                onAboutKdeClicked = onAboutKdeClicked,
                onWebsiteClicked = onWebsiteClicked
            )
        }

        item {
            AuthorsCard(aboutData = aboutData)
        }
    }
}

@Composable
private fun AppInfoCard(
    aboutData: AboutData,
    onEasterEggTriggered: () -> Unit
) {
    var tapCount by remember { mutableIntStateOf(0) }
    var firstTapMillis by remember { mutableStateOf<Long?>(null) }
    val context = LocalContext.current

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
        ),
        onClick = {
            val clipboard = context.getSystemService<ClipboardManager>()!!
            clipboard.setPrimaryClip(ClipData.newPlainText("Version", aboutData.versionName))
            Toast.makeText(context, R.string.clipboard_toast, Toast.LENGTH_SHORT).show()

            val currentMillis = SystemClock.elapsedRealtime()
            if (firstTapMillis == null) {
                firstTapMillis = currentMillis
            }

            tapCount++

            if (tapCount == 3) {
                if (currentMillis - firstTapMillis!! <= 500) {
                    onEasterEggTriggered()
                }
                tapCount = 0
                firstTapMillis = null
            } else if (currentMillis - firstTapMillis!! > 500) {
                tapCount = 1
                firstTapMillis = currentMillis
            }
        }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = aboutData.icon),
                    contentDescription = null,
                    modifier = Modifier.size(44.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = stringResource(aboutData.name),
                    style = MaterialTheme.typography.headlineSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.secondaryContainer
                ) {
                    Text(
                        text = stringResource(id = R.string.version, aboutData.versionName),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun ActionButtons(
    aboutData: AboutData,
    onReportBugClicked: () -> Unit,
    onDonateClicked: () -> Unit,
    onSourceCodeClicked: () -> Unit,
    onLicensesClicked: () -> Unit,
    onAboutKdeClicked: () -> Unit,
    onWebsiteClicked: () -> Unit
) {
    val buttons = remember(aboutData) {
        val list = mutableListOf<@Composable () -> Unit>()
        if (aboutData.bugURL != null) {
            list.add {
                ActionIconTextButton(
                    textRes = R.string.report_bug,
                    iconRes = R.drawable.ic_baseline_bug_report_24,
                    onClick = onReportBugClicked
                )
            }
        }
        if (aboutData.donateURL != null) {
            list.add {
                ActionIconTextButton(
                    textRes = R.string.donate,
                    iconRes = R.drawable.ic_baseline_attach_money_24,
                    onClick = onDonateClicked
                )
            }
        }
        if (aboutData.sourceCodeURL != null) {
            list.add {
                ActionIconTextButton(
                    textRes = R.string.source_code,
                    iconRes = R.drawable.ic_baseline_code_24,
                    onClick = onSourceCodeClicked
                )
            }
        }

        list.add {
            ActionIconTextButton(
                textRes = R.string.licenses,
                iconRes = R.drawable.ic_baseline_gavel_24,
                onClick = onLicensesClicked
            )
        }
        list.add {
            ActionIconTextButton(
                textRes = R.string.about_kde,
                iconRes = R.drawable.ic_kde_24dp,
                onClick = onAboutKdeClicked
            )
        }

        if (aboutData.websiteURL != null) {
            list.add {
                ActionIconTextButton(
                    textRes = R.string.website,
                    iconRes = R.drawable.ic_baseline_web_24,
                    onClick = onWebsiteClicked
                )
            }
        }
        list
    }

    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val buttonWidth = 88.dp
        val totalNeededWidth = buttonWidth.times(buttons.size)

        val maxItemsInRow = if (totalNeededWidth <= this.maxWidth) buttons.size else 3

        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalArrangement = Arrangement.spacedBy(12.dp),
            maxItemsInEachRow = maxItemsInRow
        ) {
            buttons.forEach { button -> button() }
        }
    }
}

@Composable
private fun ActionIconTextButton(
    @StringRes textRes: Int,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }

    Column(
        modifier = Modifier
            .size(width = 88.dp, height = 84.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(MaterialTheme.colorScheme.surfaceContainer)
            .clickable(
                interactionSource = interactionSource,
                indication = ripple(
                    color = MaterialTheme.colorScheme.primary,
                    bounded = true
                ),
                onClick = onClick
            )
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = stringResource(id = textRes),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2
        )
    }
}

@Composable
private fun AuthorsCard(aboutData: AboutData) {
    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp)
        ) {
            Text(
                text = stringResource(id = R.string.authors),
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))

            aboutData.authors.forEach { author ->
                AuthorItemRow(author = author)
            }

            aboutData.authorsFooterText?.let { footerResId ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(id = footerResId),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }
        }
    }
}

@Composable
private fun AuthorItemRow(author: AboutPerson) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(
            text = author.name,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (author.task != null) {
            Text(
                text = stringResource(id = author.task),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@KdeThemePreviews
@Composable
private fun AboutScreenPreview() {
    val sampleAboutData = applicationAboutData
    KdeTheme(context = LocalContext.current) {
        Surface {
            AboutScreen(
                aboutData = sampleAboutData,
                onEasterEggTriggered = {},
                onReportBugClicked = {},
                onDonateClicked = {},
                onSourceCodeClicked = {},
                onLicensesClicked = {},
                onAboutKdeClicked = {},
                onWebsiteClicked = {}
            )
        }
    }
}
