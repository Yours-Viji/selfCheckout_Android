package com.ezycart.data.remote.dto

data class DeviceDetailsResponse(
    val activationKey: String,
    val appVersion: String,
    val buildNo: String,
    val deviceBrand: String,
    val deviceId: String,
    val deviceLocale: String,
    val deviceOS: String,
    val deviceStatus: String,
    val deviceToken: String?,
    val deviceType: String,
    val id: Int,
    val merchantId: Int,
    val outletCode: String,
    val outletId: Int,
    val hlbMid: String?,
    val deviceMode: String,
    val trollyNo: String
)
