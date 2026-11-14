@file:OptIn(
    androidx.compose.material3.ExperimentalMaterial3Api::class,
    androidx.compose.material3.ExperimentalMaterial3ExpressiveApi::class,
    androidx.compose.foundation.layout.ExperimentalLayoutApi::class,
)

package com.kapsul.app.ui.newcapsule

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.LoadingIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapsul.app.R
import com.kapsul.app.data.Capsule
import com.kapsul.app.ui.components.capsuleContainer

private val EMOJIS = listOf(
    "💌", "🎁", "⏳", "🌱", "📝", "🎉",
    "🗝️", "📸", "🎯", "🌙", "☀️", "🍀",
)

private data class Preset(val label: String, val duration: Long?)

private const val HOUR = 60 * 60 * 1000L
private const val DAY = 24 * HOUR

/**
 * Yeni kapsül yazma diyalogu: başlık, not, sembol, renk ve açılma vakti.
 * "Mühütle" onaylandığında [Capsule] üretilir.
 */
@Composable
fun NewCapsuleDialog(
    saving: Boolean,
    onDismiss: () -> Unit,
    onCreate: (Capsule) -> Unit,
) {
    var title by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var emoji by remember { mutableStateOf(EMOJIS.first()) }
    var colorIndex by remember { mutableIntStateOf(0) }
    var presetIndex by remember { mutableIntStateOf(1) }
    var customDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    val presets = listOf(
        Preset(stringResource(R.string.unlock_1h), HOUR),
        Preset(stringResource(R.string.unlock_1d), DAY),
        Preset(stringResource(R.string.unlock_1w), 7 * DAY),
        Preset(stringResource(R.string.unlock_1m), 30 * DAY),
        Preset(stringResource(R.string.unlock_pick), null),
    )

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = { Text(stringResource(R.string.new_capsule)) },
        text = {
            Column(
                modifier = Modifier.verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text(stringResource(R.string.title_hint)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = message,
                    onValueChange = { message = it },
                    label = { Text(stringResource(R.string.message_hint)) },
                    minLines = 3,
                    maxLines = 6,
                    modifier = Modifier.fillMaxWidth(),
                )

                Text(
                    text = stringResource(R.string.emoji_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    EMOJIS.forEach { item ->
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(
                                    if (item == emoji) {
                                        MaterialTheme.colorScheme.primaryContainer
                                    } else {
                                        MaterialTheme.colorScheme.surfaceContainerHigh
                                    },
                                )
                                .border(
                                    width = if (item == emoji) 2.dp else 0.dp,
                                    color = MaterialTheme.colorScheme.primary,
                                    shape = CircleShape,
                                )
                                .clickable { emoji = item },
                            contentAlignment = Alignment.Center,
                        ) {
                            Text(text = item, fontSize = 20.sp)
                        }
                    }
                }

                Text(
                    text = stringResource(R.string.color_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    repeat(6) { i ->
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(capsuleContainer(i))
                                .border(
                                    width = if (i == colorIndex) 3.dp else 1.dp,
                                    color = if (i == colorIndex) {
                                        MaterialTheme.colorScheme.onSurface
                                    } else {
                                        MaterialTheme.colorScheme.outlineVariant
                                    },
                                    shape = CircleShape,
                                )
                                .clickable { colorIndex = i },
                        )
                    }
                }

                Text(
                    text = stringResource(R.string.unlock_label),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                ButtonGroup(overflowIndicator = {}) {
                    presets.forEachIndexed { index, preset ->
                        toggleableItem(
                            checked = if (preset.duration == null) {
                                customDate != null
                            } else {
                                customDate == null && presetIndex == index
                            },
                            label = preset.label,
                            onCheckedChange = {
                                presetIndex = index
                                customDate = null
                                if (preset.duration == null) showDatePicker = true
                            },
                        )
                    }
                }
                if (customDate != null) {
                    Text(
                        text = com.kapsul.app.util.formatDateTime(customDate!!),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = title.isNotBlank() && message.isNotBlank() && !saving,
                onClick = {
                    val unlockAt = customDate
                        ?: (System.currentTimeMillis() + (presets[presetIndex].duration ?: DAY))
                    onCreate(
                        Capsule(
                            title = title.trim(),
                            message = message.trim(),
                            emoji = emoji,
                            colorIndex = colorIndex,
                            unlockAt = unlockAt,
                        ),
                    )
                },
            ) {
                if (saving) {
                    LoadingIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(R.string.confirm))
                }
            }
        },
        dismissButton = {
            TextButton(onClick = { if (!saving) onDismiss() }) {
                Text(stringResource(R.string.cancel))
            }
        },
    )

    if (showDatePicker) {
        val state = rememberDatePickerState(
            initialSelectedDateMillis = customDate ?: (System.currentTimeMillis() + DAY),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        customDate = state.selectedDateMillis?.plus(12 * HOUR)
                        showDatePicker = false
                    },
                ) {
                    Text(stringResource(R.string.ok))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        ) {
            DatePicker(state = state)
        }
    }
}
