package com.ezycart.presentation.landing

import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import com.ezycart.services.usb.LedSerialConnection
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.OutlinedButton
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.ezycart.AlertState
import com.ezycart.AlertType
import com.ezycart.CommonAlertView
import com.ezycart.presentation.UsbTerminalDialog
import com.ezycart.presentation.alertview.AdminSettingsDialog
import com.ezycart.presentation.common.data.Constants
import com.ezycart.presentation.home.HomeViewModel
import com.ezycart.services.usb.AppScenario
import com.ezycart.services.usb.BixolonUsbPrinter
import com.ezycart.services.usb.PrinterStatusDialog
import com.ezycart.services.usb.StatusActionRow
import com.ezycart.services.usb.WeightScaleManager

@Composable
fun LandingScreen(
    homeViewModel: HomeViewModel, viewModel: LandingViewModel = hiltViewModel(),
    goToHomeScreen: () -> Unit, reConnectLoadCell: () -> Unit
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()
    var showGuidelines = remember { mutableStateOf(false) }
    var showLedDialog = viewModel.openLedTerminalDialog.collectAsState()
    var openLoadCellTerminalDialog = viewModel.openLoadCellTerminalDialog.collectAsState()
    var openPrinterTerminalDialog = viewModel.openPrinterTerminalDialog.collectAsState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var continueShoppingDialog = remember { mutableStateOf(false) }
    var currentSystemAlert = remember { mutableStateOf<AlertState?>(null) }
    var canStartShopping = viewModel.canStartShopping.collectAsState()
    var showAdminDialog = remember { mutableStateOf(false) }
    var settingsOpenCounter = 0
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    if (continueShoppingDialog.value) {
        ContinueShoppingDialog(

            onMemberLogin = {
                showGuidelines.value = true
                continueShoppingDialog.value = false
                // Navigate to member login
            },
            onGuestLogin = {
                showGuidelines.value = true
                continueShoppingDialog.value = false
                // Continue without login
            },
            onDismiss = {
                continueShoppingDialog.value = false
            }
        )
    }

    if (canStartShopping.value) {
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Please, Place all your products in Tray 1",
            message = "",
            lottieFileName = "anim_warning_circle.json",
            type = AlertType.INFO,
            isDismissible = false,
            showButton = true,
            positiveButtonText = "Ok",
            onPositiveClick = {
                reConnectLoadCell()
                viewModel.clearSystemAlert()
            }
        )
    }
    if (openLoadCellTerminalDialog.value) {
        WeightScaleManager.initOnce(homeViewModel)
        WeightScaleManager.connectSafe(context)
        UsbTerminalDialog(
            onDismiss = { viewModel.activateLoadCellTerminal() },
            viewModel = homeViewModel,
        )
    }
    if (openPrinterTerminalDialog.value) {
        val printer = BixolonUsbPrinter(context.applicationContext)
        PrinterStatusDialog(printer, onDismiss = { viewModel.activatePrinterTerminal() })
    }
    if (showLedDialog.value) {
        LedControlDialog(onDismiss = { viewModel.activateLedTerminal() })
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
    if (showAdminDialog.value) {
        settingsOpenCounter = 0
        AdminSettingsDialog(
            onDismiss = { showAdminDialog.value = false },
            onOpenTerminal = { type ->
                // Logic to open specific terminal
                when (type) {
                    "Loadcell" -> {
                        viewModel.activateLoadCellTerminal()
                    }

                    "LED" -> {
                        viewModel.activateLedTerminal()
                    }

                    "Printer" -> {
                        viewModel.activatePrinterTerminal()
                    }
                }
                showAdminDialog.value = false
            },

            onTransferCart = { targetCart ->
                // viewModel.transferCurrentCartTo(targetCart)
                showAdminDialog.value = false
            },
            //currentThreshold = threshold,
            currentThreshold = viewModel.getWeightThreshold(),
            onThresholdChange = { newValue ->
                viewModel.updateThreshold(newValue)
            }
        )
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
    ) {

        // 1. Dynamic Content Area (Banner or Language Selection)
        Box(
            modifier = Modifier
                .weight(1f)
                .background(Color.White)
        ) {
            AnimatedContent(
                targetState = uiState.value.isStarted,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) }
            ) { started ->
                if (!started) {
                    // Show the Auto-Scroll Banner initially
                    AutoScrollingBanner(
                        banners = uiState.value.banners,
                        currentIndex = uiState.value.currentBannerIndex,
                        modifier = Modifier
                            .fillMaxSize()
                            .focusRequester(focusRequester)
                            .focusable(),
                        onImageClick = {
                            if (!showAdminDialog.value) {
                                settingsOpenCounter++
                                if (settingsOpenCounter > 5) {
                                    showAdminDialog.value = true
                                }
                            }

                        }
                    )
                } else {
                    // Replace with Language Selection after click
                    LanguageSelectionScreen(
                        viewModel, onLanguageSelected = { lang ->
                            Log.d("Kiosk", "Selected: $lang")
                            Constants.selectedLanguage = lang
                            continueShoppingDialog.value = true


                        },
                        onSettingsSelected = { showAdminDialog.value = true })
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
                        settingsOpenCounter = 0
                        if (homeViewModel.initialTotalWeight > 50) {
                            LedSerialConnection.setScenario(AppScenario.START_SHOPPING)
                            viewModel.onStartClicked()
                        } else {
                            /* LedSerialConnection.setScenario(AppScenario.START_SHOPPING)
                             viewModel.onStartClicked()*/
                            viewModel.setStartShopping(true)
                        }

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

    currentSystemAlert.value?.let { alert ->
        CommonAlertView(state = alert) {
            viewModel.clearSystemAlert()
            currentSystemAlert.value = null
        }
    }
}

@Composable
fun LanguageSelectionScreen(
    viewModel: LandingViewModel,
    onLanguageSelected: (String) -> Unit,
    onSettingsSelected: () -> Unit
) {
    var currentSystemAlert = remember { mutableStateOf<AlertState?>(null) }
    var canShowHelpDialog = viewModel.canShowHelpDialog.collectAsState()
    if (canShowHelpDialog.value) {
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Help is on the way",
            message = "Please wait for our Staff to assist you.",
            lottieFileName = "anim_help_support.json",
            type = AlertType.INFO,
            isDismissible = false,
            showButton = true,
            positiveButtonText = "Ok",
            onPositiveClick = { viewModel.clearSystemAlert() }
        )
    }

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
        BitesHeader(
            viewModel,
            onHelpClick = { viewModel.showHelpDialog() },
            onSettingsSelected = { onSettingsSelected() })

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp),
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

    currentSystemAlert.value?.let { alert ->
        CommonAlertView(state = alert) {
            currentSystemAlert.value = null
        }
    }
}

@Composable
fun LanguageButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.6f)
            .height(90.dp),
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
    viewModel: LandingViewModel,
    onHelpClick: () -> Unit,
    onSettingsSelected: () -> Unit
) {
    val focusRequester = remember { FocusRequester() }

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


        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A)) // Deep Purple
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center // This ensures the 'SELF CHECKOUT' is exactly in the middle
        ) {
            // 1. LEFT SIDE: Scan Button

            Box(
                modifier = Modifier
                    .align(Alignment.CenterStart) // Force to far left
                    .size(32.dp)
                    .clickable { onSettingsSelected() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "scan",
                    tint = Color.White,
                    modifier = Modifier.fillMaxSize()
                )
            }
            // 2. CENTER: Title
            Text(
                text = "SELF CHECKOUT",
                style = TextStyle(
                    color = Color.White,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                ),
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .focusable()
                    .clickable { }
            )

            // 3. RIGHT SIDE: Help Content
            /* Row(
                 modifier = Modifier
                     .align(Alignment.CenterEnd)
                     .clip(RoundedCornerShape(50))
                     .border(
                         width = 2.dp,
                         color = colorResource(R.color.white),
                         shape = RoundedCornerShape(50)
                     )
                     .clickable { onHelpClick() }
                     .padding(horizontal = 14.dp, vertical = 6.dp),
                 verticalAlignment = Alignment.CenterVertically
             ) {
                 Icon(
                     painter = painterResource(id = R.drawable.ic_help),
                     contentDescription = "Help",
                     tint = colorResource(R.color.white),
                     modifier = Modifier.size(35.dp)
                 )

                 Spacer(modifier = Modifier.width(6.dp))

                 Text(
                     text = "Help",
                     color = colorResource(R.color.white),
                     fontSize = 27.sp,
                     fontWeight = FontWeight.Bold
                 )
             }*/

        }


        /* // 2. Purple Action Bar (Self Checkout & Help)
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
         }*/
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
                    GuidelineImage(
                        resId = R.drawable.ic_guideline_1,
                        label = "Load all in Entry Basket "
                    )
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
                    "I Got It",
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


/*@Composable
fun LedControlDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    // State to keep track of 6 buttons
    val ledStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(28.dp),
            color = Color(0xFFF1F3F5),
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    "Output Controls",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold
                )

                Button(
                    onClick = { LedSerialConnection.connect(context) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp),
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
                                modifier = Modifier
                                    .padding(12.dp)
                                    .fillMaxWidth(),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Text("OUT ${index + 1}", fontSize = 12.sp, color = Color.Gray)
                                Spacer(modifier = Modifier.height(8.dp))
                                // Status Circle
                                Box(
                                    modifier = Modifier
                                        .size(20.dp)
                                        .background(
                                            if (isOn) Color(0xFF4CAF50) else Color(
                                                0xFFF44336
                                            ), CircleShape
                                        )
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    if (isOn) "ON" else "OFF",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            LedSerialConnection.setAll(true)
                            for (i in 0..5) ledStates[i] = true
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF4CAF50))
                    ) { Text("ALL ON") }

                    Button(
                        onClick = {
                            LedSerialConnection.setAll(false)
                            for (i in 0..5) ledStates[i] = false
                        },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF44336))
                    ) { Text("ALL OFF") }
                }
            }
        }
    }


}*/

@Composable
fun LedControlDialog(onDismiss: () -> Unit) {
    val context = LocalContext.current
    val ledStates = remember { mutableStateListOf(false, false, false, false, false, false) }

    val usbManager = context.getSystemService(Context.USB_SERVICE) as UsbManager
    val device = usbManager.deviceList.values.find { it.vendorId == 1240 && it.productId == 58 }
    val hasPermission = remember { mutableStateOf(device?.let { usbManager.hasPermission(it) } ?: false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Disabled to allow custom sizing
        )
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.95f) // Increased width for better grid spacing
                .wrapContentHeight()
                .padding(16.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 24.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Header Section
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .background(Color(0xFFE8F5E9), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_led),
                        contentDescription = null,
                        tint = Color(0xFF2E7D32),
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "LED Controller",
                    style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 2. Connection Card
                StatusActionRow(
                    title = "LED Controller Connection",
                    subtitle = if (hasPermission.value) "Device Authorized" else "Access Required",
                    isError = !hasPermission.value,
                    buttonLabel = if (hasPermission.value) "Reconnect" else "Authorize",
                    buttonIcon = if (hasPermission.value) Icons.Default.CheckCircle else Icons.Default.Warning,
                    onAction = {
                        LedSerialConnection.connect(context)
                        hasPermission.value = device?.let { usbManager.hasPermission(it) } ?: false
                    }
                )

                Spacer(modifier = Modifier.height(24.dp))

                // 3. Output Grid - Fixed Height Removed to prevent scrolling
                Text(
                    "Manual Output Control",
                    modifier = Modifier.align(Alignment.Start).padding(start = 8.dp, bottom = 12.dp),
                    style = TextStyle(fontSize = 15.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                )

                // No fixed height here = All items visible at once
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxWidth().heightIn(max = 1000.dp)
                ) {
                    items(6) { index ->
                        val isOn = ledStates[index]
                        OutputToggleCard(
                            label = "OUT ${index + 1}",
                            isOn = isOn,
                            onClick = {
                                val newState = !isOn
                                ledStates[index] = newState
                                LedSerialConnection.updateOutput(index, newState)
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // 4. Global Control Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = {
                            LedSerialConnection.setAll(true)
                            for (i in 0..5) ledStates[i] = true
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("ALL ON", fontWeight = FontWeight.Bold, color = Color.White) }

                    Button(
                        onClick = {
                            LedSerialConnection.setAll(false)
                            for (i in 0..5) ledStates[i] = false
                        },
                        modifier = Modifier.weight(1f).height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) { Text("ALL OFF", fontWeight = FontWeight.Bold, color = Color.White) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Close Button (Changed from White to a prominent Subtle Gray)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFF1F3F5),
                        contentColor = Color(0xFF495057)
                    )
                ) {
                    Text("CLOSE CONTROLS", fontWeight = FontWeight.ExtraBold, letterSpacing = 1.sp)
                }
            }
        }
    }
}

@Composable
fun OutputToggleCard(label: String, isOn: Boolean, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isOn) Color(0xFFE8F5E9) else Color(0xFFF8F9FA)
        ),
        border = BorderStroke(1.dp, if (isOn) Color(0xFF2E7D32) else Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier.padding(10.dp).fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = if (isOn) Color(0xFF2E7D32) else Color.Gray)
            Spacer(modifier = Modifier.height(6.dp))
            Box(
                modifier = Modifier
                    .size(14.dp)
                    .background(if (isOn) Color(0xFF4CAF50) else Color(0xFFBDBDBD), CircleShape)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(if (isOn) "ACTIVE" else "INACTIVE", fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
fun ContinueShoppingDialog(
    onMemberLogin: () -> Unit,
    onGuestLogin: () -> Unit,
    onDismiss: () -> Unit
) {


    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            shape = RoundedCornerShape(20.dp),
            elevation = CardDefaults.cardElevation(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {

                // 🛒 Icon
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(55.dp)
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Title
                Text(
                    text = "Continue Shopping",
                    fontSize = 25.sp,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Message
                Text(
                    text = "Choose how you’d like to proceed",
                    fontSize = 20.sp,
                    color = Color.Black,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(24.dp))

                // ⭐ Member Login Button
                Button(
                    onClick = {
                        onMemberLogin()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Member Login",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                // 🛍 Guest Button
                OutlinedButton(
                    onClick = {
                        onGuestLogin()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.Person, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = "Continue as Guest",
                        fontSize = 16.sp
                    )
                }
                Spacer(modifier = Modifier.height(20.dp))
            }
        }
    }
}