package com.ezycart.presentation.home

import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.pm.ActivityInfo
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.LocalOnBackPressedDispatcherOwner
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ExperimentalLensFacing
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.MediaStoreOutputOptions
import androidx.camera.video.Quality
import androidx.camera.video.QualitySelector
import androidx.camera.video.Recorder
import androidx.camera.video.Recording
import androidx.camera.video.VideoCapture
import androidx.camera.video.VideoRecordEvent
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.focusTarget
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.layout
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ezycart.AlertState
import com.ezycart.AlertType
import com.ezycart.CommonAlertView
import com.ezycart.R
import com.ezycart.data.remote.dto.CartItem
import com.ezycart.data.remote.dto.ShoppingCartDetails
import com.ezycart.domain.model.AppMode
import com.ezycart.payment.nearpay.NearPaymentListener
import com.ezycart.presentation.UsbTerminalDialog
import com.ezycart.presentation.activation.LockScreenOrientation
import com.ezycart.presentation.alertview.AdminSettingsDialog
import com.ezycart.presentation.common.data.Constants
import com.ezycart.presentation.landing.LedControlDialog
import com.ezycart.presentation.payment.BitesPaymentDialog
import com.ezycart.services.usb.AppScenario
import com.ezycart.services.usb.LedSerialConnection

import com.ezycart.services.usb.WeightScaleManager
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import io.nearpay.sdk.utils.enums.TransactionData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("SuspiciousIndentation")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    //sensorSerialPortViewModel: SensorSerialPortViewModel = hiltViewModel(),
    onThemeChange: () -> Unit,
    onPaymentInitialize: () -> Unit,
    makeNearPayment: (String, String, NearPaymentListener?) -> Unit,
    onLogout: () -> Unit,
    goToPaymentScreen: () -> Unit,
    onTransactionCalled: () -> Unit
) {
    // val loadCellState by sensorSerialPortViewModel.usbData.collectAsStateWithLifecycle()
    //val weightData by sensorSerialPortViewModel.connectionLog.collectAsStateWithLifecycle()
    // val weightState by sensorSerialPortViewModel.weightState.collectAsStateWithLifecycle()


    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val showDialog = remember { mutableStateOf(false) }
    val cartCount = viewModel.cartCount.collectAsState()
    val employeeName = viewModel.employeeName.collectAsState()
    val errorMessage = viewModel.errorMessage.collectAsState()
    val priceInfo by viewModel.priceDetails.collectAsState()
    val productInfo by viewModel.productInfo.collectAsState()
    val shoppingCartInfo = viewModel.shoppingCartInfo.collectAsState()
    val canShowPriceChecker = viewModel.canShowPriceChecker.collectAsState()
    val drawerState = rememberDrawerState(DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    // var canShowPriceChecker = remember { mutableStateOf(true) }
    val appMode by viewModel.appMode.collectAsState()
    var scanBuffer = remember { mutableStateOf("") }
    // Correct way to declare the state
    val wavPayQrPaymentUrl = viewModel.wavPayQrPaymentUrl.collectAsState()
    var showQrDialog = viewModel.canShowQrPaymentDialog.collectAsState()
    var showPaymentSuccessDialog = viewModel.canShowPaymentSuccessDialog.collectAsState()
    var showPaymentErrorDialog = viewModel.canShowPaymentErrorDialog.collectAsState()
    var canMakePayment = viewModel.canMakePayment.collectAsState()
    var proceedTapToPay = remember { mutableStateOf(false) }
    val focusRequester = remember { FocusRequester() }
    val showErrorMessage = remember { mutableStateOf("") }
    val showWalletScanner = remember { mutableStateOf(false) }
    var clearTransAction = remember { mutableStateOf(false) }
    var showMainLogs = remember { mutableStateOf(false) }
    val context = LocalContext.current
    var currentSystemAlert = remember { mutableStateOf<AlertState?>(null)}
    var canShowProductNotScannedDialog = viewModel.canShowProductNotScannedDialog.collectAsState()
    var canShowProductMismatchDialog = viewModel.canShowProductMismatchDialog.collectAsState()
    var canShowProductNotFoundDialog = viewModel.canShowProductNotFoundDialog.collectAsState()
    var canShowValidationErrorDialog = viewModel.canShowValidationErrorDialog.collectAsState()
    var canShowPaymentProcess = viewModel.canShowPaymentProcessDialog.collectAsState()
    var canShowPrintReceipt = viewModel.canShowPrintReceiptDialog.collectAsState()
    var resetAndGoBack = viewModel.resetAndGoBack.collectAsState()
    var clearSystemAlert = viewModel.clearSystemAlert.collectAsState()
    /* val commonListener = remember {
         LoginWeightScaleSerialPort.createCommonListener(viewModel)
     }*/
    val loadCellValidationLog = viewModel.loadCellValidationLog.collectAsState()

    var showLedDialog = viewModel.openLedTerminalDialog.collectAsState()
    var openLoadCellTerminalDialog = viewModel.openLoadCellTerminalDialog.collectAsState()
    if (openLoadCellTerminalDialog.value){
        WeightScaleManager.initOnce(viewModel)
        WeightScaleManager.connectSafe(context)
        UsbTerminalDialog(
            onDismiss = { viewModel.activateLoadCellTerminal() },
            viewModel = viewModel,
        )
    }

    if (showLedDialog.value) {
        LedControlDialog(onDismiss = { viewModel.activateLedTerminal() })
    }

    LockScreenOrientation(context, ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
if (resetAndGoBack.value){
    currentSystemAlert.value = null
    viewModel.resetProductInfoDetails()
    viewModel.resetLoadCell()
    viewModel.clearCartDetails()
    onLogout()
}
    if (clearSystemAlert.value){
        currentSystemAlert.value = null
    }
    if(canShowProductNotFoundDialog.value) {
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Product Not Found",
            message = "Please try again or Call for Help.",
            lottieFileName = "anim_warning_circle.json",
            type = AlertType.INFO,
            isDismissible = true
        )
    }
    if (canShowProductNotScannedDialog.value){
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Product Not Scanned",
            message = "Please remove and scan the product.",
            lottieFileName = "anim_wrong.json",
            type = AlertType.WARNING,
            isDismissible = false
        )
    }
    if (canShowProductMismatchDialog.value){
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Product Mismatch",
            message = "Please remove, scan and add the correct product.",
            lottieFileName = "anim_wrong.json",
            type = AlertType.ERROR,
            isDismissible = false
        )
    }
    if(canShowValidationErrorDialog.value) {
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Validation Error",
            message = "Please Call for Help.",
            lottieFileName = "anim_wrong.json",
            type = AlertType.ERROR,
            isDismissible = false
        )
    }
    if(canShowPaymentProcess.value) {
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Please Tap Your  Credit/Debit Card on Terminal",
            message = "To be paid RM ${viewModel.getFormatedFinalAmount()}",
            lottieFileName = "anim_payment_3.json",
            type = AlertType.SUCCESS,
            isDismissible = false
        )
    }

    if(canShowPrintReceipt.value) {
        viewModel.hidePaymentView()
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Thank You for Shopping with us.",
            message = "Please remember to take your receipt",
            lottieFileName = "anim_print_receipt.json",
            type = AlertType.SUCCESS,
            isDismissible = false
        )
        viewModel.onPaymentSuccess("https://api-retailetics-ops-mini-03.retailetics.com/ezyCart/invoice/000VGO-P9900003312",context as Activity)
    }

    LaunchedEffect(state.error) {
        state.error?.let { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                DynamicToast.makeError(context, errorMessage).show()
                viewModel.clearError()
            }
        }
    }

    LaunchedEffect(errorMessage.value) {
        errorMessage.value.let { message ->
            DynamicToast.makeError(context, message).show()
            viewModel.clearError()
        }
    }

    /*LaunchedEffect(weightState) {
       // viewModel.handleWeightUpdate(weightState)
    }*/

    LaunchedEffect(state.isReadyToInitializePaymentSdk) {
        onPaymentInitialize()
    }
    LaunchedEffect(priceInfo) {
        if (priceInfo != null) {
            // showDialog.value = true
        }
    }
    if (clearTransAction.value) {
        CancelConfirmationDialog(
            onConfirm = {
                viewModel.clearCartDetails()
                clearTransAction.value = false
                onLogout()
            },
            onDismiss = { clearTransAction.value = false }
        )
    }
    if (showErrorMessage.value.isNotEmpty()) {
        DynamicToast.makeError(context, showErrorMessage.value).show()
    }
    /* LaunchedEffect(Unit) {

         try {
             LoginWeightScaleSerialPort.connectScale(context, commonListener)
         } catch (e: Exception) {
             TODO("Not yet implemented")
         }

     }*/
    /*if (showTerminal.value) {

        WeightScaleManager.initOnce(viewModel)
        WeightScaleManager.connectSafe(context)
        UsbTerminalDialog(
            onDismiss = { showTerminal.value = false },
            viewModel = viewModel,
        )
    }*/
    if (proceedTapToPay.value) {
        shoppingCartInfo.value.let {
            val finalAmount = it?.finalAmount ?: 0.0
            makeNearPayment("12345", "$finalAmount", object : NearPaymentListener {
                override fun onPaymentSuccess(transactionData: TransactionData) {
                    viewModel.makePayment(2)
                }

                override fun onPaymentFailed(error: String) {
                    showErrorMessage.value = error
                    // Toast.makeText(this, "Payment failed: $error", Toast.LENGTH_SHORT).show()
                }
            })
            proceedTapToPay.value = false
        }
    }
    if (showQrDialog.value) {
        shoppingCartInfo.value.let {
            val finalAmount = it?.finalAmount ?: 0.0
            QRPaymentAlert(

                qrCodeUrl = wavPayQrPaymentUrl.value,
                paymentAmount = "${Constants.currencySymbol} $finalAmount",
                onDismiss = {
                    viewModel.hideQrPaymentAlertView()
                    viewModel.stopPaymentStatusPolling()
                }
            )
            viewModel.startWavPayQrPaymentStatusPolling()
        }
    }
    if (showPaymentSuccessDialog.value) {
        viewModel.hidePaymentView()
       /* PaymentSuccessAlert(

            onSendReceipt = {
                // Handle send receipt logic
                println("Receipt sent!")
                viewModel.hidePaymentSuccessAlertView()
                viewModel.initNewShopping()
            },
            onDismiss = {
                viewModel.hidePaymentSuccessAlertView()
            }
        )*/
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Payment Success",
            message = "Please collect your receipt.\nThank you for your purchase!",
            lottieFileName = "anim_success_3.json",
            type = AlertType.SUCCESS,
            isDismissible = false,
            positiveButtonText = "Print Receipt",
            onPositiveClick = {

                currentSystemAlert.value = null
                viewModel.timerDisplayForReceiptPrint()

                              },
            negativeButtonText = "Exit & Close",
            onNegativeClick = {
                currentSystemAlert.value = null
                viewModel.resetLoadCell()
                onLogout()
            }
        )
        LedSerialConnection.setScenario(AppScenario.PAYMENT_SUCCESS)
    }
    if (showPaymentErrorDialog.value) {
       /* PaymentFailureAlert(

            onRetry = {
                viewModel.hidePaymentSuccessAlertView()
                viewModel.hidePaymentErrorAlertView()
                showWalletScanner.value = true

            },
            onDismiss = {
                viewModel.hidePaymentErrorAlertView()
            }
        )*/
        currentSystemAlert.value = null
        currentSystemAlert.value = AlertState(
            title = "Payment was not successful",
            message = "Please try again or choose another payment method",
            lottieFileName = "anim_wrong.json",
            type = AlertType.ERROR,
            isDismissible = false
        )
        LedSerialConnection.setScenario(AppScenario.ERROR)
    }
    if (showDialog.value) {
        var lastClickTime = 0L
        val clickDebounceTime = 300L
        if (canShowPriceChecker.value) {
            ProductPriceAlert(viewModel = viewModel) {
                showDialog.value = false
            }
            focusRequester.requestFocus()
        } else {
            val currentTime = System.currentTimeMillis()
            if (currentTime - lastClickTime > clickDebounceTime) {
                lastClickTime = currentTime
                productInfo?.let { viewModel.addProductToShoppingCart(it.barcode, 1) }
                focusRequester.requestFocus()
            }
        }
        focusRequester.requestFocus()
    }
    if (showWalletScanner.value) {
        BarcodeScannerDialog(
            onDismiss = { showWalletScanner.value = false },
            onBarcodeScanned = {
                viewModel.initWavPayQrPayment(it)

                /*scannedCode.value = it
                viewModel.resetProductInfoDetails()
                viewModel.getProductDetails(scannedCode.value.toString())
                // scannerViewModel.onScanned(scannedCode.value.toString())*/
                showWalletScanner.value = false
            }
        )
    }
    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
        viewModel.initNewShopping()
    }
    if (canMakePayment.value) {
       // goToPaymentScreen()
        BitesPaymentDialog(
            viewModel,
            onDismiss = {
                viewModel.hidePaymentView()
                        },
            onHelpClicked = {
                currentSystemAlert.value = null
                currentSystemAlert.value = AlertState(
                    title = "Help is on the way",
                    message = "Please wait for our Staff to assist you.",
                    lottieFileName = "anim_warning_circle.json",
                    type = AlertType.INFO,
                    isDismissible = true,
                    positiveButtonText = "Ok",
                    onPositiveClick = {currentSystemAlert.value = null}
                )
            },
            onCardPaymentClicked = {viewModel.timerDisplayForPaymentProcess()},
            onWalletPaymentClicked = {viewModel.timerDisplayForPaymentProcess()},
            onGrabPaymentClicked = {viewModel.timerDisplayForPaymentProcess()}

        )
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .focusRequester(focusRequester)
            .focusTarget()
            .onKeyEvent { keyEvent ->
                if (keyEvent.type == androidx.compose.ui.input.key.KeyEventType.KeyUp) {
                    when (keyEvent.key) {
                        androidx.compose.ui.input.key.Key.Enter, androidx.compose.ui.input.key.Key.NumPadEnter -> {
                            if (scanBuffer.value.isNotBlank()) {
                                viewModel.resetProductInfoDetails()
                                viewModel.getProductDetails(scanBuffer.value)
                                focusRequester.requestFocus()

                                scanBuffer.value = ""
                            }
                            true
                        }

                        else -> {
                            val c = keyEvent.utf16CodePoint.toChar()
                            if (c.isLetterOrDigit()) {
                                scanBuffer.value += c
                            }
                            false
                        }
                    }
                } else {
                    // Also consume KeyDown for Enter to prevent the "pressed" state on buttons
                    keyEvent.key == androidx.compose.ui.input.key.Key.Enter
                }
            }
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            /*ModalNavigationDrawer(
                drawerState = drawerState,
                drawerContent = {
                    DrawerContent(
                        appMode = appMode,
                        onTransActionSelected = {
                            scope.launch {
                                scope.launch { drawerState.close() }
                                onTransactionCalled()
                                delay(100)
                                focusRequester.requestFocus()
                            }
                        },
                        onAppModeUpdated = { appMode ->
                            scope.launch {
                                drawerState.close()
                                delay(100)
                                focusRequester.requestFocus()
                            }
                            viewModel.onAppModeChange(appMode)

                        },
                        isChecked = canShowPriceChecker.value,
                        onCheckedChange = {
                            scope.launch {
                                drawerState.close()
                                delay(100)
                                focusRequester.requestFocus()
                            }
                            viewModel.setPriceCheckerView(it)
                        }
                    )
                }
            ) {*/

            BitesHeaderNew(
                viewModel, cartCount = cartCount.value, onHelpClick = {
                   // showTerminal.value = true
                    currentSystemAlert.value = null
                    currentSystemAlert.value = AlertState(
                        title = "Help is on the way",
                        message = "Please wait for our Staff to assist you.",
                        lottieFileName = "anim_warning_circle.json",
                        type = AlertType.INFO,
                        isDismissible = true,
                        positiveButtonText = "Ok",
                        onPositiveClick = {currentSystemAlert.value = null}
                    )
                                                                      },
                onTitleClick = {
                    viewModel.onPaymentSuccess("https://morth.nic.in/sites/default/files/dd12-13_0.pdf",context as Activity)
                   // viewModel.onPaymentSuccess("https://api-retailetics-ops-mini-03.retailetics.com/ezyCart/invoice/000VGO-P9900003312",context as Activity)
                   /* showMainLogs.value = !showMainLogs.value
                    viewModel.clearLog()*/
                })
            if (showMainLogs.value) {
                Text(
                    text = loadCellValidationLog.value,
                    color = Color.Red,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 20.sp
                    )

                )

            }

            /* Scaffold(
                *//* topBar = {
                    MyTopAppBar(

                        employeeName = employeeName.value,
                        cartCount = cartCount.value,
                        onMenuClick = {
                            scope.launch { drawerState.open() }
                        },
                        onFirstIconClick = { *//**//* notifications *//**//* },
                        onLogout = onLogout,
                        onRefresh = {
                            viewModel.initNewShopping()
                        }
                    )
                }*//*
            ) { innerPadding ->*/
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    //.padding(innerPadding)
                    .background(Color.White)
                    .focusRequester(focusRequester) // Add focus requester here too
                    .focusTarget(),
                contentAlignment = Alignment.Center
            ) {

                /* Button(
                     onClick = {
                         // Mock scanner input
                         scanBuffer.value = "6936489101973"

                         // Request focus and dispatch with delay
                         focusRequester.requestFocus()

                         // Use coroutine to ensure proper timing
                         CoroutineScope(Dispatchers.Main).launch {
                             delay(50) // Small delay to ensure focus is acquired

                             val mockKeyEvent = android.view.KeyEvent(
                                 android.view.KeyEvent.ACTION_DOWN, // Try ACTION_DOWN first
                                 android.view.KeyEvent.KEYCODE_ENTER
                             )
                             localView.dispatchKeyEvent(mockKeyEvent)

                             // Also send ACTION_UP
                             val upEvent = android.view.KeyEvent(
                                 android.view.KeyEvent.ACTION_UP,
                                 android.view.KeyEvent.KEYCODE_ENTER
                             )
                             localView.dispatchKeyEvent(upEvent)
                         }
                     },
                     modifier = Modifier.align(Alignment.Center)
                 ) {
                     Text("Mock Enter Key")
                 }*/

                PickersShoppingScreen(
                    viewModel,
                    onQrPaymentClick = {
                        showWalletScanner.value = true
                        // viewModel.initWavPayQrPayment()

                    },
                    onTapToPayClick = { viewModel.checkPaymentWeightValidation() },
                    //onTapToPayClick = goToPaymentScreen,
                    onLogout = { clearTransAction.value = true },
                )

            }
            //}
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
fun BitesHeaderNew(
    viewModel: HomeViewModel,
    cartCount: Int = 0,
    onHelpClick: () -> Unit,
    onTitleClick: () -> Unit
) {
    val showManualBarCode = remember { mutableStateOf(false) }
    val scannedCode = remember { mutableStateOf<String?>(null) }

    var showAdminDialog = remember { mutableStateOf(false) }
    if (showAdminDialog.value) {
        AdminSettingsDialog(
            onDismiss = { showAdminDialog.value = false },
            onOpenTerminal = { type ->
                // Logic to open specific terminal
                when(type) {
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

    if (showManualBarCode.value) {
        ManualBarcodeEntryDialog(
            onProceed = { barcode ->
                viewModel.resetProductInfoDetails()
                viewModel.getProductDetails(barcode)
                // scannerViewModel.onScanned(barcode)
                showManualBarCode.value = false
            },
            onCancel = {
                showManualBarCode.value = false
                println("Cancel clicked")
            },
            onDismiss = {
                showManualBarCode.value = false
            }
        )
       /* BarcodeScannerDialog(
            onDismiss = { showManualBarCode.value = false },
            onBarcodeScanned = {
                scannedCode.value = it
                viewModel.resetProductInfoDetails()
                viewModel.getProductDetails(scannedCode.value.toString())
                showManualBarCode.value = false
            }
        )*/
    }
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

            /* Row(
                 modifier = Modifier
                     .fillMaxWidth()
                     .height(80.dp),
                 verticalAlignment = Alignment.CenterVertically,
                 horizontalArrangement = Arrangement.Center
             ) {

                 Text(
                     text = "ezy",
                     style = TextStyle(
                         color = Color.Red,
                         fontSize = 20.sp,
                         fontWeight = FontWeight.Bold,
                         letterSpacing = 1.sp
                     ),
                     modifier = Modifier.clickable{onTitleClick()}

                 )
                 Text(
                     text = "Express",
                     style = TextStyle(
                         color = Color.Black,
                         fontSize = 20.sp,
                         fontWeight = FontWeight.ExtraBold,
                         letterSpacing = 1.sp
                     ),
                     modifier = Modifier.clickable{onTitleClick()}

                 )
             }*/

        }

        // 2. Purple Action Bar (Self Checkout & Help)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A)) // Deep Purple
                .padding(horizontal = 16.dp, vertical = 8.dp),
            contentAlignment = Alignment.Center // This ensures the 'SELF CHECKOUT' is exactly in the middle
        ) {
            // 1. LEFT SIDE: Scan Button
            Row(
                modifier = Modifier.align(Alignment.CenterStart),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Barcode Scan Button
                Box(
                    modifier = Modifier
                        .size(35.dp)
                        .clickable { showManualBarCode.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_scan),
                        contentDescription = "scan",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                Spacer(modifier = Modifier.width(35.dp)) // Space between the two icons

                // Settings Button
                Box(
                    modifier = Modifier
                        .size(33.dp)
                        .clickable { showAdminDialog.value = true },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_settings),
                        contentDescription = "settings",
                        tint = Color.White,
                        modifier = Modifier.fillMaxSize()
                    )
                }
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
                modifier = Modifier.clickable { onTitleClick() }
            )

            // 3. RIGHT SIDE: Help Content
            Row(
                modifier = Modifier
                    .align(Alignment.CenterEnd) // Force to far right
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
                Spacer(modifier = Modifier.width(20.dp))
                CartIconWithBadge(count = cartCount)
            }
        }
    }
}

/*@androidx.annotation.OptIn(ExperimentalLensFacing::class)
@Composable
fun CameraPreview(modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val previewView = remember { PreviewView(context) }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }

    LaunchedEffect(Unit) {
        val cameraProvider = cameraProviderFuture.get()

        // 1. Build the Preview use case
        val preview = androidx.camera.core.Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()

            // 2. LOGIC FIX: Find the correct camera for an Android Box
            // Instead of DEFAULT_BACK, we look for any available camera
            val cameraInfoList = cameraProvider.availableCameraInfos

            // Try to find an external camera first (USB), then fall back to anything available
            val selectedCameraInfo = cameraInfoList.firstOrNull { info ->
                val facing = info.lensFacing
                facing == CameraSelector.LENS_FACING_EXTERNAL ||
                        facing == CameraSelector.LENS_FACING_BACK ||
                        facing == CameraSelector.LENS_FACING_FRONT
            } ?: cameraInfoList.firstOrNull() // Absolute fallback to the first camera found

            if (selectedCameraInfo != null) {
                val cameraSelector = selectedCameraInfo.cameraSelector
                cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview)
                Log.d("Camera", "Bound to camera facing: ${selectedCameraInfo.lensFacing}")
            } else {
                Log.e("Camera", "No cameras found on this device")
            }

        } catch (e: Exception) {
            Log.e("Camera", "Use case binding failed", e)
        }
    }

    AndroidView(
        factory = { previewView },
        modifier = modifier.clip(RoundedCornerShape(12.dp))
    )
}*/
/*@androidx.annotation.OptIn(ExperimentalLensFacing::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onRecordingStarted: () -> Unit = {},
    onRecordingFinished: (Uri?) -> Unit = {},
    onCaptureReady: (start: () -> Unit, stop: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Initialize PreviewView with COMPATIBLE mode to allow rotation
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val videoCaptureState = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        VideoCapture.withOutput(recorder)
    }

    val currentRecording = remember { mutableStateOf<Recording?>(null) }

    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        // 2. Setup Preview Use Case
        val preview = Preview.Builder()
            .build()
            .also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

        try {
            cameraProvider.unbindAll()

            // Select External or Back camera
            val cameraInfoList = cameraProvider.availableCameraInfos
            val selectedCameraInfo = cameraInfoList.firstOrNull { info ->
                info.lensFacing == CameraSelector.LENS_FACING_EXTERNAL ||
                        info.lensFacing == CameraSelector.LENS_FACING_BACK
            } ?: cameraInfoList.firstOrNull()

            if (selectedCameraInfo != null) {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selectedCameraInfo.cameraSelector,
                    preview,
                    videoCaptureState
                )
            }

            // Define Recording Logic
            val startRecording = {
                val fileName = "SelfCheckout_${System.currentTimeMillis()}.mp4"
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
                    put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
                    put(MediaStore.Video.Media.RELATIVE_PATH, Environment.DIRECTORY_MOVIES + "/CheckoutVideos")
                }

                val mediaStoreOutputOptions = MediaStoreOutputOptions
                    .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
                    .setContentValues(contentValues)
                    .build()

                currentRecording.value = videoCaptureState.output
                    .prepareRecording(context, mediaStoreOutputOptions)
                    .apply {
                        if (PermissionChecker.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                            withAudioEnabled()
                        }
                    }
                    .start(ContextCompat.getMainExecutor(context)) { event ->
                        when (event) {
                            is VideoRecordEvent.Start -> onRecordingStarted()
                            is VideoRecordEvent.Finalize -> {
                                if (!event.hasError()) {
                                    onRecordingFinished(event.outputResults.outputUri)
                                }
                                currentRecording.value = null
                            }
                        }
                    }
            }

            val stopRecording = {
                currentRecording.value?.stop()
                currentRecording.value = null
            }

            onCaptureReady(startRecording, stopRecording)

        } catch (e: Exception) {
            Log.e("CameraPreview", "Binding failed", e)
        }
    }

    // 3. Render and Rotate the View
    AndroidView(
        factory = { previewView },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .layout { measurable, constraints ->
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )

                layout(placeable.height, placeable.width) {
                    placeable.placeWithLayer(
                        x = -(placeable.width - placeable.height) / 2,
                        y = -(placeable.height - placeable.width) / 2
                    ) {
                        rotationZ = 90f // Hardware correction
                    }
                }
            }
    )
}*/

@androidx.annotation.OptIn(ExperimentalLensFacing::class)
@Composable
fun CameraPreview(
    modifier: Modifier = Modifier,
    onRecordingStarted: () -> Unit = {},
    onRecordingFinished: (Uri?) -> Unit = {},
    onCaptureReady: (start: () -> Unit, stop: () -> Unit) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // 1. Initialize PreviewView with COMPATIBLE mode for rotation
    val previewView = remember {
        PreviewView(context).apply {
            implementationMode = PreviewView.ImplementationMode.COMPATIBLE
            scaleType = PreviewView.ScaleType.FILL_CENTER
        }
    }

    val videoCaptureState = remember {
        val recorder = Recorder.Builder()
            .setQualitySelector(QualitySelector.from(Quality.HIGHEST))
            .build()
        VideoCapture.withOutput(recorder)
    }

    val currentRecording = remember { mutableStateOf<Recording?>(null) }

    // 2. Define Recording Logic with explicit types to fix "Argument type mismatch"
    val startRecording: () -> Unit = {
        val fileName = "SelfCheckout_${System.currentTimeMillis()}"
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4")
            put(MediaStore.Video.Media.RELATIVE_PATH, "Movies/CheckoutVideos")
        }

        val mediaStoreOutputOptions = MediaStoreOutputOptions
            .Builder(context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI)
            .setContentValues(contentValues)
            .build()

        try {
            val recording = videoCaptureState.output
                .prepareRecording(context, mediaStoreOutputOptions)
                .apply {
                    if (PermissionChecker.checkSelfPermission(context, android.Manifest.permission.RECORD_AUDIO) == PermissionChecker.PERMISSION_GRANTED) {
                        withAudioEnabled()
                    }
                }
                .start(ContextCompat.getMainExecutor(context)) { event ->
                    when (event) {
                        is VideoRecordEvent.Start -> onRecordingStarted()
                        is VideoRecordEvent.Finalize -> {
                            if (!event.hasError()) {
                                onRecordingFinished(event.outputResults.outputUri)
                            } else {
                                Log.e("CameraPreview", "Recording error: ${event.error}")
                            }
                            currentRecording.value = null
                        }
                    }
                }
            currentRecording.value = recording
        } catch (e: Exception) {
            Log.e("CameraPreview", "Start recording failed", e)
        }
        // Ensure lambda returns Unit
        Unit
    }

    val stopRecording: () -> Unit = {
        currentRecording.value?.stop()
        currentRecording.value = null
    }

    // 3. Bind Camera and restore your External Camera logic
    LaunchedEffect(Unit) {
        val cameraProvider = ProcessCameraProvider.getInstance(context).get()

        val preview = Preview.Builder().build().also {
            it.setSurfaceProvider(previewView.surfaceProvider)
        }

        try {
            cameraProvider.unbindAll()

            // RESTORED: Your specific External -> Back -> First logic
            val cameraInfoList = cameraProvider.availableCameraInfos
            val selectedCameraInfo = cameraInfoList.firstOrNull { info ->
                info.lensFacing == CameraSelector.LENS_FACING_EXTERNAL ||
                        info.lensFacing == CameraSelector.LENS_FACING_BACK
            } ?: cameraInfoList.firstOrNull()

            if (selectedCameraInfo != null) {
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    selectedCameraInfo.cameraSelector,
                    preview,
                    videoCaptureState
                )
            }

            // Pass the typed functions to your UI
            onCaptureReady(startRecording, stopRecording)

        } catch (e: Exception) {
            Log.e("CameraPreview", "Binding failed", e)
        }
    }

    // 4. Render and apply the 90-degree Hardware Fix
    AndroidView(
        factory = { previewView },
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .layout { measurable, constraints ->
                // Swap dimensions for rotation
                val placeable = measurable.measure(
                    constraints.copy(
                        minWidth = constraints.minHeight,
                        maxWidth = constraints.maxHeight,
                        minHeight = constraints.minWidth,
                        maxHeight = constraints.maxWidth
                    )
                )

                layout(placeable.height, placeable.width) {
                    placeable.placeWithLayer(
                        x = -(placeable.width - placeable.height) / 2,
                        y = -(placeable.height - placeable.width) / 2
                    ) {
                        rotationZ = 90f // Change to 270f if needed by TL
                    }
                }
            }
    )
}

@Composable
fun EmptyCartScreen(

    isPickerModel: Boolean,
    onScanBarcode: () -> Unit,
    onEnterBarcodeManually: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(vertical = 8.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = R.drawable.empty_trolley_2),
            contentDescription = "appLogo",
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .size(
                    width = 350.dp,
                    height = 350.dp
                )
                .graphicsLayer(
                    scaleX = -1f
                )
        )
        Text(
            text = "Cart is Empty",
            fontSize = 33.sp,
            color = MaterialTheme.colorScheme.tertiary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(
            text = "Scan a product barcode to begin shopping",
            fontSize = 28.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(35.dp))
        /* ManualBarcodeEntryButton(
             onEnterBarcodeManually, modifier = Modifier
                 .width(230.dp)
                 .height(48.dp), 20f
         )
         Spacer(modifier = Modifier.height(20.dp))
         if (!isPickerModel) {
             ScannerButton(
                 onScanBarcode, Modifier
                     .width(230.dp)
                     .height(48.dp), 20f
             )
         }*/

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PickersShoppingScreen(
    viewModel: HomeViewModel,
    onQrPaymentClick: () -> Unit,
    onTapToPayClick: () -> Unit,
    onLogout: () -> Unit,
) {
    val showScanner = remember { mutableStateOf(false) }
    val showManualBarCode = remember { mutableStateOf(false) }
    val scannedCode = remember { mutableStateOf<String?>(null) }
    val cartDataList = viewModel.cartDataList.collectAsState()
    val shoppingCartInfo = viewModel.shoppingCartInfo.collectAsState()
    val isPickerModel = viewModel.isPickerModel.collectAsState()

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    var showSheet = remember { mutableStateOf(false) }

    if (showSheet.value) {
        ModalBottomSheet(
            onDismissRequest = { showSheet.value = false },
            sheetState = sheetState,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
            containerColor = Color.White,
            dragHandle = null
        ) {
            // Wrap the content in a Box to control the height
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(0.7f) // This sets the height to 70% of the screen
            ) {
                CheckoutSummaryScreen(
                    shoppingCartInfo = shoppingCartInfo,
                    onQrPaymentClick = { onQrPaymentClick() },
                    onTapToPayClick = { onTapToPayClick() }
                )
            }
        }
    }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .padding(10.dp)
    ) {

        Box(
            modifier = Modifier
                .then(

                    Modifier.fillMaxWidth()

                )
                .fillMaxHeight()
                .background(Color.White),

            contentAlignment = Alignment.Center
        ) {
            if (cartDataList.value.isEmpty()) {
                EmptyCartScreen(

                    isPickerModel.value,
                    //scannerViewModel,
                    onScanBarcode = { showScanner.value = true },
                    onEnterBarcodeManually = { showManualBarCode.value = true })
            } else {
                CartScreen(

                    shoppingCartInfo,
                    isPickerModel.value,
                    cartItems = cartDataList.value,
                    onScanBarcode = { showScanner.value = true },
                    onEnterBarcodeManually = { showManualBarCode.value = true },
                    onClearCart = { viewModel.initNewShopping() },
                    onRemoveItem = { cartItem ->
                        viewModel.deleteProductFromShoppingCart(cartItem.barcode, cartItem.id)
                    },
                    onEditProduct = { barCode, id, quantity ->
                        viewModel.editProductInShoppingCart(
                            barCode = barCode,
                            quantity = quantity,
                            id = id
                        )
                    },
                    onPayNowClick = onTapToPayClick,
                    onLogout = onLogout,
                )
            }
        }

    }

    if (showScanner.value) {
        BarcodeScannerDialog(
            onDismiss = { showScanner.value = false },
            onBarcodeScanned = {
                scannedCode.value = it
                viewModel.resetProductInfoDetails()
                viewModel.getProductDetails(scannedCode.value.toString())
                // scannerViewModel.onScanned(scannedCode.value.toString())
                showScanner.value = false
            }
        )
    }
    if (showManualBarCode.value) {
        ManualBarcodeEntryDialog(
            onProceed = { barcode ->
                viewModel.resetProductInfoDetails()
                viewModel.getProductDetails(barcode)
                // scannerViewModel.onScanned(barcode)
                showManualBarCode.value = false
            },
            onCancel = {
                showManualBarCode.value = false
                println("Cancel clicked")
            },
            onDismiss = {
                showManualBarCode.value = false
            }
        )
    }
}

@Composable
fun CheckoutSummaryScreen(
    shoppingCartInfo: State<ShoppingCartDetails?>,
    onQrPaymentClick: () -> Unit,
    onTapToPayClick: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        val paymentSummary = shoppingCartInfo.value
        Column {
            BillRow(
                "Sub Total",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.totalPrice ?: 0.0)}",
                color = Color.Black
            )
            BillRow(
                "Promo Discount",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.promotionSave ?: 0.0)}",
                color = Color.Black
            )
            BillRow(
                "Special Discount",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.totalDiscount ?: 0.0)}",
                color = Color.Black
            )
            BillRow(
                "Coupon Discount",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.couponAmount ?: 0.0)}",
                color = Color.Black
            )
            BillRow(
                "Voucher Discount",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.vourcherAmount ?: 0.0)}",
                color = Color.Black
            )
            BillRow(
                "Tax",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.totalTax ?: 0.0)}",
                color = Color.Black
            )
            Divider(modifier = Modifier.padding(vertical = 8.dp))
            Spacer(modifier = Modifier.height(5.dp))
            BillRow(
                "Grand Total",
                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.finalAmount ?: 0.0)}",
                isBold = true,
                color = MaterialTheme.colorScheme.primary
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        // Apply Coupon / Voucher Button
        /* Button(
             onClick = {

             },
             modifier = Modifier
                 .fillMaxWidth()
                 .height(45.dp),
             shape = RoundedCornerShape(50.dp),
             colors = ButtonDefaults.buttonColors(
                 containerColor = colorResource(R.color.colorGreen),      // background color
                 contentColor = Color.White,              // default for text & icons
                 disabledContainerColor = Color.Gray,     // background when disabled
                 disabledContentColor = Color.LightGray   // text/icon when disabled
             )
         ) {
             Icon(
                 painter = painterResource(id = R.drawable.ic_coupon_white),
                 contentDescription = null,
                 modifier = Modifier.size(25.dp)
             )
             Spacer(modifier = Modifier.width(8.dp))
             Text(
                 text = "Apply Coupon / Voucher",
                 style = MaterialTheme.typography.headlineSmall.copy(
                     fontWeight = FontWeight.Bold,
                     fontSize = 17.sp,
                     color = Color.White
                 )
             )
         }*/

        // Take up remaining space → pushes "Total Payable" to bottom
        Spacer(modifier = Modifier.weight(1f))

        // Total Payable
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AnimatedPayableText()
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "${Constants.currencySymbol} ${paymentSummary?.finalAmount ?: 0.0}",
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 40.sp,
                    color = MaterialTheme.colorScheme.primary
                )
            )
            Spacer(modifier = Modifier.height(20.dp))
            Button(
                onClick = {
                    onTapToPayClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                shape = RoundedCornerShape(50.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_contactless_24),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Credit / Debit Tap To Pay",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                )
            }
            Spacer(modifier = Modifier.height(15.dp))
            Button(
                onClick = {
                    onQrPaymentClick()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(45.dp),
                shape = RoundedCornerShape(50.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorResource(R.color.colorAccent),      // background color
                    contentColor = Color.White,              // default for text & icons
                    disabledContainerColor = Color.Gray,     // background when disabled
                    disabledContentColor = Color.LightGray   // text/icon when disabled
                )
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_qr_code_24),
                    contentDescription = null,
                    modifier = Modifier.size(25.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "QR Payment",
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 17.sp,
                        color = Color.White
                    )
                )
            }
        }
    }
}

@Composable
fun AnimatedPayableText() {
    val infiniteTransition = rememberInfiniteTransition(label = "payableAnim")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.1f, // slightly bigger
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scaleAnim"
    )

    Text(
        text = "Total Payable",
        style = MaterialTheme.typography.headlineSmall.copy(
            fontWeight = FontWeight.Bold,
            fontSize = 30.sp,
            color = MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier
            .graphicsLayer(
                scaleX = scale,
                scaleY = scale
            )
    )
}

@Composable
fun ManualBarcodeEntryButton(
    onEnterBarcodeManually: () -> Unit,
    modifier: Modifier = Modifier,
    fontSize: Float
) {
    Button(
        onClick = onEnterBarcodeManually,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(colorResource(R.color.colorGreen))
    ) {
        Icon(
            modifier = Modifier.size(22.dp),
            painter = painterResource(id = R.drawable.ic_edit),
            contentDescription = null,
            tint = Color.White
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Enter Barcode", style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = fontSize.sp
            )
        )
    }
}

@Composable
fun ScannerButton(onScanBarcode: () -> Unit, modifier: Modifier = Modifier, fontSize: Float) {
    Button(
        onClick = onScanBarcode,
        modifier = modifier,
        shape = RoundedCornerShape(50),
        colors = ButtonDefaults.buttonColors(colorResource(R.color.colorPrimary))
    ) {
        Icon(
            modifier = Modifier.size(33.dp),
            painter = painterResource(id = R.drawable.ic_scan),
            contentDescription = null,
            tint = Color.White
        )

        Spacer(modifier = Modifier.width(8.dp))
        Text(
            "Scan Barcode",
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = fontSize.sp
            )
        )

    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CartScreen(
    shoppingCartInfo: State<ShoppingCartDetails?>,
    isPickerModel: Boolean,
    cartItems: List<CartItem>,
    onScanBarcode: () -> Unit,
    onEnterBarcodeManually: () -> Unit,
    onClearCart: () -> Unit,
    onRemoveItem: (CartItem) -> Unit,
    modifier: Modifier = Modifier,
    onEditProduct: (String, Int, Int) -> Unit,
    onPayNowClick: () -> Unit,
    onLogout: () -> Unit,
) {
    var isRecording = remember { mutableStateOf(false) }
    var startFunc = remember { mutableStateOf<(() -> Unit)?>(null) }
    var stopFunc = remember { mutableStateOf<(() -> Unit)?>(null) }
    val paymentSummary = shoppingCartInfo.value
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = Color.White,
        bottomBar = {
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(550.dp),
                /*.navigationBarsPadding(),*/
                color = Color.White,
                shadowElevation = 8.dp,
                shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
            ) {
                Column(
                    modifier = Modifier.padding(24.dp)
                ) {
                    // --- BILLING SECTION ---
                    val summaryRows = listOf(
                        "Sub Total" to (paymentSummary?.totalPrice ?: 0.0),
                        "Promo Discount" to (paymentSummary?.promotionSave ?: 0.0),
                        "Special Discount" to (paymentSummary?.totalDiscount ?: 0.0),
                        "Coupon Discount" to (paymentSummary?.couponAmount ?: 0.0),
                        "Voucher Discount" to (paymentSummary?.vourcherAmount ?: 0.0),
                        "Tax" to (paymentSummary?.totalTax ?: 0.0)
                    )

                    summaryRows.forEach { (label, value) ->
                        BillRow(
                            label,
                            "${Constants.currencySymbol} ${getFormattedPrice(value)}",
                            color = Color.Black
                        )
                    }

                    Divider(
                        modifier = Modifier.padding(vertical = 12.dp),
                        thickness = 1.dp,
                        color = Color.LightGray
                    )



                    Spacer(modifier = Modifier.weight(1f)) // Pushes content to the bottom

                    // --- ACTION SECTION (Camera + Buttons) ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.Top, // Aligns buttons and camera to the bottom
                        horizontalArrangement = Arrangement.SpaceBetween // Aligns Camera Left and Buttons Right
                    ) {
                        // LEFT: Camera Preview
                        Box(
                            modifier = Modifier
                                .size(
                                    width = 270.dp,
                                    height = 220.dp
                                ) // Fixed Aspect Ratio for Camera
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color.Black)
                                .border(2.dp, Color(0xFFE0E0E0), RoundedCornerShape(16.dp))
                        ) {
                            // CameraPreview(modifier = Modifier.fillMaxSize())
                            CameraPreview(
                                modifier = Modifier
                                    .fillMaxSize(),

                                onRecordingStarted = { isRecording.value = true },
                                onRecordingFinished = { uri ->
                                    isRecording.value = false
                                    Log.d("Video", "Video saved at: $uri")
                                },
                                onCaptureReady = { start, stop ->
                                    startFunc.value = start
                                    stopFunc.value = stop
                                }
                            )
                            // Live Badge
                            Row(
                                modifier = Modifier
                                    .align(Alignment.TopStart)
                                    .padding(8.dp)
                                    .background(Color.Black.copy(0.6f), RoundedCornerShape(4.dp))
                                    .padding(horizontal = 6.dp, vertical = 2.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .background(Color.Red, CircleShape)
                                )
                                Spacer(Modifier.width(4.dp))
                                Text(
                                    "LIVE",
                                    color = Color.White,
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        // RIGHT: Buttons Column
                        Column(
                            modifier = Modifier.width(350.dp), // Fixed width for button consistency
                            horizontalAlignment = Alignment.End,
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            BillRow(
                                "Grand Total",
                                "${Constants.currencySymbol} ${getFormattedPrice(paymentSummary?.finalAmount ?: 0.0)}",
                                isBold = true,
                                color = MaterialTheme.colorScheme.primary,
                            )
                            // APPLY VOUCHER BUTTON
                            OutlinedButton(
                                onClick = { /* Add your logic */ },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp),
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.primary)
                            ) {
                                Text(
                                    "APPLY VOUCHER",
                                    style = TextStyle(
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            // PAY NOW BUTTON
                            Button(
                                onClick = {
                                    onPayNowClick()
                                    stopFunc?.value?.invoke()
                                    isRecording.value = false
                                },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(50.dp)
                                    .shadow(8.dp, RoundedCornerShape(12.dp)),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(
                                        0xFF8BC34A
                                    )
                                ), // Light Green
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Text(
                                    text = "PAY NOW",
                                    style = TextStyle(
                                        color = Color.White,
                                        fontSize = 28.sp,
                                        fontWeight = FontWeight.Black
                                    )
                                )
                            }

                            // CANCEL TRANSACTION (Red and Simple)
                            TextButton(
                                onClick = onLogout,
                                modifier = Modifier.padding(top = 4.dp)
                            ) {
                                Text(
                                    text = "Cancel Transaction",
                                    style = TextStyle(
                                        color = Color.Red,
                                        fontSize = 18.sp,
                                        fontWeight = FontWeight.Bold,
                                        textDecoration = TextDecoration.Underline
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                        }
                    }
                }
            }
        }
    ) { innerPadding ->
        // content: cart list
        if (cartItems.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("Your cart is empty", color = Color.Gray)
            }
        } else {
            if (!isRecording.value) {
                startFunc?.value?.invoke()
                isRecording.value = true
            }
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(bottom = 80.dp) // leave room for bottom bar
            ) {
                //repeat(20) {
                itemsIndexed(cartItems.reversed()) { index, productData ->
                    CartItemCard(
                        productInfo = cartItems[index],
                        onRemove = { onRemoveItem(it) },
                        onEditProduct = { barCode, id, updatedQuantity ->
                            onEditProduct(barCode, id, updatedQuantity)
                        },
                    )
                }
                //}
            }
        }
    }
}

@Composable
fun ActionButton(
    modifier: Modifier,
    label: String,
    icon: Int,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(55.dp),
        shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
        contentPadding = PaddingValues(0.dp) // Ensures content fits in small weights
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(id = icon),
                contentDescription = null,
                modifier = Modifier.size(22.dp),
                tint = Color.White
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color.White,
                    fontSize = 9.sp
                ),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
fun CartItemCard(
    productInfo: CartItem,
    onRemove: (CartItem) -> Unit,
    modifier: Modifier = Modifier,
    onEditProduct: (String, Int, Int) -> Unit,
) {
    var showDeleteDialog = remember { mutableStateOf(false) }
    var showEditDialog = remember { mutableStateOf(false) }
    var selectedCartItem = remember { mutableStateOf<CartItem?>(null) }

    if (showDeleteDialog.value && selectedCartItem.value != null) {
        DeleteProductDialog(
            productName = selectedCartItem.value?.productName ?: "",
            productCode = selectedCartItem.value?.barcode ?: "",
            oldPrice = "${Constants.currencySymbol} ${selectedCartItem.value?.originalPrice ?: 0.0}",
            newPrice = "${Constants.currencySymbol} ${selectedCartItem.value?.finalPrice ?: 0.0}",
            imageRes = selectedCartItem.value?.imageUrl ?: "",
            onRemove = {
                // handle remove
                onRemove(selectedCartItem.value!!)
                showDeleteDialog.value = false
            },
            onDismiss = { showDeleteDialog.value = false }
        )
    }
    if (showEditDialog.value && selectedCartItem.value != null) {
        EditProductDialog(
            productName = selectedCartItem.value?.productName ?: "",
            productCode = selectedCartItem.value?.barcode ?: "",
            oldPrice = "${Constants.currencySymbol} ${selectedCartItem.value?.originalPrice ?: 0.0}",
            newPrice = "${Constants.currencySymbol} ${selectedCartItem.value?.finalPrice ?: 0.0}",
            imageRes = selectedCartItem.value?.imageUrl ?: "",
            currentQuantity = selectedCartItem.value?.quantity ?: 1,
            onEdit = { updatedQuantity ->
                if (selectedCartItem.value?.quantity != updatedQuantity) {
                    selectedCartItem.value?.let {
                        onEditProduct(
                            it.barcode,
                            it.id,
                            updatedQuantity
                        )
                    }
                }
                showEditDialog.value = false
            },
            onDismiss = { showEditDialog.value = false }
        )
    }
    Card(
        modifier = modifier
            .height(110.dp)
            .fillMaxWidth()
            .padding(horizontal = 3.dp, vertical = 2.dp),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp),
            verticalAlignment = Alignment.CenterVertically // Keeps everything aligned in the middle vertically
        ) {
            // --- 1. LEFT: IMAGE ---
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F2F2)),
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = productInfo.imageUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                    placeholder = painterResource(R.drawable.ic_no_product_image),
                    error = painterResource(R.drawable.ic_no_product_image)
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- 2. CENTER: PRODUCT NAME & DISCOUNT ---
            Column(
                modifier = Modifier.weight(1f), // Takes up all available middle space
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = productInfo.productName,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    ),
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Discount Row
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_discount_icon),
                        contentDescription = "discount",
                        tint = colorResource(R.color.colorOrange),
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Buy One Get One",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 16.sp,
                            color = colorResource(R.color.colorOrange)
                        )
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // --- 3. RIGHT: PRICE DETAILS ---
            Column(
                horizontalAlignment = Alignment.End, // Aligns text to the right
                verticalArrangement = Arrangement.Center
            ) {
                // Quantity x Unit Price
                Text(
                    text = "${productInfo.displayQty} x ${Constants.currencySymbol}${
                        "%.2f".format(
                            productInfo.unitPrice
                        )
                    }",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                        color = Color.Gray
                    )
                )

                // Strikethrough Price (if discount exists)
                if (productInfo.finalPriceBeforeDiscount != productInfo.finalPrice) {
                    Text(
                        text = "${Constants.currencySymbol}${"%.2f".format(productInfo.finalPriceBeforeDiscount)}",
                        style = TextStyle(
                            fontWeight = FontWeight.Normal,
                            fontSize = 16.sp,
                            textDecoration = TextDecoration.LineThrough,
                            color = Color.Red
                        )
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                // Final Price
                Text(
                    text = "${Constants.currencySymbol}${"%.2f".format(productInfo.finalPrice)}",
                    style = TextStyle(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp, // Made slightly larger for prominence
                        color = colorResource(R.color.colorPrimary)
                    )
                )
            }
        }
    }
    /*Card(
        modifier = modifier
            .then(


                    Modifier.height(130.dp)

            )
            .fillMaxWidth()
            .padding(
                horizontal = 3.dp,
                vertical = 2.dp
            ),
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight()
                .padding(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Spacer(modifier = Modifier.width(4.dp))
            // Image
            Box(
                modifier = Modifier
                    .size(90.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFF2F2F2)),
                contentAlignment = Alignment.Center
            ) {
                if (productInfo.imageUrl != null) {
                    AsyncImage(
                        model = productInfo.imageUrl,
                        contentDescription = "${productInfo.id}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize(),
                        placeholder = painterResource(R.drawable.ic_no_product_image),
                        error = painterResource(R.drawable.ic_no_product_image)
                    )
                } else {
                    Image(
                        painter = painterResource(id = R.drawable.ic_no_product_image),
                        contentDescription = "${productInfo.id}",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            // Product name
            Column(
                modifier = Modifier.weight(1f) // take remaining space before price + icons
            ) {
                Text(
                    text = productInfo.productName,
                    modifier = Modifier.padding(start = 3.dp),
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.primary,
                        fontSize = 20.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height( 3.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        // This adds exactly 12.dp of space between every child in the Row
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Spacer(modifier = Modifier.width(3.dp))
                        // Item 1
                        *//*Text(
                            text = "${productInfo.displayQty} x ${Constants.currencySymbol} ${
                                "%.2f".format(
                                    productInfo.unitPrice
                                )
                            }",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 18.sp
                            ),
                            color = Color.Gray
                        )

                        // Item 2 (Conditional)
                        if (productInfo.finalPriceBeforeDiscount != productInfo.finalPrice) {
                            Text(
                                text = "${Constants.currencySymbol} ${"%.2f".format(productInfo.finalPriceBeforeDiscount)}",
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 18.sp
                                ),
                                textDecoration = TextDecoration.LineThrough
                            )
                        }

                        // Item 3
                        Text(
                            text = "${Constants.currencySymbol} ${"%.2f".format(productInfo.finalPrice)}",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                fontWeight = FontWeight.Bold,
                                fontSize = 18.sp,
                                color = colorResource(R.color.colorPrimary)
                            )
                        )
                    }
                    Spacer(modifier = Modifier.height(3.dp))*//*

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_discount_icon),
                        contentDescription = "discount",
                        tint = colorResource(R.color.colorOrange),
                        modifier = Modifier
                            .size(35.dp)
                            .padding(start = 4.dp, end = 6.dp)
                    )

                    Text(
                        text = "Buy One Get One",
                        style = MaterialTheme.typography.bodyLarge.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp,
                            color = colorResource(R.color.colorOrange)
                        )
                    )
                }


            }


            Spacer(Modifier.width( 3.dp))
            // Delete icon
            *//*Icon(
                painter = painterResource(id = R.drawable.ic_box_delete),
                contentDescription = "Delete ${productInfo.id}",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size( 50.dp)
                    .padding(start = 4.dp)
                    .clickable {
                        selectedCartItem.value = productInfo
                        showDeleteDialog.value = true
                    }
            )
            Spacer(Modifier.width(5.dp))
            // Edit icon
            Icon(
                painter = painterResource(id = R.drawable.ic_edit_box),
                contentDescription = "Edit ${productInfo.id}",
                tint = Color.Unspecified,
                modifier = Modifier
                    .size(50.dp)
                    .padding(start = 4.dp)
                    .clickable {
                        selectedCartItem.value = productInfo
                        showEditDialog.value = true
                    }
            )*//*
                Text(
                    text = "${productInfo.displayQty} x ${Constants.currencySymbol} ${
                        "%.2f".format(
                            productInfo.unitPrice
                        )
                    }",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp
                    ),
                    color = Color.Gray
                )

                // Item 2 (Conditional)
                if (productInfo.finalPriceBeforeDiscount != productInfo.finalPrice) {
                    Text(
                        text = "${Constants.currencySymbol} ${"%.2f".format(productInfo.finalPriceBeforeDiscount)}",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            fontWeight = FontWeight.Bold,
                            fontSize = 18.sp
                        ),
                        textDecoration = TextDecoration.LineThrough
                    )
                }

                // Item 3
                Text(
                    text = "${Constants.currencySymbol} ${"%.2f".format(productInfo.finalPrice)}",
                    style = MaterialTheme.typography.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp,
                        color = colorResource(R.color.colorPrimary)
                    )
                )
            }
            Spacer(modifier = Modifier.height(3.dp))

        }
    }*/

}

@Composable
fun CancelConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Cancel Transaction?",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Text(
                text = "Are you sure you want to cancel? All items in your cart will be cleared.",
                style = MaterialTheme.typography.bodyLarge,
                fontSize = 20.sp // Larger for kiosk readability
            )
        },
        confirmButton = {
            Button(
                onClick = onConfirm,
                modifier = Modifier
                    .padding(8.dp)
                    .height(60.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
            ) {
                Text("YES, CANCEL", color = Color.White, fontSize = 18.sp)
            }
        },
        dismissButton = {
            OutlinedButton(
                onClick = onDismiss,
                modifier = Modifier
                    .padding(8.dp)
                    .height(60.dp)
            ) {
                Text("NO, GO BACK", fontSize = 18.sp)
            }
        },
        shape = RoundedCornerShape(24.dp),
        containerColor = Color.White
    )
}

@Composable
fun BillRow(label: String, value: String, isBold: Boolean = false, color: Color = Color.Gray) {
    val scale by animateFloatAsState(
        targetValue = if (isBold) 1.2f else 1.0f, // Zooms to 120% when bold
        animationSpec = if (isBold) {
            // Continuous pulsing effect (Zoom in/out)
            infiniteRepeatable(
                animation = tween(durationMillis = 600, easing = FastOutSlowInEasing),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            // Smooth transition back to normal
            tween(durationMillis = 300)
        },
        label = "AmountZoom"
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            color = color,
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 27.sp
            )
            else MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        )
        Text(
            text = value,
            color = color,
            modifier = Modifier.graphicsLayer(
                scaleX = scale,
                scaleY = scale
            ),
            style = if (isBold) MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Medium,
                fontSize = 30.sp
            )
            else MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.SemiBold,
                fontSize = 20.sp
            )
        )
    }
}



@Composable
fun CartIconWithBadge(
    count: Int,

    modifier: Modifier = Modifier,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .size(25.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.TopEnd
    ) {
        Icon(
            painter = painterResource(id = R.drawable.ic_cart), // your vector drawable
            contentDescription = "Cart",
            tint = Color.White,
            modifier = Modifier.fillMaxSize()
        )

        //if (count > 0) {
        Box(
            modifier = Modifier
                .offset(x = (4).dp, y = (-4).dp)
                .size(if (count > 99) 26.dp else 20.dp)
                .background(Color.Red, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (count > 99) "99+" else count.toString(),
                color = Color.White,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                fontSize = 7.sp
            )
        }
        //}
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoundedDropdownSpinner(
    items: List<String>,
    selectedItem: String,
    onItemSelected: (String) -> Unit
) {
    var expanded = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
        modifier = Modifier.fillMaxWidth()
    ) {
        TextField(
            value = selectedItem,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(
                    text = "Select Customer's Pick List",
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontWeight = FontWeight.Medium,
                        fontSize = 12.sp,
                        color = Color.Black
                    )
                )
            },
            trailingIcon = {
                Icon(
                    imageVector = if (expanded.value) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            },
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
                focusedContainerColor = Color.Transparent,
                unfocusedContainerColor = Color.Transparent,
                disabledContainerColor = Color.Transparent,
                focusedTextColor = MaterialTheme.colorScheme.primary,
                unfocusedTextColor = MaterialTheme.colorScheme.primary
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .menuAnchor()
                .fillMaxWidth()
                .border(
                    width = 1.dp,
                    color = MaterialTheme.colorScheme.primary,
                    shape = RoundedCornerShape(12.dp)
                )
        )

        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            items.forEach { item ->
                DropdownMenuItem(
                    text = { Text(item) },
                    onClick = {
                        onItemSelected(item)
                        expanded.value = false
                    }
                )
            }
        }
    }
}

/*private fun normalize(s: String): String {
    return s.lowercase(Locale.getDefault())
        .replace(Regex("[^a-z0-9\\s]"), " ")
        .replace(Regex("\\s+"), " ")
        .trim()
}*/

@Composable
fun ProductPickList(
    items: List<String>,
    cartItems: List<CartItem> // pass from ViewModel
) {
    // Precompute normalized cart product names once per cartItems change
    val normalizedCart = remember(cartItems) {
        cartItems.map { normalize(it.productName) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, name ->
            val normName = normalize(name)

            // Exact match only - check if the normalized name exists exactly in cart
            val isSelected = normalizedCart.any { cart ->
                cart == normName
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Count
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(0.2f)
                )

                // Name with strikethrough when matched
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        textDecoration = if (isSelected) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Read-only checkbox that reflects cart membership
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {},
                    enabled = true,
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2E7D32),
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        checkmarkColor = Color.White
                    )
                )
            }
        }
    }
}

// Your normalize function (make sure it's consistent)
private fun normalize(text: String): String {
    return text.trim().lowercase()
}
/*@Composable
fun ProductPickList(
    items: List<String>,
    cartItems: List<CartItem> // pass from ViewModel
) {
    // Precompute normalized cart product names once per cartItems change
    val normalizedCart = remember(cartItems) {
        cartItems.map { normalize(it.productName) }
    }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        itemsIndexed(items) { index, name ->
            val normName = normalize(name)
            val nameTokens = normName.split(" ").filter { it.isNotBlank() }

            // isSelected -> true if any cart item matches the checklist item
            val isSelected = normalizedCart.any { cart ->
                // 1) whole-phrase word-boundary match: "green apple" in "green apple xyz"
                val phraseMatch = Regex("\\b${Regex.escape(normName)}\\b").containsMatchIn(cart)

                // 2) any token matches as whole word OR token-prefix (covers plural/suffix)
                val tokenMatch = nameTokens.any { token ->
                    val wholeWord = Regex("\\b${Regex.escape(token)}\\b").containsMatchIn(cart)
                    val prefix = cart.split(" ").any { cartToken ->
                        cartToken.startsWith(token)
                    }
                    wholeWord || prefix
                }

                phraseMatch || tokenMatch
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Count
                Text(
                    text = "${index + 1}.",
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.primary
                    ),
                    modifier = Modifier.weight(0.2f)
                )

                // Name with strikethrough when matched
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                        textDecoration = if (isSelected) TextDecoration.LineThrough else TextDecoration.None,
                        color = if (isSelected) Color(0xFF2E7D32) else MaterialTheme.colorScheme.primary // use your green
                    ),
                    modifier = Modifier.weight(1f)
                )

                // Read-only checkbox that reflects cart membership
                Checkbox(
                    checked = isSelected,
                    onCheckedChange = {}, // no-op (keeps it controlled by cartDataList)
                    enabled = true, // ✅ keep it enabled so green shows
                    colors = CheckboxDefaults.colors(
                        checkedColor = Color(0xFF2E7D32), // your green
                        uncheckedColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        checkmarkColor = Color.White
                    )
                )
            }
        }
    }
}*/

@androidx.annotation.OptIn(ExperimentalGetImage::class)
@Composable
fun BarcodeScannerDialog(
    onDismiss: () -> Unit,
    onBarcodeScanned: (String) -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    var previewView = remember { mutableStateOf<PreviewView?>(null) }

    var isProcessing = remember { mutableStateOf(false) }
    var lastProcessedBarcode = remember { mutableStateOf("") }
    var lastProcessedTime = remember { mutableStateOf(0L) }
    val debounceTime = 1500L // 1.5 seconds between scans

    Dialog(onDismissRequest = {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            cameraProvider.unbindAll()
        }, ContextCompat.getMainExecutor(context))
        onDismiss()
    }) {
        Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFFE3F2FD),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(16.dp)
            ) {

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(390.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color.Black)
                ) {
                    AndroidView(
                        modifier = Modifier.fillMaxSize(),
                        factory = { ctx ->
                            PreviewView(ctx).also { pv ->
                                previewView.value = pv

                                val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)

                                cameraProviderFuture.addListener({
                                    val cameraProvider = cameraProviderFuture.get()
                                    val preview = Preview.Builder().build().also {
                                        it.setSurfaceProvider(pv.surfaceProvider)
                                    }

                                    val scanner = BarcodeScanning.getClient()
                                    val analysis = ImageAnalysis.Builder()
                                        .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                        .build()
                                        .also {
                                            it.setAnalyzer(ContextCompat.getMainExecutor(ctx)) { imageProxy ->
                                                val mediaImage = imageProxy.image
                                                if (mediaImage != null && !isProcessing.value) {
                                                    val image = InputImage.fromMediaImage(
                                                        mediaImage,
                                                        imageProxy.imageInfo.rotationDegrees
                                                    )
                                                    scanner.process(image)
                                                        .addOnSuccessListener { barcodes ->
                                                            barcodes.firstOrNull()?.rawValue?.let { code ->
                                                                val currentTime =
                                                                    System.currentTimeMillis()

                                                                // Allow same product scanning after debounce time
                                                                if (code != lastProcessedBarcode.value ||
                                                                    currentTime - lastProcessedTime.value > debounceTime
                                                                ) {

                                                                    isProcessing.value = true
                                                                    lastProcessedBarcode.value =
                                                                        code
                                                                    lastProcessedTime.value =
                                                                        currentTime

                                                                    scope.launch {
                                                                        // Call the callback
                                                                        onBarcodeScanned(code)

                                                                        // Wait for debounce time before allowing next scan
                                                                        delay(debounceTime)
                                                                        isProcessing.value = false
                                                                    }
                                                                }
                                                            }
                                                        }
                                                        .addOnCompleteListener {
                                                            imageProxy.close()
                                                        }
                                                        .addOnFailureListener {
                                                            imageProxy.close()
                                                            isProcessing.value = false
                                                        }
                                                } else {
                                                    imageProxy.close()
                                                }
                                            }
                                        }

                                    cameraProvider.unbindAll()
                                    cameraProvider.bindToLifecycle(
                                        lifecycleOwner,
                                        CameraSelector.DEFAULT_BACK_CAMERA,
                                        preview,
                                        analysis
                                    )
                                }, ContextCompat.getMainExecutor(ctx))
                            }
                        }
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.TopCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Black.copy(alpha = 0.52f),
                                        Color.Transparent
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(56.dp)
                            .align(Alignment.BottomCenter)
                            .background(
                                Brush.verticalGradient(
                                    colors = listOf(
                                        Color.Transparent,
                                        Color.Black.copy(alpha = 0.52f)
                                    )
                                )
                            )
                    )

                    Box(
                        modifier = Modifier
                            .height(2.dp)
                            .fillMaxWidth(0.8f)
                            .align(Alignment.Center)
                            .background(Color.White.copy(alpha = 0.24f))
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Button(
                    onClick = {
                        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                        cameraProviderFuture.addListener({
                            val cameraProvider = cameraProviderFuture.get()
                            cameraProvider.unbindAll()
                            isProcessing.value = false
                        }, ContextCompat.getMainExecutor(context))
                        onDismiss()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                    shape = RoundedCornerShape(50)
                ) {
                    Text("Close Scanner", color = Color.White)
                }
            }
        }
    }
}


@Composable
fun ProductPriceAlert(
    viewModel: HomeViewModel,
    onDismiss: () -> Unit
) {
    val priceInfo by viewModel.priceDetails.collectAsState()
    val productInfo by viewModel.productInfo.collectAsState()
    var isAdding = remember { mutableStateOf(false) }
    if (productInfo != null && priceInfo != null) {
        var lastClickTime = 0L
        val clickDebounceTime = 300L
        AlertDialog(
            onDismissRequest = { onDismiss() },
            confirmButton = {
            },
            title = null,
            text = {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Product Content in Row (left image, right info)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Product Image (left side)
                        AsyncImage(
                            model = productInfo!!.imageUrl,
                            contentDescription = productInfo!!.productName,
                            modifier = Modifier
                                .size(90.dp)
                                .clip(RoundedCornerShape(12.dp)),
                            placeholder = painterResource(R.drawable.ic_no_product_image),
                            error = painterResource(R.drawable.ic_no_product_image),
                            contentScale = ContentScale.Crop
                        )

                        Spacer(Modifier.width(8.dp))

                        // Product Details (right side)
                        Column(
                            modifier = Modifier.weight(1f),
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text(
                                text = productInfo!!.productName,
                                style = MaterialTheme.typography.titleLarge.copy(
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    color = MaterialTheme.colorScheme.primary
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(4.dp))

                            Text(
                                text = "SKU: ${productInfo!!.barcode}",
                                style = MaterialTheme.typography.bodyMedium.copy(color = Color.Gray),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )

                            Spacer(Modifier.height(8.dp))

                            if (priceInfo!!.price != priceInfo!!.originalPrice) {
                                Text(
                                    text = "${Constants.currencySymbol} %.2f".format(
                                        priceInfo!!.originalPrice ?: 0.0
                                    ),
                                    style = MaterialTheme.typography.headlineMedium.copy(
                                        color = Color.Gray,
                                        fontWeight = FontWeight.Medium,
                                        fontSize = 10.sp,
                                        textDecoration = TextDecoration.LineThrough,
                                    ),
                                    textAlign = TextAlign.Start,
                                    modifier = Modifier.fillMaxWidth()
                                )
                            }

                            Text(
                                text = "${Constants.currencySymbol} %.2f".format(
                                    priceInfo!!.price ?: 0.0
                                ),
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = MaterialTheme.colorScheme.primary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                ),
                                textAlign = TextAlign.Start,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }

                    Spacer(Modifier.height(10.dp))

                    // Buttons Row at the bottom - same style, different colors
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        // Cancel Button (Red background)
                        Button(
                            onClick = { onDismiss() },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color.Red,
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(end = 8.dp)
                        ) {
                            Text(
                                text = "Cancel",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                ),
                            )
                        }

                        // Add To Cart Button (Green background)
                        Button(
                            onClick = {
                                if (!isAdding.value) {
                                    isAdding.value = true
                                    val currentTime = System.currentTimeMillis()
                                    if (currentTime - lastClickTime > clickDebounceTime) {
                                        lastClickTime = currentTime
                                        viewModel.addProductToShoppingCart(productInfo!!.barcode, 1)
                                        onDismiss()
                                    }
                                }
                            },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = Color(0xFF4CAF50),
                                contentColor = Color.White
                            ),
                            modifier = Modifier
                                .weight(1f)
                                .padding(start = 8.dp)
                        ) {
                            Text(
                                text = "Add To Cart",
                                style = MaterialTheme.typography.headlineMedium.copy(
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 10.sp,
                                )
                            )
                        }
                    }
                }
            },
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .padding(8.dp)
                .fillMaxWidth(),
            containerColor = Color.White
        )
    }
}

@Composable
fun DeleteProductDialog(
    productName: String,
    productCode: String,
    oldPrice: String,
    newPrice: String,
    imageRes: String,
    onRemove: () -> Unit,
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.background(Color.White)) {

                // Top banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.colorPrimary))
                        .padding(horizontal = 8.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cart),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(15.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Are you sure want to delete this product?",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image + Sale badge
                    Box(modifier = Modifier.size(85.dp)) {
                        if (imageRes.isNotEmpty()) {
                            AsyncImage(
                                model = imageRes,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = painterResource(R.drawable.ic_no_product_image),
                                error = painterResource(R.drawable.ic_no_product_image)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_no_product_image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }

                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    // Product info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = productName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.colorPrimary)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "SKU: $productCode",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = oldPrice,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = newPrice,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.colorPrimary)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Action buttons (Cancel + Remove)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 25.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp) // spacing between buttons
                ) {
                    // Cancel button
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = colorResource(R.color.colorPrimary)
                        ),
                        border = BorderStroke(2.dp, colorResource(R.color.colorPrimary))
                    ) {
                        Text(
                            text = "CANCEL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.colorPrimary)
                            )
                        )
                    }

                    // Remove button
                    Button(
                        onClick = onRemove,
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorRed)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "DELETE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun EditProductDialog(
    productName: String,
    productCode: String,
    oldPrice: String,
    newPrice: String,
    imageRes: String,
    currentQuantity: Int,
    onEdit: (Int) -> Unit, // pass selected quantity
    onDismiss: () -> Unit
) {
    var quantity = remember { mutableStateOf(currentQuantity) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.95f)
        ) {
            Column(modifier = Modifier.background(Color.White)) {

                // Top banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(colorResource(R.color.colorPrimary))
                        .padding(horizontal = 8.dp, vertical = 9.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.ic_cart),
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = "Edit Product",
                            color = Color.White,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Product area
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 9.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Image
                    Box(modifier = Modifier.size(85.dp)) {
                        if (imageRes.isNotEmpty()) {
                            AsyncImage(
                                model = imageRes,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize(),
                                placeholder = painterResource(R.drawable.ic_no_product_image),
                                error = painterResource(R.drawable.ic_no_product_image)
                            )
                        } else {
                            Image(
                                painter = painterResource(id = R.drawable.ic_no_product_image),
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(7.dp))

                    // Product info
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = productName,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = colorResource(R.color.colorPrimary)
                            )
                        )

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = "SKU: $productCode",
                            style = MaterialTheme.typography.bodySmall.copy(color = Color.Gray)
                        )

                        Spacer(modifier = Modifier.height(5.dp))

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = oldPrice,
                                style = MaterialTheme.typography.bodyMedium.copy(
                                    color = Color.Gray,
                                    textDecoration = TextDecoration.LineThrough
                                )
                            )
                            Spacer(modifier = Modifier.width(5.dp))
                            Text(
                                text = newPrice,
                                style = MaterialTheme.typography.titleMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = colorResource(R.color.colorPrimary)
                                )
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // Quantity selector
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    IconButton(
                        onClick = { if (quantity.value > 1) quantity.value-- },
                        modifier = Modifier
                            .size(40.dp)
                            .background(colorResource(R.color.colorRed), CircleShape)
                    ) {
                        Icon(
                            painter = painterResource(R.drawable.outline_remove_24),
                            contentDescription = "Decrease",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )

                    }

                    Text(
                        text = quantity.value.toString(),
                        modifier = Modifier.padding(horizontal = 10.dp),
                        style = MaterialTheme.typography.titleLarge.copy(
                            fontWeight = FontWeight.Bold,
                            color = colorResource(R.color.colorPrimary)
                        )
                    )

                    IconButton(
                        onClick = { quantity.value++ },
                        modifier = Modifier
                            .size(25.dp)
                            .background(colorResource(R.color.colorPrimary), CircleShape)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase",
                            tint = Color.White,
                            modifier = Modifier.size(10.dp)
                        )

                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // Action buttons
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 15.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = colorResource(R.color.colorPrimary)
                        ),
                        border = BorderStroke(2.dp, colorResource(R.color.colorPrimary))
                    ) {
                        Text(
                            text = "CANCEL",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }

                    Button(
                        onClick = { onEdit(quantity.value) },
                        colors = ButtonDefaults.buttonColors(containerColor = colorResource(R.color.colorGreen)),
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp),
                        shape = RoundedCornerShape(50)
                    ) {
                        Text(
                            text = "UPDATE",
                            style = MaterialTheme.typography.titleMedium.copy(
                                color = Color.White,
                                fontWeight = FontWeight.Bold
                            )
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun ManualBarcodeEntryDialog(
    onProceed: (String) -> Unit,
    onCancel: () -> Unit,
    onDismiss: () -> Unit
) {
    var barcode = remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth(0.95f),
            colors = CardDefaults.cardColors(containerColor = Color.White)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Title
                Text(
                    text = "Enter bar code manually",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color(0xFF0D47A1),
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(20.dp))

                // TextField
                TextField(
                    value = barcode.value,
                    onValueChange = { barcode.value = it },
                    placeholder = { Text("Enter barcode") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color(0xFFF2F2F2),
                        unfocusedContainerColor = Color(0xFFF2F2F2),
                        disabledContainerColor = Color(0xFFF2F2F2),
                        errorContainerColor = Color(0xFFF2F2F2),
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        disabledIndicatorColor = Color.Transparent,
                        errorIndicatorColor = Color.Transparent
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Buttons Row
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    // Cancel Button
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(50),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp),
                        colors = ButtonDefaults.outlinedButtonColors(
                            containerColor = Color.White,
                            contentColor = colorResource(R.color.colorRed)
                        ),
                        border = BorderStroke(2.dp, colorResource(R.color.colorRed))
                    ) {
                        Text(
                            text = "Cancel",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }

                    // Proceed Button
                    Button(
                        onClick = { onProceed(barcode.value) },
                        shape = RoundedCornerShape(50),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF0D47A1)
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text(
                            text = "Proceed",
                            color = Color.White,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun DrawerContent(
    appMode: AppMode,
    onTransActionSelected: () -> Unit,
    onAppModeUpdated: (AppMode) -> Unit,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(300.dp)
            .background(MaterialTheme.colorScheme.surface),
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(Modifier.height(50.dp))
        /* Image(
             painter = painterResource(id = R.drawable.ic_merchant_logo),
             contentDescription = "Ad Banner",
             contentScale = ContentScale.Crop,
             modifier = Modifier.fillMaxWidth().height(70.dp)
         )

         Divider()*/

        Spacer(Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTransActionSelected() }
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                painter = painterResource(id = R.drawable.transaction),
                contentDescription = "transaction",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "View Transaction",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                )
            )
        }
        Divider()
        /*Spacer(modifier = Modifier.height(20.dp))
        Text(modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 5.dp),
            text = "App Mode",
            style = MaterialTheme.typography.bodyLarge.copy(
                color = MaterialTheme.colorScheme.onSurface
            )
        )
        SingleSelectCheckboxes(
            onSelectionChanged = onAppModeUpdated,
            selected = appMode
        )
        Divider()*/
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            /* Icon(
                 painter = painterResource(id = icon),
                 contentDescription = text,
                 tint = MaterialTheme.colorScheme.primary,
                 modifier = Modifier.size(24.dp)
             )*/
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Show Price Checker",
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = MaterialTheme.colorScheme.onSurface
                ),
                modifier = Modifier.weight(1f) // 👈 pushes switch to the right
            )
            Switch(
                checked = isChecked,
                onCheckedChange = onCheckedChange
            )
        }

    }
}

@Composable
fun SingleSelectCheckboxes(
    options: List<Pair<AppMode, String>> = listOf(
        AppMode.EzyCartPicker to "EzyCartPicker",
        AppMode.EzyLite to "EzyLite"
    ),
    selected: AppMode = AppMode.EzyLite,
    modifier: Modifier = Modifier,
    onSelectionChanged: (AppMode) -> Unit = {}
) {
    var selected = remember { mutableStateOf(selected) }

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        options.forEach { (mode, label) ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .clickable {
                        selected.value = mode
                        onSelectionChanged(mode)
                    }
                    .padding(4.dp)
            ) {
                Checkbox(
                    checked = selected.value == mode,
                    onCheckedChange = {
                        selected.value = mode
                        onSelectionChanged(mode)
                    },
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        checkmarkColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = label, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}


@SuppressLint("SetJavaScriptEnabled")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WebViewScreen(url: String, navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sales Report") },
                navigationIcon = {
                    IconButton(
                        onClick = {
                            navController.popBackStack()
                        }
                    ) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        AndroidView(
            factory = { context ->
                WebView(context).apply {
                    webViewClient = WebViewClient()
                    settings.javaScriptEnabled = true
                    settings.domStorageEnabled = true
                    loadUrl(url)
                }
            },
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        )
    }

    // Handle back press
    BackPressHandler {
        navController.popBackStack()
    }
}

@Composable
fun BackPressHandler(onBackPressed: () -> Unit) {
    val backPressedDispatcher = LocalOnBackPressedDispatcherOwner.current?.onBackPressedDispatcher
    val currentOnBackPressed by rememberUpdatedState(onBackPressed)

    DisposableEffect(backPressedDispatcher) {
        val callback = object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                currentOnBackPressed()
            }
        }

        backPressedDispatcher?.addCallback(callback)

        onDispose {
            callback.remove()
        }
    }
}

@Composable
fun QRPaymentAlert(

    qrCodeUrl: String,
    paymentAmount: String,
    onDismiss: () -> Unit
) {
    var isLoading = remember { mutableStateOf(true) }
    var loadingError = remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = { /* Do nothing - prevent closing on outside touch */ },
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .width(500.dp) // Fixed width
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(16.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Text(
                text = "Scan to Pay",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 16.dp)
            )

            // Amount Display
            Text(
                text = "Amount to Pay",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.Gray,
                modifier = Modifier.padding(bottom = 4.dp)
            )

            Text(
                text = paymentAmount,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = Color.Black,
                modifier = Modifier.padding(bottom = 20.dp)
            )

            // QR Code
            Box(
                modifier = Modifier
                    .size(300.dp) // Adjusted to fit in 280dp container
                    .background(Color.White),
                contentAlignment = Alignment.Center
            ) {
                if (isLoading.value) {
                    CircularProgressIndicator(
                        color = Color.Black,
                        modifier = Modifier.size(32.dp)
                    )
                }

                if (loadingError.value) {
                    Text(
                        text = "Failed to load QR",
                        color = Color.Gray,
                        fontSize = 12.sp
                    )
                }

                AndroidView(
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = false
                            settings.loadWithOverviewMode = true
                            settings.useWideViewPort = true
                            settings.builtInZoomControls = false
                            settings.displayZoomControls = false

                            webViewClient = object : WebViewClient() {
                                override fun onPageStarted(
                                    view: WebView?,
                                    url: String?,
                                    favicon: android.graphics.Bitmap?
                                ) {
                                    isLoading.value = true
                                    loadingError.value = false
                                }

                                override fun onPageFinished(view: WebView?, url: String?) {
                                    isLoading.value = false
                                }

                                override fun onReceivedError(
                                    view: WebView?,
                                    errorCode: Int,
                                    description: String?,
                                    failingUrl: String?
                                ) {
                                    isLoading.value = false
                                    loadingError.value = true
                                }
                            }
                        }
                    },
                    modifier = Modifier.size(300.dp),
                    update = { webView ->
                        if (webView.url != qrCodeUrl) {
                            webView.loadUrl(qrCodeUrl)
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Cancel Button
            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color.Red,
                    contentColor = Color.White
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text(
                    text = "Cancel Payment",
                    fontWeight = FontWeight.Medium,
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun QrPaymentAlert(
    amount: String,
    qrPainter: Painter, // QR code image painter
    onDismiss: () -> Unit
) {
    Dialog(onDismissRequest = { onDismiss() }) {
        Box(
            modifier = Modifier
                .fillMaxWidth(0.9f)
                .background(Color.White, shape = RoundedCornerShape(16.dp))
                .padding(16.dp)
        ) {
            // Top-right close button
            IconButton(
                onClick = onDismiss,
                modifier = Modifier.align(Alignment.TopEnd)
            ) {
                Icon(
                    imageVector = Icons.Default.Clear,
                    contentDescription = "Close",
                    tint = Color.Black
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.Center), // keep column centered
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(24.dp))
                /* Image(
                     painter = painterResource(id = R.drawable.id_duit_now_logo),
                     contentDescription = "Ad Banner",
                     modifier = Modifier.size(80.dp)
                 )*/

                Spacer(modifier = Modifier.height(10.dp))
                // QR Code Image
                Image(
                    painter = qrPainter,
                    contentDescription = "QR Code",
                    modifier = Modifier.size(200.dp)
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Amount
                Text(
                    text = "${Constants.currencySymbol} $amount",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun PaymentSuccessAlert(

    onSendReceipt: () -> Unit,
    onDismiss: () -> Unit
) {
    val timerComposition by rememberLottieComposition(LottieCompositionSpec.Asset("anim_success_3.json"))
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lottie Animation
            Box(
                modifier = Modifier.size(85.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = timerComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(60.dp)
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Success Text
            Text(
                text = "Payment Successful!",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF00C853), // Green color for success
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Your payment has been processed successfully",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 8.sp,
                color = Color.Gray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Send Receipt Button
            Button(
                onClick = onSendReceipt,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853), // Green color
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Send Receipt",
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

        }
    }
}

@Composable
fun PaymentFailureAlert(
    onRetry: () -> Unit,
    onDismiss: () -> Unit
) {
    val timerComposition by rememberLottieComposition(LottieCompositionSpec.Asset("anim_wrong.json"))
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(dismissOnBackPress = false, dismissOnClickOutside = false)
    ) {
        Column(
            modifier = Modifier
                .width(300.dp)
                .background(
                    color = Color.White,
                    shape = RoundedCornerShape(20.dp)
                )
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Lottie Animation
            Box(
                modifier = Modifier.size(85.dp),
                contentAlignment = Alignment.Center
            ) {
                LottieAnimation(
                    composition = timerComposition,
                    iterations = LottieConstants.IterateForever,
                    modifier = Modifier.size(60.dp)
                )

            }

            Spacer(modifier = Modifier.height(8.dp))

            // Success Text
            Text(
                text = "Payment Failed!",
                style = MaterialTheme.typography.titleLarge,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Red, // Green color for success
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Please Try Again!",
                style = MaterialTheme.typography.bodyMedium,
                fontSize = 10.sp,
                color = Color.DarkGray,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Send Receipt Button
            Button(
                onClick = onRetry,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF00C853), // Green color
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = "Retry Again",
                    fontWeight = FontWeight.Medium,
                    fontSize = 10.sp,
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

        }
    }
}

fun getFormattedPrice(price: Double): String {
    var formattedAmount = "0.0"

    try {
        formattedAmount = String.format("%.2f", price)
    } catch (e: Exception) {
        TODO("Not yet implemented")
    }

    return formattedAmount
}



