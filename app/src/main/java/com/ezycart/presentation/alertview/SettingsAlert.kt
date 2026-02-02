package com.ezycart.presentation.alertview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ezycart.R

@Composable
fun AdminSettingsDialog(
    onDismiss: () -> Unit,
    onOpenTerminal: (String) -> Unit, // "Loadcell", "LED", "Printer"
    onTransferCart: (String) -> Unit,
    currentThreshold: Double,
    onThresholdChange: (Double) -> Unit
) {
    val trolleyList = remember { (1..25).map { "Trolley ${it.toString().padStart(2, '0')}" } }
    var selectedTrolley = remember { mutableStateOf("") }
    var thresholdValue = remember { mutableStateOf(currentThreshold) }
    var expanded = remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .fillMaxHeight(0.85f),
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFF8F9FF) // Light Slate Blue tint
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                // HEADER
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(painter = painterResource(id = R.drawable.ic_settings), contentDescription = null, tint = Color(0xFF6A1B9A), modifier = Modifier.size(32.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Admin Control Panel", style = MaterialTheme.typography.headlineMedium.copy(fontWeight = FontWeight.ExtraBold))
                    Spacer(modifier = Modifier.weight(1f))
                    IconButton(onClick = onDismiss) { Icon(Icons.Default.Close, contentDescription = null) }
                }

                Divider(modifier = Modifier.padding(vertical = 16.dp), color = Color.LightGray.copy(alpha = 0.5f))

                LazyColumn(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                    // SECTION 1: HARDWARE TERMINALS
                    item {
                        Text("Hardware Diagnostics", style = MaterialTheme.typography.titleMedium, color = Color.Gray)
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            TerminalCard(
                                label = "Loadcell",
                                iconResId = R.drawable.ic_scale, // Your drawable name
                                color = Color(0xFF43A047),
                                modifier = Modifier.weight(1f)
                            ) { onOpenTerminal("Loadcell") }

                            TerminalCard(
                                label = "LED",
                                iconResId = R.drawable.ic_led,      // Your drawable name
                                color = Color.Red,
                                modifier = Modifier.weight(1f)
                            ) { onOpenTerminal("LED") }

                            TerminalCard(
                                label = "Printer",
                                iconResId = R.drawable.ic_print,  // Your drawable name
                                color = Color(0xFF1E88E5),
                                modifier = Modifier.weight(1f)
                            ) { onOpenTerminal("Printer") }
                        }
                    }

                    // SECTION 2: CART TRANSFER (Dropdown + Action)
                    item {
                        AdminSectionCard(title = "Inventory Transfer") {
                            Column {
                                Text(
                                    text = "Select Target Trolley to Transfer Everything:",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = Color.DarkGray
                                )
                                Spacer(modifier = Modifier.height(12.dp))

                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // THE DROPDOWN SELECTOR
                                    Box(
                                        modifier = Modifier
                                            .weight(1f)
                                            .height(50.dp)
                                            .background(Color.White, RoundedCornerShape(8.dp))
                                            .border(1.0.dp, Color(0xFF6A1B9A).copy(alpha = 0.5f), RoundedCornerShape(8.dp))
                                            .clickable { expanded.value = true }
                                            .padding(horizontal = 12.dp),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                text = selectedTrolley.value.ifEmpty { "Select Trolley" },
                                                color = if (selectedTrolley.value.isEmpty()) Color.Gray else Color.Black,
                                                fontSize = 16.sp
                                            )
                                            Icon(
                                                imageVector = Icons.Default.ArrowDropDown,
                                                contentDescription = null,
                                                tint = Color(0xFF6A1B9A)
                                            )
                                        }

                                        // DROPDOWN MENU
                                        DropdownMenu(
                                            expanded = expanded.value,
                                            onDismissRequest = { expanded.value = false },
                                            modifier = Modifier
                                                .fillMaxWidth(0.5f) // Adjust width to fit screen
                                                .background(Color.White)
                                                .heightIn(max = 300.dp) // Scrollable after a certain height
                                        ) {
                                            trolleyList.forEach { trolley ->
                                                DropdownMenuItem(
                                                    text = {
                                                        Text(
                                                            text = trolley,
                                                            style = TextStyle(fontSize = 16.sp)
                                                        )
                                                    },
                                                    onClick = {
                                                        selectedTrolley.value = trolley
                                                        expanded.value = false
                                                    }
                                                )
                                            }
                                        }
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    // THE ACTION BUTTON
                                    Button(
                                        onClick = {
                                            if (selectedTrolley.value.isNotEmpty()) {
                                                onTransferCart(selectedTrolley.value)
                                            }
                                        },
                                        enabled = selectedTrolley.value.isNotEmpty(),
                                        modifier = Modifier.height(50.dp),
                                        shape = RoundedCornerShape(8.dp),
                                        colors = ButtonDefaults.buttonColors(
                                            containerColor = Color(0xFF6A1B9A),
                                            disabledContainerColor = Color.LightGray
                                        )
                                    ) {
                                        Text("TRANSFER", fontWeight = FontWeight.Bold)
                                    }
                                }
                            }
                        }
                    }

                    // SECTION 3: LOADCELL THRESHOLD (Slider)
                    item {
                        AdminSectionCard(title = "Loadcell Calibration") {
                            Column {
                                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                    Text("Sensitivity Threshold", style = MaterialTheme.typography.bodyMedium)
                                    Text("${thresholdValue.value.toInt()}g", fontWeight = FontWeight.Bold, color = Color(0xFF6A1B9A))
                                }
                                Slider(
                                    value = thresholdValue.value.toFloat(),
                                    onValueChange = { thresholdValue.value = it.toDouble() },
                                    onValueChangeFinished = { onThresholdChange(thresholdValue.value) },
                                    valueRange = 0f..500f,
                                    colors = SliderDefaults.colors(thumbColor = Color(0xFF6A1B9A), activeTrackColor = Color(0xFF6A1B9A))
                                )
                            }
                        }
                    }

                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}

@Composable
fun TerminalCard(
    label: String,
    iconResId: Int, // Changed to Int for Drawable ID
    color: Color,
    modifier: Modifier,
    onClick: () -> Unit
) {
    Card(
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp),
        onClick = onClick
    ) {
        Column(
            Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Use painterResource for custom drawables
            Icon(
                painter = painterResource(id = iconResId),
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = label,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color.Black
            )
        }
    }
}

@Composable
fun AdminSectionCard(title: String, content: @Composable () -> Unit) {

    Column {
        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.Gray)
        Spacer(modifier = Modifier.height(8.dp))
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}