package com.gridraw.app.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val GridRawColorScheme = darkColorScheme(
    primary            = AccentBlue,
    onPrimary          = TextMain,
    primaryContainer   = AccentBlueDim,
    onPrimaryContainer = AccentBlue,

    secondary          = AccentPurple,
    onSecondary        = TextMain,

    tertiary           = AccentCyan,
    onTertiary         = BgRoot,

    background         = BgRoot,
    onBackground       = TextMain,

    surface            = BgPanel,
    onSurface          = TextMain,
    surfaceVariant     = SurfaceVariant,
    onSurfaceVariant   = OnSurfaceVariant,

    outline            = BorderLight,
    outlineVariant     = BorderHover,

    error              = Danger,
    onError            = TextMain,
)

@Composable
fun GridRawTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = GridRawColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = BgRoot.toArgb()
            window.navigationBarColor = BgRoot.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = false
                isAppearanceLightNavigationBars = false
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography  = GridRawTypography,
        content     = content
    )
}
