package com.ezycart.presentation.landing

import com.ezycart.R


data class LandingUiState(
    val banners: List<Int> = listOf(R.drawable.programatic_ad_demo, R.drawable.ic_banner_2, R.drawable.ic_banner_3),
    val currentBannerIndex: Int = 0,
    val isStarted: Boolean = false // Track if "Touch to Start" was clicked
)
