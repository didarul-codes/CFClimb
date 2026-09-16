package com.codeforcesvisualizer.core.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Row
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
import androidx.compose.foundation.layout.fillMaxSize
import com.codeforcesvisualizer.core.theme.CFSpace

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    searchText: String = "",
    placeholderText: String = "",
    onSearchTextChanged: (String) -> Unit,
    onSearch: (() -> Unit)? = null,
    onClearText: () -> Unit,
    /** Off when the screen opens with a result already, so the keyboard doesn't cover it. */
    requestFocusOnStart: Boolean = true,
) {
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    SearchBarInputField(
        modifier = modifier.fillMaxWidth().padding(horizontal = CFSpace.gutter),
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

    LaunchedEffect(Unit) {
        if (requestFocusOnStart) focusRequester.requestFocus()
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

    CFTextField(
        modifier = modifier
            .onFocusChanged { showClearButton = it.isFocused }
            .focusRequester(focusRequester),
        value = textFieldValue,
        onValueChange = { value ->
            textFieldValue = value
            onSearchTextChanged(value.text)
        },
        placeholder = placeholderText,
        imeAction = if (onSearch != null) ImeAction.Search else ImeAction.Done,
        onImeAction = {
            keyboardController?.hide()
            onSearch?.invoke()
        },
        trailing = {
            SearchBarTrailingIcon(
                visible = showClearButton,
                onClearText = {
                    textFieldValue = TextFieldValue("")
                    onClearText()
                },
                onSearch = onSearch
            )
        },
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
    )
}