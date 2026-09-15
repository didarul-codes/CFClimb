package com.codeforcesvisualizer.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

val Violet = Color(0xFF7C5CFF)
val Green = Color(0xFF36D399)
val Amber = Color(0xFFFBBF24)
val Red = Color(0xFFF87171)
val Blue = Color(0xFF60A5FA)
val Rose = Color(0xFFF472B6)

val DarkBg = Color(0xFF0B0D10)
val DarkSurface = Color(0xFF14171D)
val DarkSurface2 = Color(0xFF1C2029)
val DarkBorder = Color(0xFF262B36)
val DarkFg = Color(0xFFE8EAEF)
val DarkDim = Color(0xFF7D8597)

val LightBg = Color(0xFFFAFAF7)
val LightSurface = Color(0xFFFFFFFF)
val LightSurface2 = Color(0xFFF2F0EA)
val LightBorder = Color(0xFFE5E3DD)
val LightFg = Color(0xFF1A1D24)
val LightDim = Color(0xFF7D8597)

val LightViolet = Color(0xFF5B3DF0)
val LightGreen = Color(0xFF15A36A)
val LightAmber = Color(0xFFB88404)
val LightRed = Color(0xFFDC2626)
val LightBlue = Color(0xFF2563EB)
val LightRose = Color(0xFFDB2777)

@Immutable
data class CFExtendedColors(
    val bg: Color,
    val fg: Color,
    val dim: Color,
    val surface: Color,
    val surface2: Color,
    val border: Color,
    val violet: Color,
    val green: Color,
    val amber: Color,
    val red: Color,
    val blue: Color,
    val rose: Color,
)

val DarkExtendedColors = CFExtendedColors(
    bg = DarkBg,
    fg = DarkFg,
    dim = DarkDim,
    surface = DarkSurface,
    surface2 = DarkSurface2,
    border = DarkBorder,
    violet = Violet,
    green = Green,
    amber = Amber,
    red = Red,
    blue = Blue,
    rose = Rose,
)

val LightExtendedColors = CFExtendedColors(
    bg = LightBg,
    fg = LightFg,
    dim = LightDim,
    surface = LightSurface,
    surface2 = LightSurface2,
    border = LightBorder,
    violet = LightViolet,
    green = LightGreen,
    amber = LightAmber,
    red = LightRed,
    blue = LightBlue,
    rose = LightRose,
)

val LocalCFColors = staticCompositionLocalOf { DarkExtendedColors }

internal val CFDarkColorScheme = darkColorScheme(
    primary = Violet,
    onPrimary = DarkFg,
    secondary = Green,
    onSecondary = DarkBg,
    background = DarkBg,
    onBackground = DarkFg,
    surface = DarkSurface,
    onSurface = DarkFg,
    surfaceVariant = DarkSurface2,
    onSurfaceVariant = DarkDim,
    error = Red,
    onError = DarkBg,
    outline = DarkBorder,
    outlineVariant = DarkBorder,
)

internal val CFLightColorScheme = lightColorScheme(
    primary = LightViolet,
    onPrimary = LightBg,
    secondary = LightGreen,
    onSecondary = LightBg,
    background = LightBg,
    onBackground = LightFg,
    surface = LightSurface,
    onSurface = LightFg,
    surfaceVariant = LightSurface2,
    onSurfaceVariant = LightDim,
    error = LightRed,
    onError = LightBg,
    outline = LightBorder,
    outlineVariant = LightBorder,
)

val RankTiers = listOf(
    RankTier(0, 1199, "newbie", Color(0xFF6B7280)),
    RankTier(1200, 1399, "pupil", Color(0xFF36D399)),
    RankTier(1400, 1599, "specialist", Color(0xFF22D3EE)),
    RankTier(1600, 1899, "expert", Color(0xFF60A5FA)),
    RankTier(1900, 2099, "candidate master", Color(0xFFA78BFA)),
    RankTier(2100, 2299, "master", Color(0xFFFBBF24)),
    RankTier(2300, 2399, "international master", Color(0xFFF59E0B)),
    RankTier(2400, 2599, "grandmaster", Color(0xFFF87171)),
    RankTier(2600, 2999, "international grandmaster", Color(0xFFEF4444)),
    RankTier(3000, 9999, "legendary grandmaster", Color(0xFFDC2626)),
)

data class RankTier(val min: Int, val max: Int, val name: String, val color: Color)

fun rankTierFor(rating: Int): RankTier {
    return RankTiers.find { rating in it.min..it.max } ?: RankTiers[0]
}

fun rankColorFor(rating: Int): Color = rankTierFor(rating).color

val VerdictColors = mapOf(
    "AC" to Color(0xFF36D399),
    "OK" to Color(0xFF36D399),
    "WA" to Color(0xFFF87171),
    "WRONG_ANSWER" to Color(0xFFF87171),
    "TLE" to Color(0xFFFBBF24),
    "TIME_LIMIT_EXCEEDED" to Color(0xFFFBBF24),
    "RE" to Color(0xFFF472B6),
    "RUNTIME_ERROR" to Color(0xFFF472B6),
    "MLE" to Color(0xFFA78BFA),
    "MEMORY_LIMIT_EXCEEDED" to Color(0xFFA78BFA),
    "CE" to Color(0xFF60A5FA),
    "COMPILATION_ERROR" to Color(0xFF60A5FA),
)
