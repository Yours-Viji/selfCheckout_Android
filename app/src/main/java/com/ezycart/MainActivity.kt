package com.ezycart

import android.app.Application
import android.os.Bundle
import android.util.Log
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.ezycart.domain.usecase.LoadingManager
import com.ezycart.payment.nearpay.NearPayService
import com.ezycart.presentation.SplashViewModel
import com.ezycart.presentation.activation.ActivationScreen
import com.ezycart.presentation.common.components.CustomRationaleDialog
import com.ezycart.presentation.common.components.GlobalLoadingOverlay
import com.ezycart.presentation.common.data.Constants
import com.ezycart.presentation.home.HomeScreen
import com.ezycart.presentation.home.HomeViewModel
import com.ezycart.presentation.home.WebViewScreen
import com.ezycart.presentation.landing.LandingScreen
import com.ezycart.presentation.payment.PaymentSelectionScreen
import com.ezycart.services.usb.SensorSerialPortCommunication
import com.ezycart.services.usb.com.UsbLedManager
import com.ezycart.services.usb.com.WeightScaleManager
import com.meticha.permissions_compose.PermissionManagerConfig
import dagger.hilt.android.AndroidEntryPoint
import dagger.hilt.android.HiltAndroidApp
import io.nearpay.sdk.utils.enums.TransactionData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.net.URLDecoder
import java.net.URLEncoder
import javax.inject.Inject

@HiltAndroidApp
class MyApp : Application()

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @Inject
    lateinit var loadingManager: LoadingManager

    @Inject
    lateinit var nearPayService: NearPayService
    private val homeViewModel: HomeViewModel by viewModels()

    //private lateinit var ledManager: UsbLedManager
    //@Inject lateinit var ledManager: UsbLedManager
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        window.statusBarColor = getColor(R.color.colorPrimaryDark)

        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightStatusBars = false

        PermissionManagerConfig.setCustomRationaleUI { permission, onDismiss, onConfirm ->
            CustomRationaleDialog(
                description = permission.description,
                onDismiss = onDismiss,
                onConfirm = onConfirm
            )
        }
        WeightScaleManager.init(homeViewModel)
        WeightScaleManager.connect(this)
        //ledManager = UsbLedManager.getInstance(this)
        //  ledManager.connectAndPrepare()
        /* lifecycleScope.launch {
             SensorSerialPortCommunication.sensorMessage.collect { data ->
                 Log.i("--->>","LOG RECEIVED: $data")
                 Toast.makeText(application,"-->>$data", Toast.LENGTH_SHORT).show()
             }
         }*/
        //enableEdgeToEdge()
        setContent {

            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val splashViewModel: SplashViewModel = hiltViewModel()
                    //val scannerViewModel: ScannerViewModel = hiltViewModel()
                    val isActivated = splashViewModel.isDeviceActivated.collectAsState()
                    splashViewModel.getDeviceId(this)

                    /* BarcodeScannerListener(
                         onBarcodeScanned = { code ->
                             scannerViewModel.onScanned(code)
                         }
                     )*/

                    when (isActivated.value) {
                        null -> {
                            // Optional loading UI
                            Text("Checking device activation...")
                        }

                        else -> {
                            val startDestination =
                                if (isActivated.value == true) "landing" else "activation"

                            NavHost(navController, startDestination = startDestination) {
                                composable("activation") {

                                    ActivationScreen(
                                        onLoginSuccess = {
                                            navController.navigate("landing") {
                                                popUpTo("activation") { inclusive = true }
                                            }
                                        }
                                    )
                                }
                                composable("landing") {
                                    val context = LocalContext.current
                                    LaunchedEffect(Unit) {
                                        try {
                                            WeightScaleManager.initOnce(homeViewModel)
                                            WeightScaleManager.connectSafe(context)

                                        } catch (e: Exception) {
                                        }
                                    }
                                    LandingScreen(goToHomeScreen = {
                                        try {
                                            homeViewModel.requestTotalWeightFromLoadCell()
                                            //homeViewModel.switchStartShoppingLed()
                                        } catch (e: Exception) {
                                        }
                                        navController.navigate("home") {
                                            popUpTo("landing") { inclusive = true }
                                        }

                                    })
                                    /* LoginScreen(

                                         onThemeChange = {
                                             // 🔹 Handle theme change
                                             //  Toast.makeText(this, "Theme change clicked", Toast.LENGTH_SHORT).show()
                                         },
                                         onLanguageChange = {
                                             // 🔹 Handle language change
                                             // Toast.makeText(this, "Language change clicked", Toast.LENGTH_SHORT).show()
                                         },
                                         onLoginSuccess = {
                                             navController.navigate("home") {
                                                 popUpTo("login") { inclusive = true }
                                             }
                                         }
                                     )*/
                                }
                                composable("home") {
                                    HomeScreen(
                                        homeViewModel,
                                        onThemeChange = {
                                            // 🔹 Handle theme change
                                            //  Toast.makeText(this, "Theme change clicked", Toast.LENGTH_SHORT).show()
                                        },
                                        onPaymentInitialize = {
                                            //  nearPayService.initializeSdk(this@MainActivity)
                                            //  nearPayService.paymentSdkSetUp()
                                        },
                                        makeNearPayment = { reference, amount, nearPaymentListener ->
                                            // nearPayService.initTapOnPayTransaction(this@MainActivity,"1234","100","test@gmail.com","",listener = nearPaymentListener)
                                        },

                                        onLogout = {
                                            try {
                                                homeViewModel.resetLoadCell()
                                            } catch (e: Exception) {
                                            }
                                            navController.navigate("landing") {
                                                popUpTo("home") {
                                                    inclusive = true
                                                } // remove home from back stack
                                            }
                                            lifecycleScope.launch {
                                                splashViewModel.clearUserPreference()
                                            }
                                        },
                                        goToPaymentScreen = {
                                            try {
                                                // homeViewModel.switchPaymentLed()
                                            } catch (e: Exception) {
                                            }
                                            /*navController.navigate("payment") {
                                                popUpTo("home") { inclusive = true }
                                            }*/
                                            navController.navigate("payment")
                                        },
                                        onTransactionCalled = {
                                            navController.navigateToWebView(Constants.EZY_LITE_TRANSACTION_URL)
                                        }
                                    )
                                }
                                composable("payment") { backStackEntry ->

                                    val count =
                                        homeViewModel.cartCount.collectAsStateWithLifecycle()
                                    val shoppingCartInfo =
                                        homeViewModel.shoppingCartInfo.collectAsStateWithLifecycle()
                                    BackHandler {
                                        navController.popBackStack()
                                    }
                                    PaymentSelectionScreen(
                                        cartCount = count.value,
                                        shoppingCartInfo = shoppingCartInfo.value,
                                        onBackClick = { navController.popBackStack() }
                                    )
                                }
                                composable("webview/{url}") { backStackEntry ->
                                    val encodedUrl =
                                        backStackEntry.arguments?.getString("url") ?: ""
                                    val url = URLDecoder.decode(encodedUrl, "UTF-8")
                                    WebViewScreen(
                                        url = url,
                                        navController = navController
                                    )
                                }
                            }

                        }
                    }
                    GlobalLoadingOverlay(loadingManager)
                }
            }
        }
    }

    fun NavController.navigateToWebView(url: String) {
        try {
            val encodedUrl = URLEncoder.encode(url, "UTF-8")
            navigate("webview/$encodedUrl")
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback: navigate with empty URL or show error
            navigate("webview/error")
        }
    }

    /* override fun onPaymentSuccess(transactionData: TransactionData) {
         runOnUiThread {
             nearPayResult(true, transactionData)
         }
     }

     override fun onPaymentFailed(error: String) {
         runOnUiThread {
             // Handle payment failure
             Toast.makeText(this, "Payment failed: $error", Toast.LENGTH_SHORT).show()
         }
     }*/
    fun nearPayResult(status: Boolean, transactionData: TransactionData) {
        CoroutineScope(Dispatchers.Main).launch {
            delay(3000)
            lifecycleScope.launch(Dispatchers.Main) {
                try {

                } catch (_: Exception) {
                }
            }
        }
    }
}



