package com.ezycart.presentation

import android.content.Context
import android.hardware.usb.UsbManager
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Send
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon

import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ezycart.R
import com.ezycart.presentation.home.HomeViewModel
import com.ezycart.services.usb.LoadCellSerialPort
import com.ezycart.services.usb.StatusActionRow

/*
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
        val listener = LoadCellSerialPort.createCommonListener(viewModel)
        LoadCellSerialPort.connectPicoScaleDirectly(context, listener)
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

                                LoadCellSerialPort.sendMessageToWeightScale("${inputText.value}\r\n")
                                // Log the sent command to the screen
                                viewModel.logStatus("TX: ${inputText.value}")
                                viewModel.sendMessageToLoadCell(inputText.value)
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
}*/

@Composable
fun UsbTerminalDialog(
    onDismiss: () -> Unit,
    viewModel: HomeViewModel = hiltViewModel()
) {
    val terminalLogs = viewModel.terminalContent.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val inputText = remember { mutableStateOf("") }
    val scrollState = rememberScrollState()

    // Permission and Connection State
    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    val picoDevice = usbManager.deviceList.values.find { it.vendorId == 11914 && it.productId == 10 }
    val hasPermission = remember { mutableStateOf(picoDevice?.let { usbManager.hasPermission(it) } ?: false) }

    // Auto-scroll logic
    LaunchedEffect(terminalLogs.value) {
        scrollState.animateScrollTo(scrollState.maxValue)
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.85f)
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // 1. Header with Icon
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(48.dp).background(Color(0xFFE3F2FD), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_scale),
                                contentDescription = null,
                                tint = Color(0xFF1976D2),
                                modifier = Modifier.size(32.dp)
                            )
                           // Icon(Icons.Default.Send, contentDescription = null, tint = Color(0xFF1976D2), modifier = Modifier.size(24.dp))
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Text("Scale Terminal", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.ExtraBold))
                    }
                    IconButton(onClick = onDismiss) {
                        Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.Black)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Connection Status & Force Connect Button
                StatusActionRow(
                    title = "Load Cell Connection",
                    subtitle = if (hasPermission.value) "Pico Connected" else "Pico Disconnected",
                    isError = !hasPermission.value,
                    buttonLabel = if (hasPermission.value) "Reset" else "Connect",
                    buttonIcon = if (hasPermission.value) Icons.Default.CheckCircle else Icons.Default.Warning,
                    onAction = {
                        val listener = LoadCellSerialPort.createCommonListener(viewModel)
                        LoadCellSerialPort.connectPicoScaleDirectly(context, listener)
                        hasPermission.value = picoDevice?.let { usbManager.hasPermission(it) } ?: false
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Terminal Display Area (Green on Black)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color(0xFF0A0A0A), shape = RoundedCornerShape(16.dp))
                        .padding(12.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = terminalLogs.value,
                            color = Color(0xFF00E676),
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.verticalScroll(scrollState)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 4. Input Bar with "Clear" and "Send"
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    // Clear Logs Button
                    IconButton(
                        onClick = { viewModel.clearLogs() },
                        modifier = Modifier.background(Color(0xFFF5F5F5), CircleShape)
                    ) {
                        Icon(Icons.Default.Delete, contentDescription = "Clear", tint = Color.Red)
                    }

                    // Command Input
                    BasicTextField(
                        value = inputText.value,
                        onValueChange = { inputText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            // Added a Border to make it visible
                            .border(
                                width = 1.dp,
                                color = Color(0xFFE0E0E0),
                                shape = RoundedCornerShape(12.dp)
                            )
                            // Slightly darker background than the card to create depth
                            .background(Color(0xFFF1F3F5), shape = RoundedCornerShape(12.dp))
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        textStyle = TextStyle(
                            color = Color.Black,
                            fontFamily = FontFamily.Monospace,
                            fontWeight = FontWeight.Bold,
                            fontSize = 15.sp
                        ),
                        cursorBrush = SolidColor(Color(0xFF1976D2)),
                        decorationBox = { innerTextField ->
                            Box(contentAlignment = Alignment.CenterStart) {
                                if (inputText.value.isEmpty()) {
                                    Text(
                                        text = "Enter Command...",
                                        style = TextStyle(
                                            color = Color.Gray,
                                            fontFamily = FontFamily.Monospace,
                                            fontSize = 15.sp
                                        )
                                    )
                                }
                                innerTextField()
                            }
                        }
                    )

                    // Prominent Send Button
                    Button(
                        onClick = {
                            if (inputText.value.isNotBlank()) {
                                // FIXED: Send via LoadCellSerialPort directly
                                LoadCellSerialPort.sendMessageToWeightScale("${inputText.value.uppercase()}\r\n")
                                viewModel.logStatus("TX > ${inputText.value.uppercase()}")
                                inputText.value = ""
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF1976D2)),
                        modifier = Modifier.height(52.dp)
                    ) {
                        Icon(Icons.Default.Send, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(Modifier.width(8.dp))
                        Text("SEND")
                    }
                }
            }
        }
    }
}
