package com.ezycart

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.hilt.navigation.compose.hiltViewModel
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
import com.ezycart.services.usb.AppScenario
import com.ezycart.services.usb.LedSerialConnection
import com.ezycart.services.usb.LoadCellSerialPort
import com.ezycart.services.usb.WeightScaleManager
import com.itbizflow.ecrsdkhelper.EcrSdkHelper
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
import java.util.Locale
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
        if (!EcrSdkHelper.isInitialized()) {
            EcrSdkHelper.initializeSdk(this)
        }
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
                                    LandingScreen(homeViewModel,goToHomeScreen = {
                                        try {
                                            homeViewModel.requestTotalWeightFromLoadCell()
                                            //homeViewModel.switchOnAllLed()
                                        } catch (e: Exception) {
                                        }
                                        navController.navigate("home") {
                                            popUpTo("landing") { inclusive = true }
                                        }

                                    },
                                        reConnectLoadCell = {
                                            try {
                                                lifecycleScope.launch {
                                                    repeat(2) { attempt ->
                                                        try {
                                                            WeightScaleManager.initOnce(homeViewModel)
                                                            WeightScaleManager.connectSafe(context)
                                                            return@launch // success → exit
                                                        } catch (e: Exception) {
                                                            if (attempt == 1) {
                                                                e.printStackTrace()
                                                            }
                                                        }
                                                    }

                                                    delay(100)
                                                    LoadCellSerialPort.sendMessageToWeightScale("2\r\n")
                                                }
                                            } catch (e: Exception) {
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
                                               LedSerialConnection.setScenario(AppScenario.ALL_OFF)
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
                                               // LedSerialConnection.setScenario(AppScenario.PAYMENT_SUCCESS)
                                            } catch (e: Exception) {
                                            }
                                            /*navController.navigate("payment") {
                                                popUpTo("home") { inclusive = true }
                                            }*/
                                           // navController.navigate("payment")
                                        },
                                        onTransactionCalled = {
                                            navController.navigateToWebView(Constants.EZY_LITE_TRANSACTION_URL)
                                        },
                                        reSetLanguage={
                                            updateLanguage(this@MainActivity,"en")
                                        }
                                    )
                                }
                               /* composable("payment") { backStackEntry ->

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
                                }*/
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
    fun updateLanguage(activity: Activity, selectedLanguage:String){

        val langCode = languageToCode(selectedLanguage)
        updateAppLanguage(activity, langCode)
         activity.recreate()
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



