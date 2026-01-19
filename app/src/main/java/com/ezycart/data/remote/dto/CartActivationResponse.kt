package com.ezycart.model

data class CartActivationResponse(
    val id: Int,
    val merchantId: Int,
    val activationKey: String,
    val address: Any,
    val attention1Name: Any,
    val attention1Phone: Any,
    val attention2Name: Any,
    val attention2Phone: Any,
    val attention3Name: Any,
    val attention3Phone: Any,
    val code: String,
    val devices: List<Device>,
    val email: Any,
    val freshProductCode: Any,
    val latitude: Any,
    val longitude: Any,
    val name: String,
    val operatingHours: Any,
    val password: Any,
    val phoneNumber: Any,
    val region: Any,
    val sstPercentage: Any,
    val status: String,
    val totalSubscription: Int,
    val rekeyPOSTransactionFromEmail: String,
    val rekeyPOSTransactionToEmail: String,
    val liteSubscription: Int

)
data class Device(
    val activationKey: String,
    val appVersion: Any,
    val buildNo: Any,
    val deviceBrand: Any,
    val deviceId: String,
    val deviceLocale: Any,
    val deviceOs: Any,
    val deviceToken: Any,
    val deviceType: Any,
    val appMode: String,
    val id: Int

)