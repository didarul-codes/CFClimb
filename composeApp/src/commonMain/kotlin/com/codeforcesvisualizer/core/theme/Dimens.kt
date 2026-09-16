package com.codeforcesvisualizer.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

/** Every rounded corner in the app comes from here. */
object CFShapes {
    /** Cards, list rows, hero blocks. */
    val card = RoundedCornerShape(12.dp)

    /** Buttons, tab bars, input fields. */
    val control = RoundedCornerShape(8.dp)

    /** Chips and badges. */
    val chip = RoundedCornerShape(4.dp)

    /** Progress bars and chart bars. */
    val bar = RoundedCornerShape(2.dp)

    /** The floating bottom navigation. */
    val pill = RoundedCornerShape(22.dp)

    /** Fully round, for avatars and pill buttons that follow their content. */
    val full = RoundedCornerShape(50)
}

/** The spacing steps screens are laid out on. */
object CFSpace {
    /** Distance from the screen edge to any content. */
    val gutter = 16.dp

    /** Inset for a centred message, so a full-width line still reads as a block. */
    val wideGutter = 24.dp

    /** Padding inside a card or list row. */
    val card = 14.dp

    /** Between two sections. */
    val section = 12.dp

    /** Between a title and the line under it. */
    val tight = 6.dp

    /** Between items in a list or a row of chips. */
    val item = 4.dp
}

/** Named alpha values, so colour tints stop being 0x22-style literals. */
object CFAlpha {
    /** Background tint of a coloured control or chip. */
    const val FILL = 0.15f

    /** Background tint of a chip or row that should recede. */
    const val FILL_SUBTLE = 0.08f

    /** Border of a coloured control or chip. */
    const val BORDER = 0.30f

    /** The coloured end of a gradient. */
    const val WASH = 0.12f

    /** Placeholder text and resting input borders. */
    const val MUTED = 0.5f
}
