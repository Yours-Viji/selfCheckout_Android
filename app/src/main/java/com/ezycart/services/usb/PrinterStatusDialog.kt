package com.ezycart.services.usb

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Divider
import androidx.compose.material3.TextButton
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import com.ezycart.R

@Composable
fun PrinterStatusDialog(
    printer: BixolonUsbPrinter,
    onDismiss: () -> Unit
) {
    var paperStatus = remember { mutableStateOf("TAP TO CHECK") }
    var hasPermission = remember { mutableStateOf(printer.hasUsbPermission()) }

    val (statusLabel, statusColor) = when(paperStatus.value) {
        "OK" -> "Paper Ready" to Color(0xFF2E7D32)
        "NEAR_EMPTY" -> "Low Paper" to Color(0xFFEF6C00)
        "EMPTY" -> "Out of Paper" to Color(0xFFC62828)
        else -> "Unknown" to Color.Gray
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = true)
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFF3E5F5), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_print),
                        contentDescription = null,
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Printer Service",
                    style = TextStyle(fontSize = 22.sp, fontWeight = FontWeight.ExtraBold)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. USB Permission Section
                StatusActionRow(
                    title = "USB Permission",
                    subtitle = if (hasPermission.value) "Hardware Linked" else "Access Required",
                    isError = !hasPermission.value,
                    buttonLabel = if (hasPermission.value) "Re-check" else "Authorize",
                    // Added Icon for Permission
                    buttonIcon = if (hasPermission.value) Icons.Default.CheckCircle else Icons.Default.Warning,
                    onAction = {
                        printer.requestUsbPermission { hasPermission.value = it }
                    }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Paper Status Section
                StatusActionRow(
                    title = "Paper Supply",
                    subtitle = statusLabel,
                    isError = paperStatus.value == "EMPTY",
                    buttonLabel = "Check Paper",
                    // Added Icon for Paper
                    buttonIcon = Icons.Default.Info,
                    onAction = {
                        paperStatus.value = printer.checkPaperStatus()
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Text("CLOSE DIAGNOSTICS", fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun StatusActionRow(
    title: String,
    subtitle: String,
    isError: Boolean,
    buttonLabel: String,
    buttonIcon: ImageVector, // New Parameter
    onAction: () -> Unit
) {
    Surface(
        color = Color(0xFFF8F9FA),
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, fontSize = 14.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                Text(
                    subtitle,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isError) Color.Red else Color.Black
                )
            }

            Button(
                onClick = onAction,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.White,
                    contentColor = Color(0xFF6A1B9A)
                ),
                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 8.dp)
            ) {
                // ICON ADDED HERE ON THE LEFT
                Icon(
                    imageVector = buttonIcon,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(buttonLabel, fontSize = 14.sp, fontWeight = FontWeight.ExtraBold)
            }
        }
    }
}