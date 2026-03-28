package com.havos.lubricerp.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.ui.unit.dp

private val LightScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = BrandOnPrimary,
    primaryContainer = BrandPrimaryContainer,
    onPrimaryContainer = BrandOnPrimaryContainer,
    secondary = BrandSecondary,
    onSecondary = BrandOnSecondary,
    secondaryContainer = BrandSecondaryContainer,
    onSecondaryContainer = BrandOnSecondaryContainer,
    surface = SurfaceSoft,
    surfaceContainer = SurfaceContainer,
    onSurface = SurfaceOn,
    error = ErrorTone
)

private val DarkScheme = darkColorScheme(
    primary = BrandPrimaryContainer,
    onPrimary = BrandOnPrimaryContainer,
    primaryContainer = BrandPrimary,
    onPrimaryContainer = BrandOnPrimary,
    secondary = BrandSecondaryContainer,
    onSecondary = BrandOnSecondaryContainer,
    secondaryContainer = BrandSecondary,
    onSecondaryContainer = BrandOnSecondary,
    surface = SurfaceOn,
    onSurface = SurfaceSoft,
    error = ErrorTone
)

private val GoalShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
)

@Composable
fun GoalErpTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkScheme else LightScheme,
        typography = Typography,
        shapes = GoalShapes,
        content = content
    )
}
