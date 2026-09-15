package com.codeforcesvisualizer.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.SoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchText: String = "",
    placeholderText: String = "",
    onSearchTextChanged: (String) -> Unit,
    onSearch: (() -> Unit)? = null,
    onClearText: () -> Unit,
    onNavigateBack: () -> Unit,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    CFAppBar(
        modifier = modifier.fillMaxWidth(),
        title = "",
        onNavigateBack = onNavigateBack,
        actions = {
            SearchBarInputField(
                searchText = searchText,
                placeholderText = placeholderText,
                focusRequester = focusRequester,
                keyboardController = keyboardController,
                onSearchTextChanged = onSearchTextChanged,
                onSearch = if (onSearch == null) null else {
                    {
                        onSearch.invoke()
                    }
                },
                onClearText = onClearText,
            )
        }
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}

@OptIn(ExperimentalComposeUiApi::class, ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
@Composable
internal fun SearchBarInputField(
    modifier: Modifier = Modifier,
    searchText: String = "",
    placeholderText: String = "",
    focusRequester: FocusRequester,
    keyboardController: SoftwareKeyboardController?,
    onSearchTextChanged: (String) -> Unit,
    onSearch: (() -> Unit)? = null,
    onClearText: () -> Unit,
) {
    var showClearButton by remember { mutableStateOf(false) }
    var textFieldValue by remember {
        mutableStateOf(TextFieldValue(searchText, selection = TextRange(searchText.length)))
    }

    // Follow changes made outside the field, such as tapping a recent search.
    LaunchedEffect(searchText) {
        if (searchText != textFieldValue.text) {
            textFieldValue = TextFieldValue(searchText, selection = TextRange(searchText.length))
        }
    }

    TextField(
        modifier = modifier
            .padding(vertical = 2.dp)
            .onFocusChanged { showClearButton = it.isFocused }
            .focusRequester(focusRequester),
        value = textFieldValue,
        onValueChange = { value ->
            textFieldValue = value
            onSearchTextChanged(value.text)
        },
        placeholder = { Text(text = placeholderText) },
        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
            errorContainerColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            cursorColor = MaterialTheme.colorScheme.primary,
            focusedTextColor = MaterialTheme.colorScheme.onSurface,
            unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
            focusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedPlaceholderColor = MaterialTheme.colorScheme.onSurfaceVariant,
            focusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
            unfocusedTrailingIconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        trailingIcon = {
            SearchBarTrailingIcon(
                visible = showClearButton,
                onClearText = {
                    textFieldValue = TextFieldValue("")
                    onClearText()
                },
                onSearch = onSearch
            )
        },
        singleLine = true,
        keyboardOptions = KeyboardOptions.Default.copy(imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Done),
        keyboardActions = KeyboardActions(
            onSearch = {
                keyboardController?.hide()
                onSearch?.invoke()
            },
            onDone = {
                keyboardController?.hide()
            }
        )
    )
}


@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun SearchBarTrailingIcon(
    modifier: Modifier = Modifier,
    visible: Boolean,
    onClearText: () -> Unit,
    onSearch: (() -> Unit)? = null,
) {
    AnimatedVisibility(
        modifier = modifier,
        visible = visible,
        enter = fadeIn(),
        exit = fadeOut()
    ) {
        Row {
            IconButton(onClick = onClearText) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "Clear"
                )
            }
            if (onSearch != null) {
                IconButton(onClick = onSearch) {
                    Icon(
                        imageVector = Icons.Filled.Search,
                        contentDescription = "Search"
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun Preview() {
    SearchBar(
        onSearchTextChanged = {},
        onClearText = { /*TODO*/ },
        onNavigateBack = { /*TODO*/ }
    )
}