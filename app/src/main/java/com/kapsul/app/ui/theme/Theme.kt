package com.kapsul.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialExpressiveTheme
import androidx.compose.material3.MotionScheme
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

/** Expressive köşe yarıçapları: büyük, yuvarlak, zarif. */
private val KapsulShapes = Shapes(
    extraSmall = RoundedCornerShape(10.dp),
    small = RoundedCornerShape(16.dp),
    medium = RoundedCornerShape(24.dp),
    large = RoundedCornerShape(32.dp),
    extraLarge = RoundedCornerShape(40.dp),
)

/**
 * Kapsül teması — Material 3 Expressive.
 *
 * [MotionScheme.expressive()] tüm bileşenlere canlı yay/spring animasyonları
 * kazandırır; mühür kırma ve liste hareketleri bu şemayı kullanır.
 */
@Composable
fun KapsulTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    MaterialExpressiveTheme(
        colorScheme = if (darkTheme) KapsulDarkColors else KapsulLightColors,
        motionScheme = MotionScheme.expressive(),
        typography = KapsulTypography,
        shapes = KapsulShapes,
        content = content,
    )
}
