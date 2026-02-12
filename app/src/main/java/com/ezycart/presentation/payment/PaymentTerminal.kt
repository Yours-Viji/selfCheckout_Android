package com.ezycart.presentation.payment

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.ezycart.payment.maybank.LogDisplay
import com.ezycart.payment.maybank.PaymentButtonsGrid

import com.ezycart.payment.maybank.PaymentTerminalManager
import com.ezycart.presentation.common.data.Constants
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun TerminalDebugDialog(
    onDismiss: () -> Unit,
    terminalIp: String = Constants.PAYMENT_TERMINAL_IP,
    terminalPort: Int = Constants.PAYMENT_TERMINAL_PORT
) {
    val context = LocalContext.current
    val paymentManager = remember { PaymentTerminalManager.getInstance(context) }
    val coroutineScope = rememberCoroutineScope()

    // States
    val logs = remember { mutableStateListOf<LogEntry>() }
    var connectionStatus by remember { mutableStateOf("Ready") }
    var isProcessing by remember { mutableStateOf(false) }

    // Helper to add logs
    fun addLog(message: String, type: LogType = LogType.INFO) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        logs.add(LogEntry(timestamp, message, type))
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // Allows for wider view
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f)
                .fillMaxHeight(0.8f),
            shape = MaterialTheme.shapes.large,
            color = MaterialTheme.colorScheme.surface
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                // Header
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        "Terminal Debug Console",
                        style = MaterialTheme.typography.headlineSmall,
                        modifier = Modifier.weight(1f)
                    )
                    if (isProcessing) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                }

                Text("IP: $terminalIp : $terminalPort", style = MaterialTheme.typography.bodySmall)

                Spacer(modifier = Modifier.height(8.dp))

                // Status Indicator
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            when(connectionStatus) {
                                "Connected" -> Color(0xFFE8F5E9)
                                "Failed" -> Color(0xFFFFEBEE)
                                else -> Color(0xFFF5F5F5)
                            }
                        )
                        .padding(8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        connectionStatus,
                        fontWeight = FontWeight.Bold,
                        color = when(connectionStatus) {
                            "Connected" -> Color(0xFF2E7D32)
                            "Failed" -> Color(0xFFC62828)
                            else -> Color.DarkGray
                        }
                    )
                }

                // Log Window (Terminal View)
                Text("Logs", style = MaterialTheme.typography.labelLarge, modifier = Modifier.padding(top = 8.dp))
                LogDisplay(
                    logs = logs,
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                        .background(Color.Black) // Terminal look
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Action Buttons
                PaymentButtonsGrid(
                    onConnect = {
                        isProcessing = true
                        connectionStatus = "Connecting..."
                        addLog("Pinging $terminalIp...")
                        paymentManager.pingTerminal(terminalIp, terminalPort) { reachable ->
                            if (reachable) {
                                addLog("Ping Success", LogType.SUCCESS)
                                paymentManager.logonToTerminal(terminalIp, terminalPort) { success ->
                                    coroutineScope.launch {
                                        isProcessing = false
                                        connectionStatus = if (success) "Connected" else "Failed"
                                        addLog(if (success) "Logon Successful" else "Logon Failed", if (success) LogType.SUCCESS else LogType.ERROR)
                                    }
                                }
                            } else {
                                coroutineScope.launch {
                                    isProcessing = false
                                    connectionStatus = "Failed"
                                    addLog("Terminal unreachable", LogType.ERROR)
                                }
                            }
                        }
                    },
                    onSale = {
                        addLog("Requesting RM 10.00 Sale...")
                        paymentManager.performSale(terminalIp, terminalPort, 1000L) { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) addLog("Sale Approved: ${result.approvalCode}", LogType.SUCCESS)
                                if (result.isDeclined) addLog("Sale Declined: ${result.description}", LogType.ERROR)
                            }
                        }
                    },
                    onQrSale = { /* Similar to Sale */ },
                    onCancel = { paymentManager.cancelTransaction(terminalIp, terminalPort) { addLog("Cancel Sent") } },
                    onVoid = { /* implementation */ },
                    onSettlement = {
                        addLog("Starting Settlement...")
                        paymentManager.performSettlement(terminalIp, terminalPort) { result ->
                            coroutineScope.launch { addLog(if (result.isSuccess) "Settlement OK" else "Settlement Failed", if (result.isSuccess) LogType.SUCCESS else LogType.ERROR) }
                        }
                    },
                    onCustomerFacing = { paymentManager.setCustomerFacingMode(terminalIp, terminalPort) { addLog("Customer Mode Set") } },
                    onMerchantFacing = { paymentManager.setMerchantFacingMode(terminalIp, terminalPort) { addLog("Merchant Mode Set") } }
                )

                Button(onClick = onDismiss, modifier = Modifier.fillMaxWidth().padding(top = 8.dp)) {
                    Text("Close Console")
                }
            }
        }
    }
}

@Composable
fun LogItem(log: LogEntry, modifier: Modifier = Modifier) {
    val textColor = when (log.type) {
        LogType.SUCCESS -> Color(0xFF00FF00) // Terminal Green
        LogType.ERROR -> Color(0xFFFF4444)   // Bright Red
        LogType.WARNING -> Color(0xFFFFBB33) // Orange
        LogType.INFO -> Color.White
    }

    Row(modifier = modifier) {
        Text(
            text = "${log.timestamp} > ",
            color = Color.DarkGray,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace
        )
        Text(
            text = log.message,
            color = textColor,
            fontSize = 11.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.weight(1f)
        )
    }
}

// Data classes for logs
data class LogEntry(
    val timestamp: String,
    val message: String,
    val type: LogType
)

enum class LogType {
    INFO,
    SUCCESS,
    ERROR,
    WARNING
}
