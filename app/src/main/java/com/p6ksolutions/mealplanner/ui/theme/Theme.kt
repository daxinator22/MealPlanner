package com.p6ksolutions.mealplanner.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = TomatoDark,
    onPrimary = OnTomatoDark,
    primaryContainer = TomatoDarkContainer,
    onPrimaryContainer = OnTomatoDarkContainer,
    secondary = SageDark,
    onSecondary = OnSageDark,
    secondaryContainer = SageDarkContainer,
    onSecondaryContainer = OnSageDarkContainer,
    tertiary = SquashDark,
    onTertiary = OnSquashDark,
    tertiaryContainer = SquashDarkContainer,
    onTertiaryContainer = OnSquashDarkContainer,
    background = Espresso,
    onBackground = TextLight,
    surface = SurfaceDarkWarm,
    onSurface = TextLight,
    surfaceVariant = SurfaceDarkVariant,
    onSurfaceVariant = TextLightMuted,
    outline = WarmOutline,
    outlineVariant = SurfaceDarkVariant
)

private val LightColorScheme = lightColorScheme(
    primary = Tomato,
    onPrimary = OnTomato,
    primaryContainer = TomatoContainer,
    onPrimaryContainer = OnTomatoContainer,
    secondary = Sage,
    onSecondary = OnSage,
    secondaryContainer = SageContainer,
    onSecondaryContainer = OnSageContainer,
    tertiary = Squash,
    onTertiary = OnSquash,
    tertiaryContainer = SquashContainer,
    onTertiaryContainer = OnSquashContainer,
    background = Cream,
    onBackground = TextDark,
    surface = SurfaceWarm,
    onSurface = TextDark,
    surfaceVariant = SurfaceWarmVariant,
    onSurfaceVariant = TextMuted,
    outline = WarmOutline,
    outlineVariant = WarmOutlineVariant
)

@Composable
fun MealPlannerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
