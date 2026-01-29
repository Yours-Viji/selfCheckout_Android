package com.ezycart

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition

@Composable
fun CommonAlertView(
    state: AlertState,
    onClose: () -> Unit
) {
    Dialog(
        onDismissRequest = { if (state.isDismissible) onClose() },
        properties = DialogProperties(
            dismissOnBackPress = state.isDismissible,
            dismissOnClickOutside = state.isDismissible
        )
    ) {
        Surface(
            shape = RoundedCornerShape(24.dp), // Smooth rounded corners
            color = Color.White,
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shadowElevation = 12.dp
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. Title
                Text(
                    text = state.title,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.ExtraBold,
                        fontSize = 22.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 2. Center Lottie Animation (Loading from Assets)
                val composition = rememberLottieComposition(
                    LottieCompositionSpec.Asset(state.lottieFileName)
                )
                val progress = animateLottieCompositionAsState(
                    composition = composition.value,
                    iterations = LottieConstants.IterateForever
                )

                LottieAnimation(
                    composition = composition.value,
                    progress = { progress.value },
                    modifier = Modifier.size(180.dp) // Optimized size for kiosk screens
                )

                Spacer(modifier = Modifier.height(16.dp))

                // 3. Bottom Message
                Text(
                    text = state.message,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        lineHeight = 24.sp
                    ),
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray
                )

                // 4. Dismiss Button (Only if cancellable)
                if (state.isDismissible) {
                    Spacer(modifier = Modifier.height(24.dp))
                    Button(
                        onClick = onClose,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = when (state.type) {
                                AlertType.ERROR -> Color(0xFFC62828)
                                AlertType.SUCCESS -> Color(0xFF2E7D32)
                                else -> Color.Black
                            }
                        )
                    ) {
                        Text("CLOSE", fontWeight = FontWeight.Bold, color = Color.White)
                    }
                }
            }
        }
    }
}

enum class AlertType {
    SUCCESS, ERROR, INFO, WARNING
}

data class AlertState(
    val title: String,
    val message: String,
    val lottieFileName: String,
    val type: AlertType,
    val isDismissible: Boolean = true,
    val onDismiss: () -> Unit = {}
)