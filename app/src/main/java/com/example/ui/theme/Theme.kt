package com.example.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private val TasbihColorScheme = darkColorScheme(
    primary = GoldMain,
    onPrimary = OliveBg,
    primaryContainer = OliveLift,
    onPrimaryContainer = GoldGlow,
    secondary = EmeraldAccent,
    onSecondary = OliveBg,
    secondaryContainer = EmeraldDeepBg,
    onSecondaryContainer = EmeraldAccent,
    tertiary = GoldGlow,
    onTertiary = OliveBg,
    background = OliveBg,
    onBackground = CreamText,
    surface = OliveCard,
    onSurface = CreamText,
    surfaceVariant = OliveLift,
    onSurfaceVariant = MutedGold,
    outline = GoldMain.copy(alpha = 0.30f),
    outlineVariant = GoldMain.copy(alpha = 0.18f),
    error = ErrorCrimson,
    onError = OliveBg,
    errorContainer = ErrorContainerDark,
    onErrorContainer = ErrorCrimson
)

private val TasbihShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(18.dp),
    large = RoundedCornerShape(24.dp),
    extraLarge = RoundedCornerShape(32.dp)
)

@Composable
fun TasbihTheme(
    content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
        MaterialTheme(
            colorScheme = TasbihColorScheme,
            typography = TasbihTypography,
            shapes = TasbihShapes,
            content = content
        )
    }
}
