package com.ezycart.presentation.landing

import android.app.Activity
import android.content.Context
import android.hardware.usb.UsbManager
import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
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
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
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
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
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
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import java.util.Locale
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.FloatingActionButton
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.Dp
import com.ezycart.presentation.payment.TerminalDebugDialog
import kotlinx.coroutines.delay
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter

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
    var openPaymentTerminalDialog = viewModel.openPaymentTerminalDialog.collectAsState()
    val context = LocalContext.current
    val focusRequester = remember { FocusRequester() }
    var continueShoppingDialog = remember { mutableStateOf(false) }
    var currentSystemAlert = remember { mutableStateOf<AlertState?>(null) }
    var canStartShopping = viewModel.canStartShopping.collectAsState()
    var showAdminDialog = remember { mutableStateOf(false) }
    var canShowMemberDialog = viewModel.canShowMemberDialog.collectAsState()
    var isMemberLoginSuccess= viewModel.isMemberLoginSuccess.collectAsState()
    var scanBuffer = remember { mutableStateOf("") }
    val errorMessage = viewModel.errorMessage.collectAsState()

    var settingsOpenCounter = 0
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }
    LaunchedEffect(errorMessage.value) {
        errorMessage.value.let { message ->
            DynamicToast.makeError(context, message).show()
        }
    }
    fun updateAppLanguage(context: Context, languageCode: String) {
        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = context.resources.configuration
        config.setLocale(locale)

        context.resources.updateConfiguration(
            config,
            context.resources.displayMetrics
        )
    }
    fun languageToCode(language: String): String {
        return when (language) {
            "English" -> "en"
            "Bahasa Malaysia" -> "ms"
            "中文" -> "zh"
            "日本語" -> "ja"
            "한국인" -> "ko"
            else -> "en"
        }
    }
    if (isMemberLoginSuccess.value){
        viewModel.hideAdminSettings()
        showGuidelines.value = true
        continueShoppingDialog.value = false
    }
    if (canShowMemberDialog.value) {
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = stringResource(R.string.member_login),
            message = stringResource(R.string.scan_your_member_code),
            lottieFileName = "anim_scanner.json",
            type = AlertType.INFO,
            isDismissible = false,
            showButton = true
        )
        //viewModel.memberLogin("88001962")
    }
    if (continueShoppingDialog.value) {
        ContinueShoppingDialog(

            onMemberLogin = {
                viewModel.showMemberDialog()
                // Navigate to member login
            },
            onGuestLogin = {
                viewModel.clearMemberData()
                viewModel.hideAdminSettings()
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
            title = stringResource(R.string.please_place_all_your_products_in_tray_1),
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
    if (openPaymentTerminalDialog.value){
        TerminalDebugDialog(
            onDismiss = { viewModel.activatePaymentTerminal() },
            terminalIp = Constants.PAYMENT_TERMINAL_IP,
            terminalPort = Constants.PAYMENT_TERMINAL_PORT
        )
    }
    if (showLedDialog.value) {
        LedControlDialog(onDismiss = { viewModel.activateLedTerminal() })
    }
    if (showGuidelines.value) {
        GuidelinesDialog(
            onDismiss = { showGuidelines.value = false },
            onConfirm = {
                showGuidelines.value = false
                viewModel.clearAdminSettings()
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
                    "Payment" -> {
                        viewModel.activatePaymentTerminal()
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
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { keyEvent ->
                // We handle KeyDown to capture characters as they are typed
                if (keyEvent.type == androidx.compose.ui.input.key.KeyEventType.KeyDown) {
                    val nativeEvent = keyEvent.nativeKeyEvent

                    when (keyEvent.key) {
                        androidx.compose.ui.input.key.Key.Enter,
                        androidx.compose.ui.input.key.Key.NumPadEnter -> {
                            if (scanBuffer.value.isNotBlank()) {
                                val re = Regex("[^a-zA-Z0-9]")
                                var barCode =scanBuffer.value.trim()
                                val isQR = viewModel.isProbablyQRCode(barCode)
                                val containsEmp = barCode.lowercase().contains(":")

                                viewModel.setErrorMessage("Scanned Code - $barCode")

                                when {
                                    // This now works because getUnicodeChar captured the ':' correctly
                                    isQR && containsEmp -> {
                                        val pinList = barCode.split(":")
                                        if (pinList.size > 1) {
                                            val empPin = re.replace(pinList[1], "")
                                            viewModel.setErrorMessage("employee API Call - $empPin")
                                            viewModel.employeeLogin(empPin)
                                        }
                                    }

                                    !isQR -> {
                                        if (canShowMemberDialog.value) {
                                            viewModel.clearSystemAlert()
                                            viewModel.memberLogin(barCode)
                                        }
                                    }

                                    else -> {
                                        // Logic for "Alert to scan barcode and hide qr code"
                                    }
                                }
                                scanBuffer.value = ""
                            }
                            return@onKeyEvent true
                        }
                        // Ignore standalone modifier keys so they don't add null chars to buffer
                        androidx.compose.ui.input.key.Key.ShiftLeft,
                        androidx.compose.ui.input.key.Key.ShiftRight,
                        androidx.compose.ui.input.key.Key.CapsLock -> return@onKeyEvent false

                        else -> {
                            // IMPORTANT: getUnicodeChar handles the Shift modifier for you
                            // It converts (Shift + ;) into (:) automatically
                            val unicodeChar = nativeEvent.getUnicodeChar(nativeEvent.metaState)

                            if (unicodeChar != 0) {
                                val c = unicodeChar.toChar()
                                // Accept printable characters
                                if (!c.isISOControl()) {
                                    scanBuffer.value += c
                                }
                            }
                            return@onKeyEvent false
                        }
                    }
                }
                false
            }
    ) {
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


                            val activity = context as Activity

                            //val langCode = languageToCode(lang)
                            updateAppLanguage(activity, lang)
                            //activity.recreate()

                            //delay(100)
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
                        if (Constants.isAdminLogin || homeViewModel.initialTotalWeight > 50) {
                            LedSerialConnection.setScenario(AppScenario.START_SHOPPING)
                            viewModel.onStartClicked()
                        } else {
                           //viewModel.employeeLogin("15532")
                            LedSerialConnection.setScenario(AppScenario.START_SHOPPING)
                            viewModel.onStartClicked()

                            //viewModel.setStartShopping(true)
                          //  viewModel.onQrPayClicked("20.00")
                        }

                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.touch_to_start),
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

    currentSystemAlert.value?.let { alert ->
        CommonAlertView(state = alert) {
            viewModel.clearSystemAlert()
            currentSystemAlert.value = null
        }
    }


}
@Composable
fun LiveClock() {
    // State to hold the formatted string
    var currentDateTime = remember {
        mutableStateOf(LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy | hh:mm:ss a")))
    }

    LaunchedEffect(Unit) {
        while (true) {
            // Updated pattern: EEEE (Day), dd MMM (Date), hh:mm:ss a (12hr time with AM/PM)
            currentDateTime.value = LocalDateTime.now().format(DateTimeFormatter.ofPattern("EEEE, dd MMM yyyy | hh:mm:ss a"))
            delay(1000)
        }
    }

    Text(
        text = currentDateTime.value,
        style = TextStyle(
            fontSize = 24.sp, // Adjusted size to fit the longer date string
            fontWeight = FontWeight.Bold,
            color = Color(0xFF2D3436),
            textAlign = TextAlign.Center
        ),
        modifier = Modifier
            .fillMaxWidth() // Essential for centering
            .padding(vertical = 16.dp)
    )
}

@Composable
fun LanguageSelectionScreen(
    viewModel: LandingViewModel,
    onLanguageSelected: (String) -> Unit,
    onSettingsSelected: () -> Unit
) {
    var currentSystemAlert = remember { mutableStateOf<AlertState?>(null) }
    var canShowHelpDialog = viewModel.canShowHelpDialog.collectAsState()
    val canViewAdminSettings = viewModel.canViewAdminSettings.collectAsState()
    val languages = listOf(
        LanguageItem("English", "en", "🇺🇸"),
        LanguageItem("Bahasa Malaysia", "ms", "🇲🇾"),
        LanguageItem("中文", "zh", "🇨🇳"),
        LanguageItem("日本語", "ja", "🇯🇵"),
        LanguageItem("한국어", "ko", "🇰🇷")
    )

    if (canShowHelpDialog.value) {
        currentSystemAlert.value = AlertState(
            title = stringResource(R.string.help_is_on_the_way),
            message = stringResource(R.string.please_wait_for_our_staff_to_assist_you),
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
            // Using the circle color as the main solid background
            .background(Color(0xFF6A1B9A).copy(alpha = 0.05f))
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            BitesHeader(
                viewModel,
                onHelpClick = { viewModel.showHelpDialog() },
                onSettingsSelected = { onSettingsSelected() }
            )
            LiveClock()
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 40.dp)
                    .verticalScroll(rememberScrollState()), // Allows scrolling if list gets long
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.please_select_your_preferred_language),
                    style = TextStyle(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF2D3436)
                    ),
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                // Vertical list of buttons
                languages.forEach { lang ->
                    LanguageRowCard(lang) {
                        onLanguageSelected(lang.code)
                    }
                    Spacer(modifier = Modifier.height(20.dp))
                }
            }
        }
        if (canViewAdminSettings.value){
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .padding(100.dp)
                    .align(Alignment.BottomEnd)
            ) {

                LogoutFab(onLogoutClick = {
                    viewModel.hideAdminSettings()
                    viewModel.clearAdminSettings()
                })

            }
        }


    }

    currentSystemAlert.value?.let { alert ->
        CommonAlertView(state = alert) { currentSystemAlert.value = null }
    }
}
@Composable
fun LogoutFab(onLogoutClick: () -> Unit) {
    // 1. Create the Infinite Transition
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    // 2. Define the scale animation (zoom in and out)
    val scale = infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.15f, // Zooms in by 15%
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse // Zooms back out
        ),
        label = "scale"
    )

    FloatingActionButton(
        onClick = onLogoutClick,
        containerColor = Color(0xFFD35443),
        shape = CircleShape, // Makes it a perfect circle
        modifier = Modifier
            .size(65.dp) // Set a fixed size for a consistent circle
            .graphicsLayer(scaleX = scale.value, scaleY = scale.value) // Applies the zoom
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_logout),
            contentDescription = "Logout",
            modifier = Modifier.size(30.dp),
            tint = Color.White
        )
    }
}
@Composable
fun LanguageRowCard(language: LanguageItem, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth(0.7f) // Wide enough for a kiosk but not touching edges
            .height(100.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        shadowElevation = 6.dp,
        border = BorderStroke(1.dp, Color(0xFFEDEDED))
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Start
        ) {
            // Circle Flag container
            Box(
                modifier = Modifier
                    .size(60.dp)
                    .background(Color(0xFFF8F9FA), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(language.flag, fontSize = 36.sp)
            }

            Spacer(modifier = Modifier.width(24.dp))

            Text(
                text = language.name,
                style = TextStyle(
                    fontSize = 26.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D3436)
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // Subtle arrow to indicate action
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.LightGray,
                modifier = Modifier.size(32.dp)
            )
        }
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
    val canViewAdminSettings = viewModel.canViewAdminSettings.collectAsState()
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
            if (canViewAdminSettings.value) {
                Box(
                    modifier = Modifier
                        .align(Alignment.CenterStart) // Force to far left
                        .size(32.dp)
                        .clickable { onSettingsSelected() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "settings",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(20.dp))
            }
            // 2. CENTER: Title
            Text(
                text = stringResource(R.string.self_checkout),
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
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.9f) // Slightly wider for 22" screen
                .wrapContentHeight(),
            shape = RoundedCornerShape(28.dp),
            color = Color.White
        ) {
            Column(
                modifier = Modifier.padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.guidelines_to_follow),
                    style = TextStyle(
                        fontSize = 40.sp,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                )

                Text(
                    text = stringResource(R.string.please_follow_these_instructions_for_a_smooth_checkout),
                    style = TextStyle(fontSize = 22.sp, color = Color.Gray),
                    modifier = Modifier.padding(top = 12.dp, bottom = 40.dp),
                    textAlign = TextAlign.Center
                )

                // The Fix: Using a Row with weighted children
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp) // Gap between items
                ) {
                    GuidelineItem(
                        modifier = Modifier.weight(1f),
                        resId = R.drawable.ic_guideline_1,
                        label = stringResource(R.string.load_all_in_entry_basket)
                    )
                    GuidelineItem(
                        modifier = Modifier.weight(1f),
                        resId = R.drawable.ic_guideline_2,
                        label = stringResource(R.string.scan_a_product)
                    )
                    GuidelineItem(
                        modifier = Modifier.weight(1f),
                        resId = R.drawable.ic_guideline_3,
                        label = stringResource(R.string.drop_in_exit_basket)
                    )
                }

                Spacer(modifier = Modifier.height(48.dp))

                Button(
                    onClick = onConfirm,
                    modifier = Modifier
                        .fillMaxWidth(0.35f)
                        .height(70.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF3B82F6))
                ) {
                    Text("Got It", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun GuidelineItem(modifier: Modifier = Modifier, resId: Int, label: String) {
    Column(
        modifier = modifier, // Uses the weight from the Row
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = resId),
            contentDescription = null,
            contentScale = ContentScale.FillWidth, // Adapts to the weight width
            modifier = Modifier
                .fillMaxWidth() // Fills its 1/3rd of the Row
                .aspectRatio(1f) // Keeps it square
                .clip(RoundedCornerShape(16.dp))
                .background(Color(0xFFF8FAFC))
        )
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = label,
            style = TextStyle(
                fontSize = 18.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            maxLines = 3, // Prevents text from pushing things off-screen
            lineHeight = 22.sp
        )
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
                    modifier = Modifier
                        .align(Alignment.Start)
                        .padding(start = 8.dp, bottom = 12.dp),
                    style = TextStyle(fontSize = 15.sp, color = Color.Gray, fontWeight = FontWeight.Bold)
                )

                // No fixed height here = All items visible at once
                LazyVerticalGrid(
                    columns = GridCells.Fixed(3),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 1000.dp)
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
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2E7D32))
                    ) { Text("ALL ON", fontWeight = FontWeight.Bold, color = Color.White) }

                    Button(
                        onClick = {
                            LedSerialConnection.setAll(false)
                            for (i in 0..5) ledStates[i] = false
                        },
                        modifier = Modifier
                            .weight(1f)
                            .height(56.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFC62828))
                    ) { Text("ALL OFF", fontWeight = FontWeight.Bold, color = Color.White) }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // 5. Close Button (Changed from White to a prominent Subtle Gray)
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
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
            modifier = Modifier
                .padding(10.dp)
                .fillMaxWidth(),
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
    Dialog(
        onDismissRequest = onDismiss,
        // Critical for 22-inch screens to allow the dialog to expand
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.75f) // Large width for kiosk visibility
                .wrapContentHeight()
                .padding(24.dp),
            shape = RoundedCornerShape(32.dp),
            color = Color.White,
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(48.dp), // Spacious internal padding
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 🛒 Premium Icon Container
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .background(
                            color = Color(0xFF6A1B9A).copy(alpha = 0.1f),
                            shape = CircleShape
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_shopping_bag),
                        contentDescription = null,
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(75.dp)
                    )
                }

                Spacer(modifier = Modifier.height(32.dp))

                // Title - Scaled Up
                Text(
                    text = stringResource(R.string.continue_shopping),
                    style = TextStyle(
                        fontSize = 42.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-1).sp
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Message - Scaled Up
                Text(
                    text = stringResource(R.string.choose_how_you_d_like_to_proceed),
                    style = TextStyle(
                        fontSize = 24.sp,
                        color = Color.Gray,
                        textAlign = TextAlign.Center
                    )
                )

                Spacer(modifier = Modifier.height(56.dp))

                // ⭐ Member Login Button - High Contrast
                Button(
                    onClick = {
                        onMemberLogin()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f)  // Not stretching across the whole huge screen
                        .height(90.dp), // Massive hit target for easy touch
                    shape = RoundedCornerShape(18.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                ) {
                    Icon(painter = painterResource(id = R.drawable.ic_member_icon), contentDescription = null, modifier = Modifier.size(35.dp))
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.member_login),
                        fontSize = 26.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                // 🛍 Guest Button - Clean Outlined Look
                OutlinedButton(
                    onClick = {
                        onGuestLogin()
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth(0.9f) // Increase slightly to give text more horizontal room
                        .height(90.dp),
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(2.dp, Color(0xFF6A1B9A)),
                    // REDUCE internal padding so text has more room before wrapping
                    contentPadding = PaddingValues(horizontal = 8.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_basket_1),
                        contentDescription = null,
                        tint = Color(0xFF6A1B9A),
                        modifier = Modifier.size(35.dp)
                    )
                    Spacer(Modifier.width(12.dp))
                    Text(
                        text = stringResource(R.string.continue_as_guest),
                        fontSize = 26.sp,
                        color = Color(0xFF6A1B9A),
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1,          // Force single line
                        softWrap = false       // Prevent automatic wrapping
                    )
                }
            }
        }
    }
}
data class LanguageItem(val name: String, val code: String, val flag: String)