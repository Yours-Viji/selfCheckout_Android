package com.ezycart.presentation.alertview

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.rememberLottieComposition
import com.ezycart.R

@Composable
fun QrPaymentAlertView(
    modifier: Modifier = Modifier,
    onBackToSummary: () -> Unit,
    onClose: () -> Unit,
    onRefresh: () -> Unit,
    qrCodeImage: Painter,
    amount: String,
    timerSeconds: Int,
    isTimerVisible: Boolean = true,
    isErrorVisible: Boolean = false,
    qrTypeLogo: Painter = painterResource(id = R.drawable.ic_duit_now_qr),
    qrTypeLogoVisible: Boolean = false
) {
    // Convert sdp dimensions (assuming approximate conversion)
    val cardWidth = 310.dp
    val cardHeight = 250.dp
    val smallPadding = 4.dp
    val mediumPadding = 5.dp
    val largePadding = 10.dp

    // Animation compositions
    val timerComposition by rememberLottieComposition(LottieCompositionSpec.Asset("anim_clock_primary.json"))
    val celebrateComposition by rememberLottieComposition(LottieCompositionSpec.Asset("anim_success_3.json"))
    val loadingComposition by rememberLottieComposition(LottieCompositionSpec.Asset("anim_loading_white.json"))

    Card(
        modifier = modifier
            .size(width = cardWidth, height = cardHeight)
            .padding(mediumPadding),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Back to Summary Button (Top Left)
            Row(
                modifier = Modifier
                    .align(Alignment.TopStart)
                    .padding(mediumPadding),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onBackToSummary,
                    modifier = Modifier.size(15.dp)
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.outline_arrow_back_24),
                        contentDescription = "Back to Summary",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "Back to Summary",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 7.sp
                )
            }

            // Close Button (Top Right)
            IconButton(
                onClick = onClose,
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .size(35.dp)
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.outline_close_24),
                    contentDescription = "Close",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            // QR Type Logo (Top Center - invisible by default)
            if (qrTypeLogoVisible) {
                Image(
                    painter = qrTypeLogo,
                    contentDescription = "QR Type Logo",
                    modifier = Modifier
                        .size(width = 55.dp, height = 20.dp)
                        .align(Alignment.TopCenter)
                )
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(mediumPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Message Text
                Spacer(modifier = Modifier.height(30.dp)) // Adjust based on your layout
                Text(
                    text = "Scan QR code Pay",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(vertical = mediumPadding)
                )

                // QR Code Container with Timer and Error Overlay
                Box(
                    modifier = Modifier
                        .size(145.dp)
                        .padding(vertical = mediumPadding)
                ) {
                    // QR Code Card
                    Card(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(20.dp)),
                        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Image(
                            painter = qrCodeImage,
                            contentDescription = "QR Code",
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(5.dp)
                        )

                        // Error Overlay
                        if (isErrorVisible) {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(Color(0x99000000))
                                    .padding(mediumPadding),
                                contentAlignment = Alignment.Center
                            ) {
                                Column(
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.Center
                                ) {
                                    Text(
                                        text = "Timed out!.\n\nScanned? Please Wait\n\nOR",
                                        color = Color.White,
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center
                                    )
                                    Spacer(modifier = Modifier.height(7.dp))
                                    Text(
                                        text = "Refresh",
                                        color = Color(0xFFFFC0CB), // Pink color
                                        fontWeight = FontWeight.Black,
                                        fontSize = 11.sp,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .background(
                                                color = Color.LightGray,
                                                shape = RoundedCornerShape(20.dp)
                                            )
                                            .padding(horizontal = 2.dp)
                                    )
                                }
                            }
                        }
                    }

                    // Timer Container (Top Center of QR Code)
                    if (isTimerVisible) {
                        Card(
                            modifier = Modifier
                                .size(29.dp)
                                .align(Alignment.TopCenter)
                                .offset(y = (-15).dp), // Adjust position as needed
                            shape = CircleShape,
                            elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
                            colors = CardDefaults.cardColors(containerColor = Color.Gray)
                        ) {
                            Column(
                                modifier = Modifier.fillMaxSize(),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                LottieAnimation(
                                    composition = timerComposition,
                                    iterations = LottieConstants.IterateForever,
                                    modifier = Modifier.size(20.dp)
                                )
                                Text(
                                    text = "$timerSeconds Sec",
                                    color = Color.White,
                                    fontWeight = FontWeight.Black,
                                    fontSize = 6.sp,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }

                // Transaction Amount Title
                Text(
                    text = "Transaction Amount",
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Medium,
                    fontSize = 9.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(smallPadding)
                )

                // Amount Text
                Text(
                    text = amount,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 20.sp,
                    textAlign = TextAlign.Center
                )
            }

            // Celebration Animation (Centered)
            LottieAnimation(
                composition = celebrateComposition,
                modifier = Modifier
                    .size(130.dp)
                    .align(Alignment.Center)
            )

            // Loading Animation (Centered)
            LottieAnimation(
                composition = loadingComposition,
                iterations = LottieConstants.IterateForever,
                modifier = Modifier
                    .size(width = 130.dp, height = 100.dp)
                    .align(Alignment.Center)
            )
        }
    }
}

/*
// Helper function for offset (if not available in your Compose version)
fun Modifier.offset(x: Dp = 0.dp, y: Dp = 0.dp) = this.then(
    androidx.compose.ui.draw.offset(x = x, y = y)
)*/
