// SPDX-FileCopyrightText: 2026 Saul Cintero Chocarro <scintero@gmail.com>
// SPDX-FileCopyrightText: 2026 Antonio De Gaetano <https://github.com/Antux912012>
// SPDX-License-Identifier: GPL-2.0-only OR GPL-3.0-only OR LicenseRef-KDE-Accepted-GPL

/**
 * File Responsibility:
 * Renders the Material 3 Expressive cryptographic pairing and verification screen.
 *
 * Architecture Position:
 * UI Presentation Screen Layer (`org.kde.kdeconnect.ui.compose.screen.device`).
 * Upstream callers: `DeviceFragment`.
 * Downstream dependencies: `PairingHandler`, `KdeButton`, `KdeTheme`.
 *
 * Concurrency & Thread-Safety Constraints:
 * Stateless Composable UI hierarchy evaluated on the Compose UI composition thread.
 */
package org.kde.kdeconnect.ui.compose.screen.device

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.kde.kdeconnect.PairingHandler
import org.kde.kdeconnect.ui.compose.KdeButton
import org.kde.kdeconnect.ui.compose.KdeTheme
import org.kde.kdeconnect.ui.compose.components.KdeBodyMediumText
import org.kde.kdeconnect.ui.compose.components.KdeThemePreviews
import org.kde.kdeconnect.ui.compose.components.KdeTitleMediumText
import org.kde.kdeconnect_tp.R

@Composable
fun DevicePairingScreen(
    pairStatus: PairingHandler.PairState,
    verificationKey: String,
    pairMessage: String? = null,
    onRequestPairing: () -> Unit,
    onAcceptPairing: () -> Unit,
    onRejectPairing: () -> Unit
) {
    DevicePairingScreenContent(
        pairStatus = pairStatus,
        verificationKey = verificationKey,
        pairMessage = pairMessage,
        onRequestPairing = onRequestPairing,
        onAcceptPairing = onAcceptPairing,
        onRejectPairing = onRejectPairing
    )
}

@Composable
private fun DevicePairingScreenContent(
    pairStatus: PairingHandler.PairState,
    verificationKey: String,
    pairMessage: String? = null,
    onRequestPairing: () -> Unit,
    onAcceptPairing: () -> Unit,
    onRejectPairing: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(all = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            ElevatedCard(
                shape = MaterialTheme.shapes.large,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(all = 20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    val finalPairMessage = pairMessage ?: when (pairStatus) {
                        PairingHandler.PairState.NotPaired -> stringResource(id = R.string.device_not_paired)
                        else -> stringResource(id = R.string.pair_requested)
                    }

                    KdeTitleMediumText(
                        text = finalPairMessage,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    KdeBodyMediumText(
                        text = stringResource(id = R.string.pairing_explanation),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    if (pairStatus != PairingHandler.PairState.NotPaired && verificationKey.isNotEmpty()) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.secondaryContainer,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(12.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        painter = painterResource(id = R.drawable.ic_key),
                                        contentDescription = null,
                                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Text(
                                    text = verificationKey,
                                    fontFamily = FontFamily.Monospace,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp,
                                    letterSpacing = 1.5.sp,
                                    color = MaterialTheme.colorScheme.onSecondaryContainer
                                )
                            }
                        }
                    }

                    if (pairStatus == PairingHandler.PairState.Requested) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(vertical = 12.dp)
                                .align(Alignment.CenterHorizontally)
                        )
                    }
                }
            }

            if (pairStatus == PairingHandler.PairState.NotPaired) {
                KdeButton(
                    onClick = onRequestPairing,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    text = stringResource(id = R.string.request_pairing)
                )
            }

            if (pairStatus == PairingHandler.PairState.RequestedByPeer) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    KdeButton(
                        onClick = onAcceptPairing,
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        icon = Icons.Default.Check,
                        text = stringResource(id = R.string.pairing_accept)
                    )
                    KdeButton(
                        onClick = onRejectPairing,
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer,
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        icon = Icons.Default.Close,
                        text = stringResource(id = R.string.pairing_reject)
                    )
                }
            }
        }
    }
}

@KdeThemePreviews
@Composable
private fun DevicePairingScreenNotPairedPreview() {
    KdeTheme(context = LocalContext.current) {
        DevicePairingScreenContent(
            pairStatus = PairingHandler.PairState.NotPaired,
            verificationKey = "",
            onRequestPairing = { /* Do nothing */ },
            onAcceptPairing = { /* Do nothing */ },
            onRejectPairing = { /* Do nothing */ }
        )
    }
}

@KdeThemePreviews
@Composable
private fun DevicePairingScreenRequestedPreview() {
    KdeTheme(context = LocalContext.current) {
        DevicePairingScreenContent(
            pairStatus = PairingHandler.PairState.Requested,
            verificationKey = "123456",
            onRequestPairing = { /* Do nothing */ },
            onAcceptPairing = { /* Do nothing */ },
            onRejectPairing = { /* Do nothing */ }
        )
    }
}

@KdeThemePreviews
@Composable
private fun DevicePairingScreenRequestedByPeerPreview() {
    KdeTheme(context = LocalContext.current) {
        DevicePairingScreenContent(
            pairStatus = PairingHandler.PairState.RequestedByPeer,
            verificationKey = "123456",
            onRequestPairing = { /* Do nothing */ },
            onAcceptPairing = { /* Do nothing */ },
            onRejectPairing = { /* Do nothing */ }
        )
    }
}
