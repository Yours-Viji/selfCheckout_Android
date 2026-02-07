package com.ezycart.presentation.payment

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import com.ezycart.R
import com.ezycart.presentation.home.HomeViewModel

@Composable
fun BitesPaymentDialog(
    homeViewModel: HomeViewModel,
    viewModel: PaymentViewModel = viewModel(),
    onDismiss: () -> Unit,
    onHelpClicked: () -> Unit,
    onCardPaymentClicked: () -> Unit,
    onWalletPaymentClicked: () -> Unit,
    onGrabPaymentClicked: () -> Unit
) {
    val shoppingCartInfo = homeViewModel.shoppingCartInfo.collectAsState()
    val formattedAmount = String.format("%.2f", shoppingCartInfo?.value?.finalAmount ?: 0.0)
    // Use Dialog with platformDefaultScrollDisabled to make it look like an Activity
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(
            usePlatformDefaultWidth = false // Forces full screen
        )
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.White
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Header (Your provided code)
                BitesHeaderPayment()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    // 1. Pricing Section
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = stringResource(R.string.please_select_your_payment_preference),
                            style = TextStyle(
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFF333333)
                            ),
                            textAlign = TextAlign.Center
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        Text(
                            text = stringResource(R.string.total_amount_rm, formattedAmount),
                            style = TextStyle(fontSize = 22.sp, color = Color.Gray)
                        )
                        Row {
                            Text(
                                text = stringResource(R.string.to_be_paid),
                                style = TextStyle(fontSize = 22.sp, color = Color.Gray)
                            )
                            Text(
                                text = "RM $formattedAmount",
                                style = TextStyle(
                                    fontSize = 24.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color.Red
                                )
                            )
                        }
                    }

                    // 2. Payment Methods Grid
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                            PaymentOptionCard(
                                title = "VISA / MASTERCARD / AMEX",
                                iconRes = R.drawable.ic_pay_1, // Add your icons
                                modifier = Modifier.weight(1f)
                            ) {
                                onCardPaymentClicked()
                                viewModel.onPaymentMethodSelected("CARD")
                                onDismiss
                            }

                            PaymentOptionCard(
                                title = "TNG / MAYBANK / ALIPAY",
                                iconRes = R.drawable.ic_pay_2,
                                modifier = Modifier.weight(1f)
                            ) {
                                onWalletPaymentClicked()
                                viewModel.onPaymentMethodSelected("E-WALLET")
                                onDismiss
                            }
                        }

                        PaymentOptionCard(
                            title = "GRABPAY / PAYLATER",
                            iconRes = R.drawable.ic_pay_3,
                            modifier = Modifier
                                .fillMaxWidth(0.7f)
                                .align(Alignment.CenterHorizontally)
                        ) {
                            onGrabPaymentClicked()
                            viewModel.onPaymentMethodSelected("GRAB")
                            onDismiss
                        }
                    }

                    // 3. Bottom Navigation Buttons
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        // Back Button
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(2.dp, Color.LightGray)
                        ) {
                            Text(stringResource(R.string.back), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.Gray))
                        }

                        // Help Button
                        Button(
                            onClick = onHelpClicked,
                            modifier = Modifier
                                .weight(1f)
                                .height(60.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6A1B9A))
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.White)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(stringResource(R.string.help), style = TextStyle(fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White))
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun PaymentOptionCard(
    title: String,
    iconRes: Int,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(200.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        border = BorderStroke(1.dp, Color(0xFFEEEEEE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Optional Image for better look
             Image(
                painter = painterResource(id = iconRes),
                contentDescription = null,
                modifier = Modifier.size(150.dp)
            )
            Text(
                text = title,
                style = TextStyle(
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.DarkGray,
                    textAlign = TextAlign.Center
                )
            )
        }
    }
}