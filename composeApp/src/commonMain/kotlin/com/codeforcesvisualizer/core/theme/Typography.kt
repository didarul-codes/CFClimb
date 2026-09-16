package com.codeforcesvisualizer.core.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

val MonoFont = FontFamily.Monospace

/**
 * The app's whole type scale. Screens pick a style from here and pass the colour separately,
 * so nobody writes a raw font size again.
 */
object CFText {
    /** One big number per screen: the rating on the climb card, the countdown. */
    val display = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.W700,
        fontSize = 26.sp,
        letterSpacing = (-0.5).sp,
    )

    /** Screen titles and hero headlines. */
    val heading = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.W700,
        fontSize = 20.sp,
        letterSpacing = (-0.3).sp,
    )

    /** Card titles, contest names, list row titles. */
    val title = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.W700,
        fontSize = 16.sp,
    )

    /** Secondary titles and stat values. */
    val subtitle = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.W600,
        fontSize = 13.sp,
    )

    /** Running text. */
    val body = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
    )

    /** Buttons, tabs and other controls. */
    val label = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.W500,
        fontSize = 11.sp,
        letterSpacing = 0.3.sp,
    )

    /** Supporting lines under a title, section labels, timestamps. */
    val caption = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 10.sp,
        letterSpacing = 0.08.sp,
    )

    /** Chips and badges. */
    val micro = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.W500,
        fontSize = 9.sp,
        letterSpacing = 0.04.sp,
    )

    /** Chart axis and data labels, where space is measured in pixels. */
    val nano = TextStyle(
        fontFamily = MonoFont,
        fontWeight = FontWeight.Normal,
        fontSize = 8.sp,
    )
}

fun TextStyle.bold(): TextStyle = copy(fontWeight = FontWeight.Bold)

fun TextStyle.normal(): TextStyle = copy(fontWeight = FontWeight.Normal)

/** Material components (dialogs, menus, the switch labels) follow the same scale. */
val CFTypography = Typography(
    headlineLarge = CFText.display,
    headlineMedium = CFText.heading,
    headlineSmall = CFText.heading,
    titleLarge = CFText.title,
    titleMedium = CFText.subtitle,
    titleSmall = CFText.subtitle,
    bodyLarge = CFText.subtitle,
    bodyMedium = CFText.body,
    bodySmall = CFText.caption,
    labelLarge = CFText.label,
    labelMedium = CFText.caption,
    labelSmall = CFText.micro,
)
