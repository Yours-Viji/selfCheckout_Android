package com.ezycart.presentation.landing

import com.ezycart.R


data class LandingUiState(
    val banners: List<Int> = listOf(R.drawable.ic_banner_guideline_1,R.drawable.ic_banner_guideline_2,R.drawable.ic_banner_guideline_3),
    val currentBannerIndex: Int = 0,
    val isStarted: Boolean = false // Track if "Touch to Start" was clicked
)
