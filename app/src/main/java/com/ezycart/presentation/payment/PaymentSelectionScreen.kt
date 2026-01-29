package com.ezycart.presentation.payment

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.material3.Surface
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import com.ezycart.R
import com.ezycart.data.remote.dto.ShoppingCartDetails

@Composable
fun PaymentSelectionScreen(
    cartCount: Int,
    shoppingCartInfo: ShoppingCartDetails?,
    onBackClick: () -> Unit,
    viewModel: PaymentViewModel = hiltViewModel(),

    ) {
    //val total = viewModel.totalAmount.collectAsStateWithLifecycle()
    val formattedAmount = String.format("%.2f", shoppingCartInfo?.finalAmount ?: 0.0)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.White),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        BitesHeaderPayment()
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.White)
            .padding(all = 30.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
        Text(
            text = "Please select your\npayment preference",
            style = MaterialTheme.typography.displayMedium, // Large font for 21"
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            color = Color.DarkGray,
            lineHeight = 50.sp
        )

        Spacer(modifier = Modifier.height(60.dp))

        // Amount Display Section
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Total Amount : ", fontSize = 32.sp, color = Color.Gray)
                Text("RM $formattedAmount", fontSize = 32.sp, fontWeight = FontWeight.Medium)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("To be paid : ", fontSize = 32.sp, color = Color.Gray)
                Text(
                    "RM $formattedAmount",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFFD32F2F)
                )
            }
        }

        Spacer(modifier = Modifier.height(80.dp))

        // Payment Method Grids
        Column(
            modifier = Modifier.width(900.dp), // Constrain width for 21" landscape/portrait
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                PaymentCategoryCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onPaymentMethodSelected("Cards") }
                ) {
                    // Place your Visa/Mastercard/Amex/MyDebit/GX images here
                    DummyPaymentLogoGroup("VISA / MASTERCARD / AMEX")
                }
                PaymentCategoryCard(
                    modifier = Modifier.weight(1f),
                    onClick = { viewModel.onPaymentMethodSelected("E-Wallet") }
                ) {
                    // Place your TNG / Maybank QR / Alipay images here
                    DummyPaymentLogoGroup("TNG / MAYBANK / ALIPAY")
                }
            }

            PaymentCategoryCard(
                modifier = Modifier.width(440.dp),
                onClick = { viewModel.onPaymentMethodSelected("GrabPay") }
            ) {
                DummyPaymentLogoGroup("GRABPAY / PAYLATER")
            }
        }
    }
    }
}

@Composable
fun BitesHeaderPayment(

) {
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
            Column (
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

        // 2. Purple Action Bar (Self Checkout & Help)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A)) // Deep Purple
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
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

        }
    }
}

@Composable
fun PaymentCategoryCard(
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        onClick = onClick,
        modifier = modifier.height(220.dp),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        border = BorderStroke(2.dp, Color(0xFFE0E0E0)),
        // In Material 3, use shadowElevation instead of elevation
        shadowElevation = 4.dp
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(24.dp)
        ) {
            content()
        }
    }
}

@Composable
fun DummyPaymentLogoGroup(label: String) {
    // Replace this with actual Image() calls for your logos
    Text(label, textAlign = TextAlign.Center, fontWeight = FontWeight.Bold, color = Color.Gray)
}