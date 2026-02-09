package com.ezycart.payment.maybank


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ezycart.payment.maybank.PaymentResult.Status
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class PaymentActivity : ComponentActivity() {

    private lateinit var paymentManager: PaymentTerminalManager

    // Terminal configuration (get these from settings or configuration)
    private val terminalIp = "192.168.0.100" // Replace with actual terminal IP
    private val terminalPort = 8080 // Replace with actual terminal port

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize Payment Manager
        paymentManager = PaymentTerminalManager.getInstance(applicationContext)

        setContent {
            PaymentScreen(
                paymentManager = paymentManager,
                terminalIp = terminalIp,
                terminalPort = terminalPort,
                onBackPressed = { finish() }
            )
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        paymentManager.cleanup()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PaymentScreen(
    paymentManager: PaymentTerminalManager,
    terminalIp: String,
    terminalPort: Int,
    onBackPressed: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // State for logs
    val logs = remember { mutableStateListOf<LogEntry>() }

    // State for connection status
    var connectionStatus by remember { mutableStateOf("Disconnected") }
    var isProcessing by remember { mutableStateOf(false) }

    // Function to add log messages
    fun addLog(message: String, type: LogType = LogType.INFO) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        val entry = LogEntry(timestamp = timestamp, message = message, type = type)

        coroutineScope.launch {
            logs.add(entry)

            // Keep only last 100 logs
            if (logs.size > 100) {
                logs.removeAt(0)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Payment Terminal Integration") },
                actions = {
                    if (isProcessing) {
                        CircularProgressIndicator(
                            modifier = Modifier
                                .padding(8.dp)
                                .width(24.dp)
                                .height(24.dp)
                        )
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        content = { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                // Status Card
                Card(
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Terminal Status",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = connectionStatus,
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.Bold,
                            color = when (connectionStatus) {
                                "Connected to Terminal" -> Color(0xFF4CAF50) // Green
                                "Connection Failed" -> Color(0xFFF44336) // Red
                                "Terminal Not Found" -> Color(0xFFFF9800) // Orange
                                else -> MaterialTheme.colorScheme.primary
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Log Display
                Text(
                    text = "Activity Log",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                LogDisplay(
                    logs = logs,
                    modifier = Modifier
                        .weight(1f)
                        .background(Color(0xFFF5F5F5))
                        .padding(8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Buttons Grid
                PaymentButtonsGrid(
                    onConnect = {
                        isProcessing = true
                        connectionStatus = "Connecting..."
                        addLog("Connecting to terminal...")

                        // First ping to check if terminal is reachable
                        paymentManager.pingTerminal(terminalIp, terminalPort) { isReachable ->
                            coroutineScope.launch {
                                if (isReachable) {
                                    addLog("✓ Terminal is reachable", LogType.SUCCESS)

                                    // Then logon to establish connection
                                    paymentManager.logonToTerminal(terminalIp, terminalPort) { logonSuccess ->
                                        coroutineScope.launch {
                                            if (logonSuccess) {
                                                addLog("✓ Logon successful", LogType.SUCCESS)
                                                connectionStatus = "Connected to Terminal"
                                            } else {
                                                addLog("✗ Logon failed", LogType.ERROR)
                                                connectionStatus = "Connection Failed"
                                            }
                                            isProcessing = false
                                        }
                                    }
                                } else {
                                    addLog("✗ Terminal not reachable", LogType.ERROR)
                                    connectionStatus = "Terminal Not Found"
                                    isProcessing = false
                                }
                            }
                        }
                    },
                    onSale = {
                        val amount = 1000L // RM 10.00 in cents
                        addLog("Initiating sale: RM ${amount / 100}.00")

                        paymentManager.performSale(terminalIp, terminalPort, amount) { result ->
                            coroutineScope.launch {
                                when {
                                    result.isProcessing -> {
                                        addLog("⏳ Transaction processing: ${result.description}", LogType.INFO)
                                        connectionStatus = "Processing..."
                                    }
                                    result.isSuccess -> {
                                        addLog("✓ Sale successful!", LogType.SUCCESS)
                                        addLog("  Transaction ID: ${result.transactionId}", LogType.INFO)
                                        addLog("  Amount: RM ${result.amount.toDoubleOrNull()?.div(100) ?: 0}", LogType.INFO)
                                        addLog("  Approval: ${result.approvalCode}", LogType.INFO)
                                        addLog("  RRN: ${result.rrn}", LogType.INFO)
                                        addLog("  Issuer: ${result.issuer}", LogType.INFO)
                                        connectionStatus = "Sale Approved"
                                    }
                                    result.isDeclined -> {
                                        addLog("✗ Sale declined: ${result.description}", LogType.ERROR)
                                        connectionStatus = "Sale Declined"
                                    }
                                    result.isCancelled -> {
                                        addLog("⚠ Sale cancelled: ${result.description}", LogType.WARNING)
                                        connectionStatus = "Sale Cancelled"
                                    }
                                    result.isError -> {
                                        addLog("✗ Error: ${result.errorMessage}", LogType.ERROR)
                                        connectionStatus = "Error"
                                    }
                                }
                            }
                        }
                    },
                    onQrSale = {
                        val amount = 1500L // RM 15.00 in cents
                        addLog("Initiating QR sale: RM ${amount / 100}.00")

                        paymentManager.performQrSale(terminalIp, terminalPort, amount) { result ->
                            coroutineScope.launch {
                                when {
                                    result.isProcessing -> {
                                        addLog("⏳ QR transaction processing: ${result.description}", LogType.INFO)
                                        connectionStatus = "QR Processing..."
                                    }
                                    result.isSuccess -> {
                                        addLog("✓ QR sale successful!", LogType.SUCCESS)
                                        addLog("  Transaction ID: ${result.transactionId}", LogType.INFO)
                                        addLog("  Amount: RM ${result.amount.toDoubleOrNull()?.div(100) ?: 0}", LogType.INFO)
                                        connectionStatus = "QR Sale Approved"
                                    }
                                    result.isDeclined -> {
                                        addLog("✗ QR sale declined: ${result.description}", LogType.ERROR)
                                        connectionStatus = "QR Sale Declined"
                                    }
                                    result.isError -> {
                                        addLog("✗ QR Error: ${result.errorMessage}", LogType.ERROR)
                                        connectionStatus = "QR Error"
                                    }
                                }
                            }
                        }
                    },
                    onCancel = {
                        addLog("Cancelling current transaction...")

                        paymentManager.cancelTransaction(terminalIp, terminalPort) { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    addLog("✓ Transaction cancelled", LogType.SUCCESS)
                                    connectionStatus = "Transaction Cancelled"
                                } else {
                                    addLog("✗ Cancel failed: ${result.errorMessage}", LogType.ERROR)
                                    connectionStatus = "Cancel Failed"
                                }
                            }
                        }
                    },
                    onVoid = {
                        val invoiceNumber = 1 // Replace with actual invoice number
                        addLog("Voiding invoice #$invoiceNumber...")

                        paymentManager.performVoid(terminalIp, terminalPort, invoiceNumber) { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    addLog("✓ Void successful", LogType.SUCCESS)
                                    connectionStatus = "Void Approved"
                                } else {
                                    addLog("✗ Void failed: ${result.errorMessage}", LogType.ERROR)
                                    connectionStatus = "Void Failed"
                                }
                            }
                        }
                    },
                    onSettlement = {
                        addLog("Performing settlement...")

                        paymentManager.performSettlement(terminalIp, terminalPort) { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    addLog("✓ Settlement successful", LogType.SUCCESS)
                                    connectionStatus = "Settlement Complete"
                                } else {
                                    addLog("✗ Settlement failed: ${result.errorMessage}", LogType.ERROR)
                                    connectionStatus = "Settlement Failed"
                                }
                            }
                        }
                    },
                    onCustomerFacing = {
                        addLog("Setting terminal to Customer Facing mode...")

                        paymentManager.setCustomerFacingMode(terminalIp, terminalPort) { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    addLog("✓ Terminal set to Customer Facing", LogType.SUCCESS)
                                    connectionStatus = "Customer Facing Mode"
                                } else {
                                    addLog("✗ Failed to set Customer Facing: ${result.errorMessage}", LogType.ERROR)
                                }
                            }
                        }
                    },
                    onMerchantFacing = {
                        addLog("Setting terminal to Merchant Facing mode...")

                        paymentManager.setMerchantFacingMode(terminalIp, terminalPort) { result ->
                            coroutineScope.launch {
                                if (result.isSuccess) {
                                    addLog("✓ Terminal set to Merchant Facing", LogType.SUCCESS)
                                    connectionStatus = "Merchant Facing Mode"
                                } else {
                                    addLog("✗ Failed to set Merchant Facing: ${result.errorMessage}", LogType.ERROR)
                                }
                            }
                        }
                    }
                )
            }
        }
    )
}

@Composable
fun PaymentButtonsGrid(
    onConnect: () -> Unit,
    onSale: () -> Unit,
    onQrSale: () -> Unit,
    onCancel: () -> Unit,
    onVoid: () -> Unit,
    onSettlement: () -> Unit,
    onCustomerFacing: () -> Unit,
    onMerchantFacing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Row 1
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onConnect,
                modifier = Modifier.weight(1f)
            ) {
                Text("Connect")
            }

            Button(
                onClick = onSale,
                modifier = Modifier.weight(1f)
            ) {
                Text("Sale")
            }
        }

        // Row 2
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onQrSale,
                modifier = Modifier.weight(1f)
            ) {
                Text("QR Sale")
            }

            Button(
                onClick = onCancel,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }
        }

        // Row 3
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onVoid,
                modifier = Modifier.weight(1f)
            ) {
                Text("Void")
            }

            Button(
                onClick = onSettlement,
                modifier = Modifier.weight(1f)
            ) {
                Text("Settlement")
            }
        }

        // Row 4
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onCustomerFacing,
                modifier = Modifier.weight(1f)
            ) {
                Text("Customer Facing")
            }

            Button(
                onClick = onMerchantFacing,
                modifier = Modifier.weight(1f)
            ) {
                Text("Merchant Facing")
            }
        }
    }
}

@Composable
fun LogDisplay(
    logs: List<LogEntry>,
    modifier: Modifier = Modifier
) {
    val listState = rememberLazyListState()

    // Scroll to bottom when new logs are added
    LaunchedEffect(logs.size) {
        if (logs.isNotEmpty()) {
            listState.animateScrollToItem(logs.size - 1)
        }
    }

    LazyColumn(
        state = listState,
        modifier = modifier,
        reverseLayout = false
    ) {
        itemsIndexed(logs) { index, log ->
            LogItem(
                log = log,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 2.dp)
            )
        }
    }
}

@Composable
fun LogItem(
    log: LogEntry,
    modifier: Modifier = Modifier
) {
    val textColor = when (log.type) {
        LogType.SUCCESS -> Color(0xFF4CAF50) // Green
        LogType.ERROR -> Color(0xFFF44336) // Red
        LogType.WARNING -> Color(0xFFFF9800) // Orange
        LogType.INFO -> Color.Black
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "[${log.timestamp}]",
            color = Color.Gray,
            fontSize = 12.sp,
            fontFamily = FontFamily.Monospace,
            modifier = Modifier.width(70.dp)
        )

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = log.message,
            color = textColor,
            fontSize = 12.sp,
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

// If you need a ViewModel for more complex state management:
/*
@HiltViewModel
class PaymentViewModel @Inject constructor(
    private val paymentManager: PaymentTerminalManager
) : ViewModel() {

    private val _logs = mutableStateListOf<LogEntry>()
    val logs: SnapshotStateList<LogEntry> = _logs

    private val _connectionStatus = mutableStateOf("Disconnected")
    val connectionStatus = _connectionStatus.asStateFlow()

    private val _isProcessing = mutableStateOf(false)
    val isProcessing = _isProcessing.asStateFlow()

    fun addLog(message: String, type: LogType = LogType.INFO) {
        val timestamp = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        _logs.add(LogEntry(timestamp, message, type))

        // Keep only last 100 logs
        if (_logs.size > 100) {
            _logs.removeAt(0)
        }
    }

    fun connectToTerminal(terminalIp: String, terminalPort: Int) {
        // Implementation...
    }

    // Other functions...
}
*/