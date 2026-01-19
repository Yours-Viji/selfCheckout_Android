package com.ezycart.presentation.home

data class HomeState(
    val scannedBarCode: String = "",
    val isLoading: Boolean = false,
    val error: String? = null,
    val isPaymentSuccess: Boolean = false,
    val isReadyToInitializePaymentSdk: Boolean=false
)