package dev.thestbar.tunify.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import dev.thestbar.tunify.data.preferences.ThemePreference

private val DarkColors = darkColorScheme(
    primary                = DarkPrimary,
    onPrimary              = DarkOnPrimary,
    primaryContainer       = DarkPrimaryContainer,
    onPrimaryContainer     = DarkOnPrimaryContainer,
    secondary              = DarkSecondary,
    onSecondary            = DarkOnSecondary,
    secondaryContainer     = DarkSecondaryContainer,
    onSecondaryContainer   = DarkOnSecondaryContainer,
    tertiary               = DarkTertiary,
    onTertiary             = DarkOnTertiary,
    tertiaryContainer      = DarkTertiaryContainer,
    onTertiaryContainer    = DarkOnTertiaryContainer,
    background             = DarkBackground,
    onBackground           = DarkOnBackground,
    surface                = DarkSurface,
    onSurface              = DarkOnSurface,
    onSurfaceVariant       = DarkOnSurfaceVariant,
    outline                = DarkOutline,
    outlineVariant         = DarkOutlineVariant,
    surfaceContainer       = DarkSurfaceContainer,
    surfaceContainerHigh   = DarkSurfaceContainerHigh,
    surfaceContainerHighest= DarkSurfaceContainerHighest,
    surfaceContainerLow    = DarkSurfaceContainerLow,
    surfaceContainerLowest = DarkSurfaceContainerLowest,
    error                  = DarkError,
    onError                = DarkOnError,
    errorContainer         = DarkErrorContainer,
    onErrorContainer       = DarkOnErrorContainer,
)

private val LightColors = lightColorScheme(
    primary                = LightPrimary,
    onPrimary              = LightOnPrimary,
    primaryContainer       = LightPrimaryContainer,
    onPrimaryContainer     = LightOnPrimaryContainer,
    secondary              = LightSecondary,
    onSecondary            = LightOnSecondary,
    secondaryContainer     = LightSecondaryContainer,
    onSecondaryContainer   = LightOnSecondaryContainer,
    tertiary               = LightTertiary,
    onTertiary             = LightOnTertiary,
    tertiaryContainer      = LightTertiaryContainer,
    onTertiaryContainer    = LightOnTertiaryContainer,
    background             = LightBackground,
    onBackground           = LightOnBackground,
    surface                = LightSurface,
    onSurface              = LightOnSurface,
    onSurfaceVariant       = LightOnSurfaceVariant,
    outline                = LightOutline,
    outlineVariant         = LightOutlineVariant,
    surfaceContainer       = LightSurfaceContainer,
    surfaceContainerHigh   = LightSurfaceContainerHigh,
    surfaceContainerHighest= LightSurfaceContainerHighest,
    surfaceContainerLow    = LightSurfaceContainerLow,
    surfaceContainerLowest = LightSurfaceContainerLowest,
    error                  = LightError,
    onError                = LightOnError,
    errorContainer         = LightErrorContainer,
    onErrorContainer       = LightOnErrorContainer,
)

@Composable
fun TunifyTheme(
    themePreference: ThemePreference = ThemePreference.SYSTEM,
    content: @Composable () -> Unit
) {
    val darkTheme = when (themePreference) {
        ThemePreference.DARK   -> true
        ThemePreference.LIGHT  -> false
        ThemePreference.SYSTEM -> isSystemInDarkTheme()
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        content = content
    )
}
