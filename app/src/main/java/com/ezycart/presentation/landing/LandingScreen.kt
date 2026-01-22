package com.ezycart.presentation.landing

import android.util.Log
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ezycart.R
import com.ezycart.presentation.home.CartIconWithBadge

@Composable
fun LandingScreen(viewModel: LandingViewModel = viewModel(),
                  goToHomeScreen: () -> Unit) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize().background(Color.Black)) {

        // 1. Dynamic Content Area (Banner or Language Selection)
        Box(modifier = Modifier.weight(1f).background(Color.White)) {
            AnimatedContent(
                targetState = uiState.value.isStarted,
                transitionSpec = { fadeIn(tween(500)) togetherWith fadeOut(tween(500)) }
            ) { started ->
                if (!started) {
                    // Show the Auto-Scroll Banner initially
                    AutoScrollingBanner(
                        banners = uiState.value.banners,
                        currentIndex = uiState.value.currentBannerIndex,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    // Replace with Language Selection after click
                    LanguageSelectionScreen(onLanguageSelected = { lang ->
                        Log.d("Kiosk", "Selected: $lang")
                        goToHomeScreen()
                        // Proceed to next step
                    })
                }
            }
        }

        // 2. Fixed Black Footer
        if (!uiState.value.isStarted) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Black)
                    // ADD THIS: It pushes the content up away from the system navigation buttons
                    .navigationBarsPadding()
                    .clickable(enabled = !uiState.value.isStarted) {
                        viewModel.onStartClicked()
                    },
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TOUCH TO START",
                    color = Color.White,
                    style = TextStyle(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 4.sp,
                        textAlign = TextAlign.Center,
                        platformStyle = PlatformTextStyle(
                            includeFontPadding = false
                        ),
                        lineHeight = 36.sp
                    ),
                    modifier = Modifier.wrapContentSize() // Only takes space needed
                )
            }
        }
    }
}

@Composable
fun LanguageSelectionScreen(onLanguageSelected: (String) -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE5D9F2), // Light lavender at top
                        Color(0xFFF3F0F7), // Very light grey-white in middle
                        Color(0xFFE0D7EA)  // Soft purple at bottom
                    )
                )
            )
    ) {
        // Decorative background illustrations (place in corners)
       /* Image(
            painter = painterResource(id = R.drawable.veg_illustration),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Inside,
            alpha = 0.6f
        )*/
        BitesHeader(onHelpClick = { /* Show Help Dialog */ })

            Column(
                modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Please select your preferred language",
                    fontSize = 28.sp,
                    textAlign = TextAlign.Center,
                    color = Color.DarkGray,
                    modifier = Modifier.padding(bottom = 48.dp)
                )

                val languages = listOf("Bahasa Malaysia", "中文", "English", "日本語", "한국인")
                languages.forEach { lang ->
                    LanguageButton(text = lang) { onLanguageSelected(lang) }
                    Spacer(modifier = Modifier.height(24.dp))
                }

        }
    }
}

@Composable
fun LanguageButton(text: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.6f).height(90.dp),
        colors = ButtonDefaults.buttonColors(containerColor = Color.White),
        shape = RoundedCornerShape(12.dp),
        border = BorderStroke(1.dp, Color.LightGray),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
    ) {
        Text(text = text, color = Color.Black, fontSize = 24.sp)
    }
}

@Composable
fun GlassyKioskBackground(
    content: @Composable ColumnScope.() -> Unit
) {
    // 1. Main Gradient Layer
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFE5D9F2), // Light lavender at top
                        Color(0xFFF3F0F7), // Very light grey-white in middle
                        Color(0xFFE0D7EA)  // Soft purple at bottom
                    )
                )
            )
    ) {
        // 2. The Glassy Card Container
        Card(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp), // Margin around the main app area
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(
                containerColor = Color.White.copy(alpha = 0.6f) // Semi-transparent "Glass"
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
            border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f)) // Soft highlight edge
        ) {
            /*// Apply a slight blur effect if on Android 12+ (optional)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                content = content
            ) {

            }*/
        }
    }
}
@Composable
fun BitesHeader(

    onHelpClick: () -> Unit
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
        }

        // 2. Purple Action Bar (Self Checkout & Help)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF6A1B9A)) // Deep Purple
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
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

            // Help Button Section
            Row(
                modifier = Modifier
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

            }
        }
    }
}