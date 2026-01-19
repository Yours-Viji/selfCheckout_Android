package com.ezycart.presentation.common.components


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp


import com.airbnb.lottie.compose.*
import com.ezycart.domain.usecase.LoadingManager

@Composable
fun GlobalLoadingOverlay(loadingManager: LoadingManager) {
    val isLoading = loadingManager.isLoading.collectAsState()

    if (isLoading.value) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = 0.4f)),
            contentAlignment = Alignment.Center
        ) {
            val composition = rememberLottieComposition(LottieCompositionSpec.Asset("anim_loading_white.json"))
            val progress = animateLottieCompositionAsState(
                composition.value,
                iterations = LottieConstants.IterateForever
            )

            LottieAnimation(
                composition = composition.value,
                progress = { progress.value },
                modifier = Modifier.size(220.dp)
            )
        }
    }
}