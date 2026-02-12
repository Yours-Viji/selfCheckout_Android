package com.ezycart.presentation.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
    viewModel: ExpressPaymentViewModel = viewModel(),
    onDismiss: () -> Unit,
    onHelpClicked: () -> Unit,
    onCardPaymentClicked: () -> Unit,
    onQrPaymentClicked: () -> Unit // Combined TNG/Grab/Maybank
) {
    val shoppingCartInfo = homeViewModel.shoppingCartInfo.collectAsState()
    val finalAmount = shoppingCartInfo.value?.finalAmount ?: 0.0
    val formattedAmount = String.format("%.2f", finalAmount)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color(0xFFF8F9FA) // Light grey background for "Premium" feel
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                BitesHeaderPayment()

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 40.dp, vertical = 32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // --- 1. Modern Pricing Header ---
                    Text(
                        text = "Checkout",
                        style = TextStyle(fontSize = 20.sp, color = Color.Gray, fontWeight = FontWeight.Medium)
                    )
                    Text(
                        text = "RM $formattedAmount",
                        style = TextStyle(
                            fontSize = 64.sp,
                            fontWeight = FontWeight.Black,
                            color = Color(0xFF1A1A1A),
                            letterSpacing = (-2).sp
                        )
                    )

                    Spacer(modifier = Modifier.height(48.dp))

                    Text(
                        text = "Select Payment Method",
                        style = TextStyle(fontSize = 24.sp, fontWeight = FontWeight.Bold)
                    )

                    Spacer(modifier = Modifier.weight(1f))

                    // --- 2. The Two Main Payment Options ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        // Option 1: Physical Card
                        PaymentOptionCard(
                            title = "Credit / Debit Card",
                            subtitle = "Visa, Mastercard, Amex",
                            iconRes = R.drawable.ic_pay_1, // Ensure this icon is high quality
                            primaryColor = Color(0xFF2196F3), // Professional Blue
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.onPaymentMethodSelected("CARD")
                                onCardPaymentClicked()
                            }
                        )

                        // Option 2: Digital QR
                        PaymentOptionCard(
                            title = "E-Wallet / QR",
                            subtitle = "TNG, Maybank, GrabPay",
                            iconRes = R.drawable.ic_pay_2,
                            primaryColor = Color(0xFF4CAF50), // "Digital" Green
                            modifier = Modifier.weight(1f),
                            onClick = {
                                viewModel.onPaymentMethodSelected("QR")
                                onQrPaymentClicked()
                            }
                        )
                    }

                    Spacer(modifier = Modifier.weight(1.2f))

                    // --- 3. Bottom Actions ---
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        OutlinedButton(
                            onClick = onDismiss,
                            modifier = Modifier.height(72.dp).weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            border = BorderStroke(2.dp, Color(0xFFE0E0E0))
                        ) {
                            Text("CANCEL", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Gray))
                        }

                        Button(
                            onClick = onHelpClicked,
                            modifier = Modifier.height(72.dp).weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFC107)) // Yellow for "Help"
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null, tint = Color.Black)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("CALL ASSISTANCE", style = TextStyle(fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.Black))
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
    subtitle: String,
    iconRes: Int,
    primaryColor: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(320.dp), // Taller for better visual impact
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
        border = BorderStroke(2.dp, primaryColor.copy(alpha = 0.1f))
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(24.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Circle Background for Icon
            Surface(
                modifier = Modifier.size(140.dp),
                shape = RoundedCornerShape(70.dp),
                color = primaryColor.copy(alpha = 0.1f)
            ) {
                Image(
                    painter = painterResource(id = iconRes),
                    contentDescription = null,
                    modifier = Modifier.padding(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = title,
                style = TextStyle(fontSize = 26.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF1A1A1A))
            )

            Text(
                text = subtitle,
                style = TextStyle(fontSize = 16.sp, color = Color.Gray, textAlign = TextAlign.Center)
            )
        }
    }

}