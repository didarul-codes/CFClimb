package com.codeforcesvisualizer.preference

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.codeforcesvisualizer.core.components.HeightSpacer
import com.codeforcesvisualizer.core.theme.CFThemeColors
import com.codeforcesvisualizer.core.theme.DarkBg
import com.codeforcesvisualizer.core.theme.DarkBorder
import com.codeforcesvisualizer.core.theme.DarkSurface
import com.codeforcesvisualizer.core.theme.LightBg
import com.codeforcesvisualizer.core.theme.LightBorder
import com.codeforcesvisualizer.core.theme.LightSurface
import com.codeforcesvisualizer.core.theme.Violet
import com.codeforcesvisualizer.shared.domain.entity.UiThemeMode
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.bold
import com.codeforcesvisualizer.core.theme.CFShapes
import androidx.compose.ui.unit.sp

@Composable
fun AppearanceSection(
    modifier: Modifier = Modifier,
    themeMode: UiThemeMode,
    onThemeModeChanged: (UiThemeMode) -> Unit
) {
    val colors = CFThemeColors.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ThemePreviewButton(
            label = "dark",
            isSelected = themeMode == UiThemeMode.Dark,
            previewBg = DarkBg,
            previewSurface = DarkSurface,
            previewBorder = DarkBorder,
            onClick = { onThemeModeChanged(UiThemeMode.Dark) },
            modifier = Modifier.weight(1f),
        )
        ThemePreviewButton(
            label = "light",
            isSelected = themeMode == UiThemeMode.Light,
            previewBg = LightBg,
            previewSurface = LightSurface,
            previewBorder = LightBorder,
            onClick = { onThemeModeChanged(UiThemeMode.Light) },
            modifier = Modifier.weight(1f),
        )
        ThemePreviewButton(
            label = "system",
            isSelected = themeMode == UiThemeMode.System,
            previewBg = DarkBg,
            previewSurface = LightSurface,
            previewBorder = DarkBorder,
            onClick = { onThemeModeChanged(UiThemeMode.System) },
            modifier = Modifier.weight(1f),
        )
    }
}

@Composable
private fun ThemePreviewButton(
    label: String,
    isSelected: Boolean,
    previewBg: Color,
    previewSurface: Color,
    previewBorder: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val colors = CFThemeColors.current
    val borderColor = if (isSelected) colors.violet else colors.border
    val shape = CFShapes.control

    Column(
        modifier = modifier
            .clip(shape)
            .background(colors.surface)
            .border(width = if (isSelected) 2.dp else 1.dp, color = borderColor, shape = shape)
            .clickable { onClick() }
            .padding(10.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        // Mini preview card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .clip(CFShapes.chip)
                .background(previewBg)
                .border(1.dp, previewBorder, CFShapes.chip)
                .padding(8.dp),
        ) {
            Column(verticalArrangement = Arrangement.SpaceBetween) {
                Box(
                    modifier = Modifier
                        .width(40.dp)
                        .height(4.dp)
                        .clip(CFShapes.bar)
                        .background(Violet)
                )
                HeightSpacer(height = 4.dp)
                Box(
                    modifier = Modifier
                        .width(28.dp)
                        .height(4.dp)
                        .clip(CFShapes.bar)
                        .background(previewSurface)
                )
                HeightSpacer(height = 4.dp)
                Box(
                    modifier = Modifier
                        .width(34.dp)
                        .height(4.dp)
                        .clip(CFShapes.bar)
                        .background(previewSurface)
                )
            }
        }

        HeightSpacer(height = 8.dp)

        Text(
            text = label,
            style = (if (isSelected) CFText.label.bold() else CFText.label)
                .copy(color = if (isSelected) colors.violet else colors.dim),
        )
    }
}
