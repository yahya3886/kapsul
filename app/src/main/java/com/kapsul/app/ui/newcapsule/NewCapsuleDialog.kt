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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonGroup
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kapsul.app.R
import com.kapsul.app.data.Capsule
import com.kapsul.app.ui.components.capsuleContainer
import com.kapsul.app.util.formatDateTime

private val EMOJIS = listOf(
    "💌", "🎁", "⏳", "🌱", "📝", "🎉",
    "🗝️", "📸", "🎯", "🌙", "☀️", "🍀",
)

private enum class PresetKind { DURATION, CUSTOM, DATE }

private data class Preset(val label: String, val kind: PresetKind, val duration: Long = 0L)

private const val HOUR = 60 * 60 * 1000L
private const val DAY = 24 * HOUR

private const val UNIT_MINUTE = 0
private const val UNIT_HOUR = 1
private const val UNIT_DAY = 2

private val PRESET_DURATIONS = longArrayOf(HOUR, DAY, 7 * DAY, 30 * DAY)

/** Düzenlemede hazır süreye eşleşen preset index'i; eşleşmezse Özel (4). */
private fun initialPresetIndex(initial: Capsule?): Int {
    if (initial == null || initial.openedAt != null) return 1
    val atCreation = initial.unlockAt - initial.createdAt
    PRESET_DURATIONS.forEachIndexed { index, duration ->
        if (kotlin.math.abs(atCreation - duration) < 60_000) return index
    }
    return 4
}

/** Kalan süreye göre en uygun birim (dakika/saat/gün). */
private fun initialUnitIndex(initial: Capsule?): Int {
    if (initial == null) return UNIT_HOUR
    val remaining = initial.unlockAt - System.currentTimeMillis()
    return when {
        remaining < HOUR -> UNIT_MINUTE
        remaining < DAY -> UNIT_HOUR
        else -> UNIT_DAY
    }
}

/** Kalan süreyi yukarı yuvarlayarak miktar metni — hiçbir zaman kısaltmaz. */
private fun initialAmountText(initial: Capsule?): String {
    if (initial == null) return ""
    val remaining = (initial.unlockAt - System.currentTimeMillis()).coerceAtLeast(60_000L)
    val unit = when {
        remaining < HOUR -> 60_000L
        remaining < DAY -> HOUR
        else -> DAY
    }
    return ((remaining + unit - 1) / unit).toString()
}

/**
 * Yeni kapsül yazma (veya mevcut bir kapsülü düzenleme) diyalogu:
 * başlık, not, sembol, renk ve açılma vakti.
 *
 * Açılma vakti üç yoldan seçilebilir: hazır süre (1 saat…1 ay), özel süre
 * (miktar + dakika/saat/gün birimi) ya da takvimden tarih.
 * [initial] verilirse düzenleme moduna geçer; dokunulmayan süre korunur.
 */
@Composable
fun NewCapsuleDialog(
    saving: Boolean,
    onDismiss: () -> Unit,
    onCreate: (Capsule) -> Unit,
    initial: Capsule? = null,
) {
    val editing = initial != null
    val editOpened = initial != null && initial.openedAt != null
    // Mühürlü kapsülün notu düzenleme diyalogunda da gizli kalır:
    // alan yerine kilitli bilgi satırı gösterilir.
    val hideMessage = editing && !editOpened

    var title by remember { mutableStateOf(initial?.title ?: "") }
    var message by remember { mutableStateOf(initial?.message ?: "") }
    var emoji by remember { mutableStateOf(initial?.emoji ?: EMOJIS.first()) }
    var colorIndex by remember { mutableIntStateOf(initial?.colorIndex ?: 0) }
    var presetIndex by remember { mutableIntStateOf(initialPresetIndex(initial)) }
    var customDate by remember { mutableStateOf<Long?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

    // Özel süre: miktar (yalnızca rakam) + birim (dakika/saat/gün).
    var customAmount by remember { mutableStateOf(initialAmountText(initial)) }
    var unitIndex by remember { mutableIntStateOf(initialUnitIndex(initial)) }

    // Süre alanına dokunulduysa yeni değer hesaplanır; dokunulmadıysa
    // mevcut açılma vakti aynen korunur (düzenlemede sürpriz olmaz).
    var durationTouched by remember { mutableStateOf(false) }

    val label1h = stringResource(R.string.unlock_1h)
    val label1d = stringResource(R.string.unlock_1d)
    val label1w = stringResource(R.string.unlock_1w)
    val label1m = stringResource(R.string.unlock_1m)
    val labelCustom = stringResource(R.string.custom_duration)
    val labelPick = stringResource(R.string.unlock_pick)
    val presets = listOf(
        Preset(label1h, PresetKind.DURATION, HOUR),
        Preset(label1d, PresetKind.DURATION, DAY),
        Preset(label1w, PresetKind.DURATION, 7 * DAY),
        Preset(label1m, PresetKind.DURATION, 30 * DAY),
        Preset(labelCustom, PresetKind.CUSTOM),
        Preset(labelPick, PresetKind.DATE),
    )
    val selected = presets[presetIndex]

    // Birim etiketleri ButtonGroup içerik lambda'sı composable olmadığı için dışta çözülür.
    val unitMinutes = stringResource(R.string.unit_minutes)
    val unitHours = stringResource(R.string.unit_hours)
    val unitDays = stringResource(R.string.unit_days)

    val amount = customAmount.toIntOrNull() ?: 0
    val customValid = amount in 1..999_999
    val unitMillis = when (unitIndex) {
        UNIT_MINUTE -> 60_000L
        UNIT_HOUR -> HOUR
        else -> DAY
    }

    val now = System.currentTimeMillis()
    val useDate = customDate != null
    val unlockAt: Long? = when {
        // Açılmış kapsülün vakti değişmez.
        editOpened -> initial?.unlockAt
        // Süreye dokunulmadıysa mevcut vakt korunur.
        editing && !durationTouched -> initial?.unlockAt
        useDate -> customDate
        selected.kind == PresetKind.DURATION -> now + selected.duration
        selected.kind == PresetKind.CUSTOM ->
            if (customValid) now + amount * unitMillis else null
        else -> null
    }

    val canCreate =
        title.isNotBlank() && message.isNotBlank() && unlockAt != null && !saving

    AlertDialog(
        onDismissRequest = { if (!saving) onDismiss() },
        title = {
            Text(
                stringResource(if (editing) R.string.edit_capsule else R.string.new_capsule),
            )
        },
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
                if (hideMessage) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(MaterialTheme.shapes.medium)
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Lock,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(R.string.message_locked),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                } else {
                    OutlinedTextField(
                        value = message,
                        onValueChange = { message = it },
                        label = { Text(stringResource(R.string.message_hint)) },
                        minLines = 3,
                        maxLines = 6,
                        modifier = Modifier.fillMaxWidth(),
                    )
                }

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

                // Açılmış kapsülün süresi değiştirilemez — bölüm gizlenir.
                if (!editOpened) {
                    Text(
                        text = stringResource(R.string.unlock_label),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    ButtonGroup(
                        overflowIndicator = { state ->
                            IconButton(onClick = { state.show() }) {
                                Icon(
                                    imageVector = Icons.Filled.MoreVert,
                                    contentDescription = stringResource(R.string.custom_duration),
                                )
                            }
                        },
                    ) {
                        presets.forEachIndexed { index, preset ->
                            toggleableItem(
                                checked = if (preset.kind == PresetKind.DATE) {
                                    customDate != null
                                } else {
                                    presetIndex == index && customDate == null
                                },
                                label = preset.label,
                                onCheckedChange = {
                                    when (preset.kind) {
                                        PresetKind.DATE -> {
                                            if (customDate == null) showDatePicker = true
                                        }
                                        else -> {
                                            presetIndex = index
                                            customDate = null
                                            durationTouched = true
                                        }
                                    }
                                },
                            )
                        }
                    }

                    // Özel süre: miktar + birim seçimi.
                    if (selected.kind == PresetKind.CUSTOM && !useDate) {
                        OutlinedTextField(
                            value = customAmount,
                            onValueChange = { input ->
                                customAmount = input.filter(Char::isDigit).take(6)
                                durationTouched = true
                            },
                            label = { Text(stringResource(R.string.amount_hint)) },
                            singleLine = true,
                            isError = customAmount.isNotEmpty() && !customValid,
                            supportingText = {
                                if (customAmount.isNotEmpty() && !customValid) {
                                    Text(stringResource(R.string.invalid_amount))
                                }
                            },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            modifier = Modifier.fillMaxWidth(),
                        )
                        ButtonGroup(overflowIndicator = {}) {
                            listOf(unitMinutes, unitHours, unitDays).forEachIndexed { index, label ->
                                toggleableItem(
                                    checked = unitIndex == index,
                                    label = label,
                                    onCheckedChange = {
                                        unitIndex = index
                                        durationTouched = true
                                    },
                                )
                            }
                        }
                    }

                    // Seçilen vaktin önizlemesi (özel süre veya tarih).
                    if (unlockAt != null && (useDate || selected.kind == PresetKind.CUSTOM)) {
                        Text(
                            text = stringResource(R.string.unlock_preview, formatDateTime(unlockAt)),
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.primary,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                enabled = canCreate,
                onClick = {
                    val target = unlockAt ?: return@TextButton
                    // Düzenlemede kimlik ve mühür zamanları korunur.
                    val base = initial ?: Capsule()
                    onCreate(
                        base.copy(
                            title = title.trim(),
                            message = message.trim(),
                            emoji = emoji,
                            colorIndex = colorIndex,
                            unlockAt = target,
                        ),
                    )
                },
            ) {
                if (saving) {
                    LoadingIndicator(modifier = Modifier.size(20.dp))
                } else {
                    Text(stringResource(if (editing) R.string.save else R.string.confirm))
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
            initialSelectedDateMillis = customDate
                ?: initial?.unlockAt
                ?: (System.currentTimeMillis() + DAY),
        )
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        customDate = state.selectedDateMillis?.plus(12 * HOUR)
                        durationTouched = true
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
