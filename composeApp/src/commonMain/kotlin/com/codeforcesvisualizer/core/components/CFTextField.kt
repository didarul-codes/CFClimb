package com.codeforcesvisualizer.core.components

import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import com.codeforcesvisualizer.core.theme.CFAlpha
import com.codeforcesvisualizer.core.theme.CFShapes
import com.codeforcesvisualizer.core.theme.CFText
import com.codeforcesvisualizer.core.theme.CFThemeColors
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.TextRange

/**
 * The app's one text input: a surface-filled box with the control radius, so the handle
 * field in Settings, the search bars and the compare pickers all look the same.
 */
@Composable
fun CFTextField(
    value: TextFieldValue,
    onValueChange: (TextFieldValue) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: TextStyle = CFText.subtitle,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    accent: Color = CFThemeColors.current.violet,
) {
    val colors = CFThemeColors.current
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier,
        singleLine = true,
        placeholder = {
            Text(
                text = placeholder,
                style = textStyle.copy(color = colors.dim.copy(alpha = CFAlpha.MUTED)),
            )
        },
        textStyle = textStyle.copy(color = colors.fg),
        trailingIcon = trailing,
        colors = OutlinedTextFieldDefaults.colors(
            focusedContainerColor = colors.surface2,
            unfocusedContainerColor = colors.surface2,
            focusedBorderColor = accent.copy(alpha = CFAlpha.MUTED),
            unfocusedBorderColor = colors.border,
            cursorColor = accent,
            focusedTrailingIconColor = colors.dim,
            unfocusedTrailingIconColor = colors.dim,
        ),
        shape = CFShapes.control,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = imeAction),
        keyboardActions = KeyboardActions(
            onSearch = { onImeAction?.invoke() },
            onDone = { onImeAction?.invoke() },
            onGo = { onImeAction?.invoke() },
        ),
    )
}

/**
 * Plain-string version. It keeps the cursor itself: rebuilding a [TextFieldValue] from the
 * string on every keystroke would send the caret back to the start and type the text backwards.
 */
@Composable
fun CFTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "",
    textStyle: TextStyle = CFText.subtitle,
    imeAction: ImeAction = ImeAction.Done,
    onImeAction: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
    accent: Color = CFThemeColors.current.violet,
) {
    var fieldValue by remember {
        mutableStateOf(TextFieldValue(value, selection = TextRange(value.length)))
    }
    // Follow changes made outside the field, such as tapping a recent search.
    LaunchedEffect(value) {
        if (value != fieldValue.text) {
            fieldValue = TextFieldValue(value, selection = TextRange(value.length))
        }
    }

    CFTextField(
        value = fieldValue,
        onValueChange = {
            fieldValue = it
            onValueChange(it.text)
        },
        modifier = modifier,
        placeholder = placeholder,
        textStyle = textStyle,
        imeAction = imeAction,
        onImeAction = onImeAction,
        trailing = trailing,
        accent = accent,
    )
}
