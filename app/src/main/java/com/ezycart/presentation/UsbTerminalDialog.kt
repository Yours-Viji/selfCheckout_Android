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
import kotlinx.coroutines.launch

@Composable
fun UsbTerminalDialog(
    onDismiss: () -> Unit,
    sensorViewModel: SensorSerialPortViewModel = hiltViewModel(),
    viewModel : HomeViewModel = hiltViewModel()
) {
    // Collecting the serial message flow from your existing ViewModel
    //val terminalLogs = sensorViewModel.connectionLog.collectAsStateWithLifecycle()
    val message = viewModel.errorMessage.collectAsStateWithLifecycle()
    var inputText = remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new logs arrive
    LaunchedEffect(message) {
        coroutineScope.launch {
            // Logic to keep the terminal scrolled to the bottom
            listState.animateScrollToItem(Int.MAX_VALUE)
        }
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFF121212) // Dark background like the terminal
        ) {
            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                // Header
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("USB Terminal", color = Color.White, style = MaterialTheme.typography.titleMedium)
                    IconButton(onClick = onDismiss) {
                        androidx.compose.material3.Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
                        //I
                    }
                }

                // Black Screen (Terminal Log Area)
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black)
                        .padding(8.dp)
                ) {
                    SelectionContainer {
                        Text(
                            text = message.value,
                            color = Color(0xFF00FF00), // Classic Green Terminal Text
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp,
                            modifier = Modifier.verticalScroll(rememberScrollState())
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Input Area (Edit Box and Send Button)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    BasicTextField(
                        value = inputText.value,
                        onValueChange = { inputText.value = it },
                        modifier = Modifier
                            .weight(1f)
                            .background(Color.DarkGray, shape = RoundedCornerShape(4.dp))
                            .padding(12.dp),
                        // FIXED: Using the correct Compose TextStyle
                        textStyle = TextStyle(
                            color = Color.White,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 16.sp
                        ),
                        cursorBrush = SolidColor(Color.Green)
                    )

                    IconButton(
                        onClick = {
                            if (inputText.value.isNotBlank()) {
                                sensorViewModel.sendCommand(inputText.value) // Calls your sendMessageToWeightScale
                                inputText.value = ""
                            }
                        }
                    ) {
                        androidx.compose.material3.Icon(
                            imageVector = Icons.Default.Send,
                            contentDescription = "Help",
                            tint = Color.Green,

                        )
                       /* Icon(
                            painter = painterResource(id = android.R.drawable.ic_menu_send),
                            contentDescription = "Send",
                            tint = Color.Green
                        )*/
                    }
                }
            }
        }
    }
}