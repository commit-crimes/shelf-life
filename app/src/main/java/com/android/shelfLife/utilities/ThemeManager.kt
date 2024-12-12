package com.android.shelfLife.ui.leaderboard

import androidx.compose.material3.ColorScheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.android.shelfLife.viewmodel.leaderboard.LeaderboardMode

object ThemeManager {
    private val RatLightColorScheme = lightColorScheme(
        primary = Color(0xFFFFEB3B), // Bright yellow
        onPrimary = Color.Black,
        primaryContainer = Color(0xFFFFF176), // Soft yellow
        onPrimaryContainer = Color.Black,
        secondary = Color(0xFFFFD54F), // Amber
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFFFE082),
        onSecondaryContainer = Color.Black,
        tertiary = Color(0xFFFFAB40), // Orange accent
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFFFFCC80),
        onTertiaryContainer = Color.Black,
        error = Color(0xFFD32F2F), // Standard error red
        onError = Color.White,
        errorContainer = Color(0xFFEF9A9A),
        onErrorContainer = Color.Black,
        background = Color(0xFFFFFDE7), // Pale yellow background
        onBackground = Color.Black,
        surface = Color(0xFFFFF8E1), // Soft cream
        onSurface = Color.Black,
        surfaceVariant = Color(0xFFFFECB3),
        onSurfaceVariant = Color.Black,
        outline = Color(0xFFFFE57F),
        outlineVariant = Color(0xFFFFF59D),
        scrim = Color.Black,
        inverseSurface = Color(0xFF212121),
        inverseOnSurface = Color.White,
        inversePrimary = Color(0xFFFFF59D),
        surfaceDim = Color(0xFFFFF8E1),
        surfaceBright = Color(0xFFFFFDE7),
        surfaceContainerLowest = Color(0xFFFFFDE7),
        surfaceContainerLow = Color(0xFFFFF8E1),
        surfaceContainer = Color(0xFFFFF4C3),
        surfaceContainerHigh = Color(0xFFFFF3B0),
        surfaceContainerHighest = Color(0xFFFFF176),
    )

    private val RatDarkColorScheme = darkColorScheme(
        primary = Color(0xFFFFD600), // Deep yellow
        onPrimary = Color.Black,
        primaryContainer = Color(0xFFFFEA00), // Bright yellow
        onPrimaryContainer = Color.Black,
        secondary = Color(0xFFFFAB40), // Deep amber
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFFFFC107),
        onSecondaryContainer = Color.Black,
        tertiary = Color(0xFFFF9100), // Orange
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFFFFA000),
        onTertiaryContainer = Color.Black,
        error = Color(0xFFF44336), // Dark red error
        onError = Color.Black,
        errorContainer = Color(0xFFD32F2F),
        onErrorContainer = Color.White,
        background = Color(0xFF212121), // Dark grey
        onBackground = Color.White,
        surface = Color(0xFF303030), // Soft dark
        onSurface = Color.White,
        surfaceVariant = Color(0xFF424242),
        onSurfaceVariant = Color.White,
        outline = Color(0xFFFFC107),
        outlineVariant = Color(0xFFFFD740),
        scrim = Color.Black,
        inverseSurface = Color(0xFFFFF9C4),
        inverseOnSurface = Color.Black,
        inversePrimary = Color(0xFFFFC107),
        surfaceDim = Color(0xFF424242),
        surfaceBright = Color(0xFF303030),
        surfaceContainerLowest = Color(0xFF212121),
        surfaceContainerLow = Color(0xFF303030),
        surfaceContainer = Color(0xFF424242),
        surfaceContainerHigh = Color(0xFF5D5D5D),
        surfaceContainerHighest = Color(0xFF757575),
    )


    private val StinkyLightColorScheme = lightColorScheme(
        primary = Color(0xFF6D9E71), // Slightly darker soft green
        onPrimary = Color.Black,
        primaryContainer = Color(0xFF97C08B), // Muted darker green
        onPrimaryContainer = Color.Black,
        secondary = Color(0xFF5EA57A), // Darker greenish teal
        onSecondary = Color.Black,
        secondaryContainer = Color(0xFF86B98E), // Slightly darker muted green
        onSecondaryContainer = Color.Black,
        tertiary = Color(0xFF4B8E61), // Slightly darker fresh green
        onTertiary = Color.Black,
        tertiaryContainer = Color(0xFF81AD7C),
        onTertiaryContainer = Color.Black,
        error = Color(0xFFBA1B1B), // Standard error red
        onError = Color.White,
        errorContainer = Color(0xFFF9DEDC),
        onErrorContainer = Color.Black,
        background = Color(0xFFE0EFE0), // Slightly darker greenish background
        onBackground = Color.Black,
        surface = Color(0xFFDCE8DC), // Darker light green
        onSurface = Color.Black,
        surfaceVariant = Color(0xFF9CB59C), // Darker soft green
        onSurfaceVariant = Color.Black,
        outline = Color(0xFF6D9E71), // Green outline
        outlineVariant = Color(0xFF5EA57A),
        scrim = Color.Black,
        inverseSurface = Color(0xFF394C39),
        inverseOnSurface = Color.White,
        inversePrimary = Color(0xFF376D3D), // Vibrant darker green
        surfaceDim = Color(0xFF97C08B),
        surfaceBright = Color(0xFFE0EFE0),
        surfaceContainerLowest = Color(0xFFE0EFE0),
        surfaceContainerLow = Color(0xFFDCE8DC),
        surfaceContainer = Color(0xFF97C08B),
        surfaceContainerHigh = Color(0xFF6D9E71),
        surfaceContainerHighest = Color(0xFF5EA57A),
    )


    private val StinkyDarkColorScheme = darkColorScheme(
        primary = Color(0xFF5EA57A), // Darker greenish teal
        onPrimary = Color.White,
        primaryContainer = Color(0xFF2D5736), // Deeper green
        onPrimaryContainer = Color.White,
        secondary = Color(0xFF3E7E5A), // Strong dark green
        onSecondary = Color.White,
        secondaryContainer = Color(0xFF204A2F), // Very deep green
        onSecondaryContainer = Color.White,
        tertiary = Color(0xFF326647), // Rich dark green
        onTertiary = Color.White,
        tertiaryContainer = Color(0xFF28513B),
        onTertiaryContainer = Color.White,
        error = Color(0xFFF2B8B5),
        onError = Color.Black,
        errorContainer = Color(0xFF8C1D18),
        onErrorContainer = Color.White,
        background = Color(0xFF1A3421), // Darker greenish background
        onBackground = Color.White,
        surface = Color(0xFF27412C), // Deeper green
        onSurface = Color.White,
        surfaceVariant = Color(0xFF44614C), // Rich mossy green
        onSurfaceVariant = Color.White,
        outline = Color(0xFF5EA57A),
        outlineVariant = Color(0xFF3E7E5A),
        scrim = Color.Black,
        inverseSurface = Color(0xFFECF3EC),
        inverseOnSurface = Color.Black,
        inversePrimary = Color(0xFF3E7E5A),
        surfaceDim = Color(0xFF1A3421),
        surfaceBright = Color(0xFF27412C),
        surfaceContainerLowest = Color(0xFF1A3421),
        surfaceContainerLow = Color(0xFF27412C),
        surfaceContainer = Color(0xFF44614C),
        surfaceContainerHigh = Color(0xFF326647),
        surfaceContainerHighest = Color(0xFF5EA57A),
    )





    // When no special mode is active, this is null
    var currentColorScheme: MutableState<ColorScheme?> = mutableStateOf(null)

    fun updateScheme(mode: LeaderboardMode, isDark: Boolean) {
        currentColorScheme.value = when (mode) {
            LeaderboardMode.RAT -> if (isDark) RatDarkColorScheme else RatLightColorScheme
            LeaderboardMode.STINKY -> if (isDark) StinkyDarkColorScheme else StinkyLightColorScheme
        }
    }

    fun resetMode() {
        // Revert to null means use the app's original color scheme
        currentColorScheme.value = null
    }
}
