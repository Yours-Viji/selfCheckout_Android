package com.ezycart.presentation.landing

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ezycart.R
import com.ezycart.services.usb.com.LedSerialConnection
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.platform.LocalContext

@Composable
fun LandingScreen(viewModel: LandingViewModel = viewModel(),
                  goToHomeScreen: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var showGuidelines = remember { mutableStateOf(false) }
    var showLedDialog = remember { mutableStateOf(false) }

    if (showLedDialog.value) {
        LedControlDialog(onDismiss = { showLedDialog.value = false })
    }
    if (showGuidelines.value) {
        GuidelinesDialog(
            onDismiss = { showGuidelines.value = false },
            onConfirm = {
                showGuidelines.value = false
                goToHomeScreen()
            }
        )
    }
    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. Dynamic Content Area (Banner or Language Selection)
        Box(modifier = Modifier.weight(1f).background(Color.White)) {
            AnimatedContent(
                targetState = uiState.value.isStarted,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) }
            ) { started ->
                if (!started) {
                    // Show the Auto-Scroll Banner initially
                    AutoScrollingBanner(
                        banners = uiState.value.banners,
                        currentIndex = uiState.value.currentBannerIndex,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Replace with Language Selection after click
                    LanguageSelectionScreen(onLanguageSelected = { lang ->
                        Log.d("Kiosk", "Selected: $lang")
                        if(lang == "中文"){
                            showLedDialog.value = true
                        }else{
                            showGuidelines.value = true
                        }


                        // Proceed to next step
                    })
                }
            }
        }

        // 2. Fixed Black Footer
        if (!uiState.value.isStarted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black)
                    // ADD THIS: It pushes the content up away from the system navigation buttons
                    .navigationBarsPadding()
                    .clickable(enabled = !uiState.value.isStarted) {
                        viewModel.onStartClicked()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TOUCH TO START",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        ),
                        lineHeight = 36.sp
                    ),
                    modifier = Modifier.wrapContentSize() // Only takes space needed
                )
            }
        }
    }
}

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE5D9F2), // Light lavender at top
                        Color(0xFFF3F0F7), // Very light grey-white in middle
                        Color(0xFFE0D7EA)  // Soft purple at bottom
                    )
                )
            )
    ) {
        // Decorative background illustrations (place in corners)
       /* Image(
            painter = painterResource(id = R.drawable.veg_illustration),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Inside,
            alpha = 0.6f
        )*/
        BitesHeader(onHelpClick = { /* Show Help Dialog */ })

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Please select your preferred language",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                val languages = listOf("Bahasa Malaysia", "中文", "English", "日本語", "한국인")
                languages.forEach { lang ->
                    LanguageButton(text = lang) { onLanguageSelected(lang) }
                    Spacer(modifier = Modifier.height(24.dp))
                }

        }
    }
}

@Composable
fun LanguageButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.6f).height(90.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, color = Color.Black, fontSize = 24.sp)
    }
}

@Composable
fun GlassyKioskBackground(
    content: @Composable ColumnScope.() -> Unit
) {
    // 1. Main Gradient Layer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE5D9F2), // Light lavender at top
                        Color(0xFFF3F0F7), // Very light grey-white in middle
                        Color(0xFFE0D7EA)  // Soft purple at bottom
                    )
                )
            )
    ) {
        // 2. The Glassy Card Container
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Margin around the main app area
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.6f) // Semi-transparent "Glass"
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) // Soft highlight edge
        ) {
            /*// Apply a slight blur effect if on Android 12+ (optional)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            ) {

            }*/
        }
    }
}
@Composable
fun BitesHeader(

    onHelpClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
    ) {
        // 1. Top White Brand Bar (Logo Section)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color.White)

        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(100.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(20.dp))
                Image(
                    painter = painterResource(id = R.drawable.ic_bites_logo),
                    contentDescription = "Bites Logo",
                    modifier = Modifier
                        .height(70.dp),

                    contentScale = ContentScale.Fit
                )
            }
        }

        // 2. Purple Action Bar (Self Checkout & Help)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A)) // Deep Purple
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Invisible spacer to keep "SELF CHECKOUT" perfectly centered
            Box(modifier = Modifier.size(40.dp))

            Text(
                text = "SELF CHECKOUT",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            )

            // Help Button Section
            Row(
                modifier = Modifier
                    .clickable { onHelpClick() }
                    .padding(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Help",
                    tint = Color.White,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Help",
                    color = Color.White,
                    fontSize = 16.sp
                )

            }
        }
    }
}

@Composable
fun GuidelinesDialog(
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(16.dp),
        containerColor = Color.White,
        title = {
            Text(
                text = "Guidelines to follow",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        text = {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    text = "Please follow these instructions for a smooth checkout.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // The three rounded corner images in a row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    GuidelineImage(resId = R.drawable.ic_guideline_1, label = "Load all in Entry Basket ")
                    GuidelineImage(resId = R.drawable.ic_guideline_2, label = "Scan a Product")
                    GuidelineImage(resId = R.drawable.ic_guideline_3, label = "Drop in Exit Basket")
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                modifier = Modifier.padding(8.dp)
            ) {
                Text(
                    "I Understand",
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 18.sp,
                    color = Color(0xFF007BFF) // Kiosk Blue
                )
            }
        }
    )
}

@Composable
fun GuidelineImage(resId: Int, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = label,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(110.dp)
                .clip(RoundedCornerShape(12.dp)) // Rounded corners for images
                .background(Color.LightGray)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = label, style = MaterialTheme.typography.bodyMedium)
    }
}


@Composable
fun LedControlDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    // State to keep track of 6 buttons
    val ledStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFF1F3F5),
            modifier = Modifier.fillMaxWidth().padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text("Output Controls", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)

                Button(
                    onClick = { LedSerialConnection.connect(context) },
                    modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Black)
                ) { Text("CONNECT DEVICE") }

                // Grid for the 6 outputs
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.height(220.dp)
                ) {
                    items(6) { index ->
                        val isOn = ledStates[index]
                        Card(
                            onClick = {
                                val newState = !isOn
                                ledStates[index] = newState
                                LedSerialConnection.updateOutput(index, newState)
                            },
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.White),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp).fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("OUT ${index + 1}", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Status Circle
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(if (isOn) Color(0xFF4CAF50) else Color(0xFFF44336), CircleShape)
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(if (isOn) "ON" else "OFF", fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    Button(
                        onClick = {
                            LedSerialConnection.setAll(true)
                            for(i in 0..5) ledStates[i] = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("ALL ON") }

                    Button(
                        onClick = {
                            LedSerialConnection.setAll(false)
                            for(i in 0..5) ledStates[i] = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) { Text("ALL OFF") }
                }
            }
        }
    }
}