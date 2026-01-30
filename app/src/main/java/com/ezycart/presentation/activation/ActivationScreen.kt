package com.ezycart.presentation.activation

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.ActivityInfo
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.ezycart.R
import com.ezycart.domain.model.AppMode
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.meticha.permissions_compose.AppPermission
import com.meticha.permissions_compose.rememberAppPermissionState
import com.pranavpandey.android.dynamic.toasts.DynamicToast
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch


@OptIn(ExperimentalPermissionsApi::class)
@Composable
fun ActivationScreen(
    viewModel: ActivationViewModel = hiltViewModel(),

   // sensorSerialPortViewModel: SensorSerialPortViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit
) {
    val permissions = rememberAppPermissionState(
        permissions = listOf(
            AppPermission(
                permission = Manifest.permission.CAMERA,
                description = "Camera access is needed to take photos. Please grant this permission.",
                isRequired = true
            ),
        )
    )
    val context = LocalContext.current

    LockScreenOrientation(context,ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)

    val cameraPermissionState = rememberPermissionState(Manifest.permission.CAMERA)
    val state by viewModel.stateFlow.collectAsStateWithLifecycle()

    val isActivated = viewModel.isDeviceActivated.collectAsState()
   // val loadCellState by sensorSerialPortViewModel.usbData.collectAsStateWithLifecycle()
   // val weightData by sensorSerialPortViewModel.connectionLog.collectAsStateWithLifecycle()
    //val weightState by sensorSerialPortViewModel.weightState.collectAsStateWithLifecycle()
    LaunchedEffect(cameraPermissionState.status) {
        when {
            !cameraPermissionState.status.isGranted ->{
                cameraPermissionState.launchPermissionRequest()
            }

        }
    }
    LaunchedEffect(isActivated.value) {
        if (isActivated.value == false) {
           // viewModel.getDeviceInfo()
        }
    }

    LaunchedEffect(state.isActivationSuccessful) {
        if (state.isActivationSuccessful) {
            onLoginSuccess()
        }
    }
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center // Centers the Column on Tablet
    ) {
       /* Text(
            text = "${weightData}",
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )*/
        // 2. The Main Form Column
        Column(
            modifier = Modifier
                // ADAPTIVE FIX START:
                // This replaces the fixed '350.dp' padding.
                // It fills width on Mobile, but stops growing at 480dp on Tablet.
                .widthIn(max = 480.dp)
                .fillMaxWidth(),
            // ADAPTIVE FIX END
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_ezycart_lite_icon),
                contentDescription = "EzyCart Logo",
                modifier = Modifier.size(width = 250.dp, height = 80.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            OutlinedTextField(
                value = state.activationCode,
                onValueChange = { viewModel.onActivationCodeChange(it) },
                label = { Text("Activation Code") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp)
            )

            Spacer(modifier = Modifier.height(20.dp))

            if (state.isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(16.dp))
            } else {
                Button(
                    onClick = {
                        if (state.activationCode.isEmpty()) {
                            DynamicToast.makeError(context, "Please enter a valid Activation Code").show()
                        } else {
                            viewModel.activateDevice()
                            permissions.requestPermission()
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFE53935),
                        contentColor = Color.White
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(text = "Activate", fontSize = 18.sp)
                }
            }

            state.error?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(16.dp)
                )
            }
        }

        // 3. Footer "Powered By" (Stays at Bottom End)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(bottom = 20.dp, end = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Product Of",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Gray
            )
            Image(
                painter = painterResource(id = R.drawable.ic_retailetics),
                contentDescription = "Retailetics Logo",
                modifier = Modifier.size(width = 150.dp, height = 40.dp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OutletSelectionDropdown() {
    val outlets = listOf("1 Mont' Kiara", "Bangsar Village", "Sunway Giza", "Sierra Fresco")
    var expanded = remember { mutableStateOf(false) }
    var selectedOutlet = remember { mutableStateOf("Select Outlet") }

    ExposedDropdownMenuBox(
        expanded = expanded.value,
        onExpandedChange = { expanded.value = !expanded.value },
        modifier = Modifier.fillMaxWidth()
    ) {
        OutlinedTextField(
            modifier = Modifier
                .fillMaxWidth()
                .menuAnchor(),
            readOnly = true,
            value = selectedOutlet.value,
            onValueChange = {},
            label = { Text("Select Outlet") },
            trailingIcon = {
                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded.value)
            },
            shape = RoundedCornerShape(8.dp),
        )
        ExposedDropdownMenu(
            expanded = expanded.value,
            onDismissRequest = { expanded.value = false }
        ) {
            outlets.forEach { selectionOption ->
                DropdownMenuItem(
                    text = { Text(selectionOption) },
                    onClick = {
                        selectedOutlet.value = selectionOption
                        expanded.value = false
                    }
                )
            }
        }
    }
}
@Composable
fun SingleSelectCheckboxes(
    options: List<Pair<AppMode, String>> = listOf(
        AppMode.EzyCartPicker to "EzyCartPicker",
        AppMode.EzyLite to "EzyLite"
    ),
    selected:AppMode = AppMode.EzyLite,
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



@Composable
fun LockScreenOrientation(context : Context,orientation: Int) {
    val activity = context.findActivity()
    if (activity !=null) {
        DisposableEffect(Unit) {
            // 1. Remember original orientation
            val originalOrientation = activity.requestedOrientation

            // 2. Lock the new orientation
            activity.requestedOrientation = orientation

            // 3. Unlock/Restore when leaving
            onDispose {
                activity.requestedOrientation = originalOrientation
            }
        }
    }
}

// Helper extension to find the Activity from any Context
fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}



