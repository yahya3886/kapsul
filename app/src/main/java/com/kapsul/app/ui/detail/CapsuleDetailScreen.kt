@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
)

package com.kapsul.app.ui.detail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapsul.app.R
import com.kapsul.app.ui.components.ConfettiBurst
import com.kapsul.app.ui.components.SealVisual
import com.kapsul.app.ui.components.capsuleContainer
import com.kapsul.app.ui.newcapsule.NewCapsuleDialog
import com.kapsul.app.util.formatDateTime
import com.kapsul.app.util.remainingText
import com.kapsul.app.viewmodel.KapsulViewModel
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

/**
 * Tek kapsülün detayı.
 *
 * Mühür kırma akışı: titreşim → onay → [SealVisual] mührü solur →
 * [KapsulViewModel.open] çağrılır → konfeti kutlaması eşliğinde içerik
 * yay ile açılır (expressive spatial spring).
 */
@Composable
fun CapsuleDetailScreen(
    vm: KapsulViewModel,
    capsuleId: String?,
    onBack: () -> Unit,
) {
    val capsules by vm.capsules.collectAsState()
    val capsule = capsules.find { it.id == capsuleId }

    // Liste silinmişse geri dön.
    LaunchedEffect(capsuleId, capsules) {
        if (capsule == null && capsules.isNotEmpty()) onBack()
    }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    var breaking by remember { mutableStateOf(false) }
    var confirmEarly by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var showEdit by remember { mutableStateOf(false) }
    var celebrate by remember { mutableStateOf(false) }

    if (capsule == null) return

    val opened = capsule.openedAt != null
    val overdue = !opened && now >= capsule.unlockAt

    // Mühürün dolma oranı: 0 (yeni mühürlendi) → 1 (vakti geldi).
    val progress = if (capsule.unlockAt > capsule.createdAt) {
        ((now - capsule.createdAt).toFloat() /
            (capsule.unlockAt - capsule.createdAt)).coerceIn(0f, 1f)
    } else {
        1f
    }

    val haptics = LocalHapticFeedback.current

    // Mühür kırma: titreşim → kısa animasyon → mührü aç → kutlama.
    LaunchedEffect(breaking) {
        if (breaking && !opened) {
            haptics.performHapticFeedback(HapticFeedbackType.LongPress)
            delay(650)
            vm.open(capsule.id)
            celebrate = true
        }
    }
    LaunchedEffect(celebrate) {
        if (celebrate) {
            delay(1700)
            celebrate = false
        }
    }

    val motion = MaterialTheme.motionScheme

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.cancel),
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showEdit = true }) {
                        Icon(
                            imageVector = Icons.Filled.Edit,
                            contentDescription = stringResource(R.string.edit_capsule),
                        )
                    }
                    IconButton(onClick = { confirmDelete = true }) {
                        Icon(
                            imageVector = Icons.Filled.Delete,
                            contentDescription = stringResource(R.string.delete),
                        )
                    }
                },
            )
        },
    ) { padding ->
        Box(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize(),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                SealVisual(
                    emoji = capsule.emoji,
                    breaking = breaking,
                    opened = opened,
                    progress = progress,
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = capsule.title,
                    style = MaterialTheme.typography.headlineMedium,
                    textAlign = TextAlign.Center,
                )

                Spacer(modifier = Modifier.height(20.dp))

                AnimatedVisibility(
                    visible = opened,
                    enter = fadeIn(tween(450)) + expandVertically(
                        animationSpec = motion.defaultSpatialSpec(),
                    ),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = MaterialTheme.shapes.large,
                            colors = CardDefaults.cardColors(
                                containerColor = capsuleContainer(capsule.colorIndex),
                            ),
                        ) {
                            Text(
                                text = capsule.message,
                                style = MaterialTheme.typography.bodyLarge,
                                modifier = Modifier.padding(20.dp),
                            )
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = stringResource(
                                R.string.opened_at,
                                formatDateTime(capsule.openedAt ?: 0L),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Text(
                            text = stringResource(
                                R.string.created_at,
                                formatDateTime(capsule.createdAt),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }

                AnimatedVisibility(
                    visible = !opened,
                    enter = fadeIn(tween(300)),
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.remaining),
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = if (overdue) {
                                stringResource(R.string.time_up)
                            } else {
                                remainingText(capsule.unlockAt, now)
                            },
                            style = MaterialTheme.typography.headlineLarge,
                            color = MaterialTheme.colorScheme.primary,
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = stringResource(
                                R.string.sealed_until,
                                formatDateTime(capsule.unlockAt),
                            ),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = TextAlign.Center,
                        )
                        Text(
                            text = stringResource(
                                R.string.seal_progress,
                                (progress * 100).roundToInt(),
                            ),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.height(20.dp))

                        if (overdue) {
                            Button(
                                onClick = { breaking = true },
                                enabled = !breaking,
                            ) {
                                if (breaking) {
                                    LoadingIndicator(modifier = Modifier.size(20.dp))
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Text(stringResource(R.string.breaking))
                                } else {
                                    Icon(
                                        imageVector = Icons.Filled.LockOpen,
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp),
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(stringResource(R.string.break_seal))
                                }
                            }
                        } else {
                            TextButton(onClick = { confirmEarly = true }) {
                                Text(stringResource(R.string.early_open))
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))
            }

            // Mühür kırılınca mührün üstünden fışkıran konfeti.
            if (celebrate) {
                ConfettiBurst(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = (-70).dp)
                        .size(340.dp),
                )
            }
        }
    }

    if (showEdit) {
        NewCapsuleDialog(
            initial = capsule,
            saving = false,
            onDismiss = { showEdit = false },
            onCreate = { updated ->
                vm.update(updated)
                showEdit = false
            },
        )
    }

    if (confirmEarly) {
        AlertDialog(
            onDismissRequest = { confirmEarly = false },
            title = { Text(stringResource(R.string.early_open)) },
            text = { Text(stringResource(R.string.early_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmEarly = false
                        breaking = true
                    },
                ) {
                    Text(stringResource(R.string.early_open))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmEarly = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text(stringResource(R.string.delete)) },
            text = { Text(stringResource(R.string.delete_warning)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        confirmDelete = false
                        vm.delete(capsule.id)
                        onBack()
                    },
                ) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { confirmDelete = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }
}
