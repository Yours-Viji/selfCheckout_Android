package com.ezycart.presentation.login

import android.Manifest
import android.content.Context
import android.content.pm.ActivityInfo
import android.widget.ImageView
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.input.key.utf16CodePoint
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.ezycart.R
import com.ezycart.presentation.activation.LockScreenOrientation
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.pranavpandey.android.dynamic.toasts.DynamicToast

@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onThemeChange: () -> Unit,
    onLanguageChange: () -> Unit,
    onLoginSuccess: () -> Unit,

) {

    val state by viewModel.stateFlow.collectAsStateWithLifecycle()
    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    var scanBuffer = remember { mutableStateOf("") }

    val context = LocalContext.current

    LockScreenOrientation(context,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    LaunchedEffect(cameraPermissionState.status) {
        when {
            !cameraPermissionState.status.isGranted ->{
                cameraPermissionState.launchPermissionRequest()
            }

        }
    }
    LaunchedEffect(state.isLoginSuccessful) {
        if (state.isLoginSuccessful) {
            onLoginSuccess()
        }
    }

        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxHeight(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_retailetics),
                    contentDescription = "Logo",
                    modifier = Modifier.size(180.dp, 50.dp)
                )
                Spacer(modifier = Modifier.height(30.dp))
                LoginForm(viewModel,state,context)
            }

        }


}

@Composable
fun LoginForm(
    viewModel: LoginViewModel,

    state: LoginState,
    context: Context,
) {

    Column(verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally) {

        Text(fontWeight = FontWeight.Bold,
            text = "Sign In With Employee PIN",
            fontSize =  20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(30.dp))
        Text(
            text = "Enter Your Employee Pin Here",
            fontSize = 16.sp,
            color = Color.Gray
        )
        Spacer(modifier = Modifier.height(10.dp))
        OtpPinView(
            otpText = state.employeePin,
            onOtpTextChange  = viewModel::onEmployeePinChange,
            otpLength = 5
        )
        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                //viewModel.clearpref()
                //scannerViewModel.onScanned("57024")
                if (state.employeePin.length == 5) {
                    viewModel.login()
                } else {
                    DynamicToast.makeError(context, "Please enter a valid 5-digit Employee PIN").show()
                }
            },
            modifier = Modifier.fillMaxWidth( 0.8f).height(50.dp)
        ) {
            Text(fontWeight = FontWeight.Bold,
                text = "Sign In",
                fontSize = 22.sp ,
                color = Color.White
            )
        }
        state.error?.let { error ->
            Text(
                text = error,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(14.dp)
            )
        }
        Spacer(modifier = Modifier.height(40.dp))
        Text(fontWeight = FontWeight.Bold,
            text = "(OR)",
            fontSize = 20.sp,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(20.dp))
        Text(fontWeight = FontWeight.Bold,
            text = "Scan your employee Barcode to Sing In",
            fontSize =  25.sp ,
            color = MaterialTheme.colorScheme.primary

        )
    }
}
@Composable
fun OtpPinView(
    modifier: Modifier = Modifier,
    otpText: String,
    onOtpTextChange: (String) -> Unit,
    otpLength: Int = 5
) {
    // Make textFieldValue a mutable state so Compose can recompose correctly
    val textFieldValue = remember(otpText) {
        mutableStateOf(
            TextFieldValue(
                text = otpText,
                selection = TextRange(otpText.length)
            )
        )
    }

    Box(
        modifier = modifier.fillMaxWidth().height(64.dp)
    ) {
        BasicTextField(
            value = textFieldValue.value,
            onValueChange = { newValue ->
                if (newValue.text.length <= otpLength && newValue.text.all { it.isDigit() }) {
                    textFieldValue.value = newValue
                    onOtpTextChange(newValue.text)
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier
                .matchParentSize()
                .background(Color.Transparent),
            singleLine = true,
            cursorBrush = SolidColor(Color.Transparent), // ✅ must use SolidColor
            decorationBox = {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp), // better than SpaceAround
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.wrapContentWidth() // don’t stretch full width
                ) {
                    repeat(otpLength) { index ->
                        val char = if (index < otpText.length) otpText[index] else ' '
                        OtpBox(
                            char = char,
                            isFocused = index == otpText.length
                        )
                    }
                }
            }
        )
    }
}


/**
 * A composable function for a single OTP input box.
 *
 * @param char The character to display in the box.
 * @param isFocused A boolean to indicate if this box is currently focused.
 */
@Composable
fun OtpBox(
    char: Char,
    isFocused: Boolean
) {
    val boxColor = if (isFocused) {
        Color.Transparent
    } else {
        Color.Transparent
    }

    val borderColor = if (isFocused) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Gray
    }

    Box(
        modifier = Modifier
            .size(50.dp)
            .background(boxColor)
            .border(2.dp, borderColor)
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = char.toString(),
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            color = MaterialTheme.colorScheme.primary,
            textAlign = TextAlign.Center
        )
    }
}
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageDropdown() {
    var expanded = remember { mutableStateOf(false) }
    var selectedLanguage = remember { mutableStateOf("English") }

    Box(
        modifier = Modifier
            .size(200.dp, 53.dp)
    ) {
        AndroidView(
            factory = { ctx ->
                ImageView(ctx).apply {
                    setImageResource(R.drawable.language_toggle_background_light)
                    scaleType = ImageView.ScaleType.FIT_XY
                }
            },
            modifier = Modifier.matchParentSize()
        )

        ExposedDropdownMenuBox(
            expanded = expanded.value,
            onExpandedChange = { expanded.value = !expanded.value },
            modifier = Modifier.width(200.dp)
        ) {
            OutlinedTextField(
                value = selectedLanguage.value,
                onValueChange = {},
                readOnly = true,
                modifier = Modifier
                    .menuAnchor()
                    .height(53.dp),

                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    errorContainerColor = Color.Transparent,
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent
                ),
                textStyle = LocalTextStyle.current.copy(
                    fontSize = 15.sp,
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.primary,
                ),

                trailingIcon = {}
            )

            ExposedDropdownMenu(
                expanded = expanded.value,
                onDismissRequest = { expanded.value = false }
            ) {
                listOf("English", "Malay").forEach { lang ->
                    DropdownMenuItem(
                        text = { Text( text = lang,
                            fontWeight = FontWeight.Bold,
                            fontSize = 17.sp,
                            color = MaterialTheme.colorScheme.primary,
                            textAlign = TextAlign.Center) },
                        onClick = {
                            selectedLanguage.value = lang
                            expanded.value = false
                        }
                    )
                }
            }
        }
    }

    }

