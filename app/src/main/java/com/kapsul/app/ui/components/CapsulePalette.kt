package com.kapsul.app.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

/** Kapsül rengi seçimi (0..5): Expressive konteyner paleti. */
@Composable
fun capsuleContainer(index: Int): Color = when (index.mod(6)) {
    0 -> MaterialTheme.colorScheme.primaryContainer
    1 -> MaterialTheme.colorScheme.secondaryContainer
    2 -> MaterialTheme.colorScheme.tertiaryContainer
    3 -> MaterialTheme.colorScheme.errorContainer
    4 -> MaterialTheme.colorScheme.surfaceContainerHigh
    else -> MaterialTheme.colorScheme.surfaceContainerHighest
}

/** Kart üzerindeki sembol rozetinin arka planı (balmumu hissi). */
val stampBackground: Color = Color.White.copy(alpha = 0.82f)
