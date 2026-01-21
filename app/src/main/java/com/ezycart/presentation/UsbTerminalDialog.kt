package com.ezycart.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Send

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButtonDefaults.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ezycart.presentation.home.HomeViewModel
import com.ezycart.services.usb.com.LoginWeightScaleSerialPort
import kotlinx.coroutines.launch

@Composable
fun UsbTerminalDialog(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    // Collect the full history of messages
    val terminalLogs = viewModel.terminalContent.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val inputText = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // 1. AUTO-CONNECT ON OPEN: Mimics the GitHub "Connect" behavior
    LaunchedEffect(Unit) {
        viewModel.logStatus("Initializing Scale Connection...")

        // Use the common listener we created in previous step
        val listener = LoginWeightScaleSerialPort.createCommonListener(context, viewModel)
        LoginWeightScaleSerialPort.connectScale(context, listener)
    }

    // 2. AUTO-SCROLL: Keep the green text moving up as new data arrives
    LaunchedEffect(terminalLogs) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212)
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("SERIAL MONITOR", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    Row {
                        // Optional Clear Button
                        IconButton(onClick = { viewModel.clearLogs() }) {
                            androidx.compose.material3.Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Gray)
                        }
                        IconButton(onClick = onDismiss) {
                            androidx.compose.material3.Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        }
                    }
                }

                // Terminal Display Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black, shape = RoundedCornerShape(4.dp))
                        .padding(8.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = terminalLogs.value,
                            color = Color(0xFF00FF00), // Classic Terminal Green
                            fontFamily = FontFamily.Monospace,
                            fontSize = 13.sp,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Input Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inputText.value,
                        onValueChange = { inputText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color(0xFF222222), shape = RoundedCornerShape(8.dp))
                            .padding(14.dp),
                        textStyle = TextStyle(color = Color.White, fontFamily = FontFamily.Monospace),
                        cursorBrush = SolidColor(Color.Green),
                        decorationBox = { innerTextField ->
                            if (inputText.value.isEmpty()) {
                                Text("Type command (e.g. TARE)", color = Color.DarkGray)
                            }
                            innerTextField()
                        }
                    )

                    IconButton(
                        onClick = {
                            if (inputText.value.isNotBlank()) {
                                // Send to hardware
                                LoginWeightScaleSerialPort.sendMessageToWeightScale("${inputText.value}\r\n")
                                // Log the sent command to the screen
                                viewModel.logStatus("TX: ${inputText.value}")
                                inputText.value = ""
                            }
                        }
                    ) {
                        androidx.compose.material3.Icon(Icons.Default.Send, contentDescription = "Send", tint = Color.Green)
                    }
                }
            }
        }
    }
}