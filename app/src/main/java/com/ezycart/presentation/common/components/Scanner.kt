package com.ezycart.presentation.common.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onPreviewKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp

@Composable
fun BarcodeScannerListener(
    onBarcodeScanned: (String) -> Unit
) {
    val scannedText = remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }

    BasicTextField(
        value = scannedText.value,
        onValueChange = { scannedText.value = it },
        textStyle = TextStyle(color = Color.Transparent),
        modifier = Modifier
            .focusRequester(focusRequester)
            .onPreviewKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                    when (keyEvent.key) {
                        Key.Enter -> {
                            if (scannedText.value.isNotBlank()) {
                                onBarcodeScanned(scannedText.value.trim())
                                scannedText.value = ""
                            }
                            true
                        }
                        else -> false
                    }
                } else {
                    false
                }
            }
            .size(0.dp)
    )

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
}