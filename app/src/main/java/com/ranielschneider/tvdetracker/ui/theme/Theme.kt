package com.ranielschneider.tvdetracker.ui.theme

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
    primary = VerdeTrackingDark,
    background = FundoAppDark,
    surface = SuperficieAppDark,
    onBackground = TextoPrimarioDark,
    onSurface = TextoPrimarioDark,
    error = VermelhoStop
)

private val LightColorScheme = lightColorScheme(
    primary = AzulPrimario,
    background = FundoApp,
    surface = SuperficieApp,
    onBackground = TextoPrimario,
    onSurface = TextoPrimario,
    error = VermelhoStop
)

@Composable
fun TvdeTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Desligado por defeito: queremos sempre as cores da marca, não as do wallpaper do telemóvel
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