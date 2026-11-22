package com.jackby03.sparky.presentation.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.jackby03.sparky.domain.model.settings.ThemeMode

private val DarkColorScheme = darkColorScheme(
    primary          = NeonGreen,
    onPrimary        = OnPrimary,
    secondary        = NeonCyan,
    onSecondary      = OnPrimary,
    tertiary         = NeonOrange,
    background       = BackgroundDark,
    onBackground     = OnBackground,
    surface          = SurfaceDark,
    onSurface        = OnSurface,
    surfaceVariant   = SurfaceVariantDark,
    error            = NeonRed,
    onError          = OnError
)

private val LightColorScheme = lightColorScheme(
    primary          = PrimaryLight,
    onPrimary        = OnPrimary,
    secondary        = SecondaryLight,
    background       = BackgroundLight,
    surface          = SurfaceLight,
    error            = NeonRed
)

private val HighContrastColorScheme = darkColorScheme(
    primary          = HCPrimary,
    onPrimary        = HCOnPrimary,
    secondary        = HCSecondary,
    onSecondary      = HCOnPrimary,
    background       = HCBackground,
    onBackground     = HCOnBackground,
    surface          = HCSurface,
    onSurface        = HCOnSurface,
    surfaceVariant   = HCSurfaceVariant,
    error            = HCError,
    onError          = HCOnPrimary
)

private val HighContrastCanvasColors = SparkCanvasColors(
    wire         = HCWire,
    wireActive   = HCWireActive,
    component    = HCComponent,
    grid         = HCGrid,
    voltageNode  = HCPrimary,
    ground       = HCOnSurface,
    terminal     = HCPrimary,
    simLabel     = HCSecondary
)

data class SparkCanvasColors(
    val wire: androidx.compose.ui.graphics.Color,
    val wireActive: androidx.compose.ui.graphics.Color,
    val component: androidx.compose.ui.graphics.Color,
    val grid: androidx.compose.ui.graphics.Color,
    val voltageNode: androidx.compose.ui.graphics.Color,
    val ground: androidx.compose.ui.graphics.Color,
    val terminal: androidx.compose.ui.graphics.Color,
    val simLabel: androidx.compose.ui.graphics.Color
)

val LocalCanvasColors = staticCompositionLocalOf {
    SparkCanvasColors(
        wire         = WireColor,
        wireActive   = WireActiveColor,
        component    = ComponentColor,
        grid         = GridLineDark,
        voltageNode  = VoltageNodeColor,
        ground       = GroundColor,
        terminal     = NeonGreen,
        simLabel     = NeonOrange
    )
}

@Composable
fun SparkTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    themeMode: ThemeMode = ThemeMode.SYSTEM,
    content: @Composable () -> Unit
) {
    val isHighContrast = themeMode == ThemeMode.HIGH_CONTRAST
    val colorScheme = when {
        isHighContrast -> HighContrastColorScheme
        darkTheme      -> DarkColorScheme
        else           -> LightColorScheme
    }
    val canvasColors = if (isHighContrast) HighContrastCanvasColors else LocalCanvasColors.current

    CompositionLocalProvider(LocalCanvasColors provides canvasColors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography  = SparkTypography,
            shapes      = SparkShapes,
            content     = content
        )
    }
}

val canvasColors: SparkCanvasColors
    @Composable get() = LocalCanvasColors.current
