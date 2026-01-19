package com.ezycart.presentation.common.data

import android.annotation.SuppressLint
import android.icu.text.SimpleDateFormat
import com.ezycart.BuildConfig
import okhttp3.MediaType.Companion.toMediaType
import java.text.DecimalFormat
import java.util.Date

object Constants {
    var BASE_URL = com.ezycart.BuildConfig.BASE_URL

    var EZY_LITE_TRANSACTION_URL = "${BASE_URL}/ezycart/lite-transactions/15/23"

    var currencySymbol = BuildConfig.CURRENCY_SYMBOL
    const val CONTENT_TYPE = "Content-Type"
    const val ACCEPT = "Accept"
    const val APPLICATION_JSON = "application/json"
    const val CONTENT_TYPE_JSON = "application/x-www-form-urlencoded"
    val MEDIA_TYPE_JSON = "application/json; charset=utf-8".toMediaType()
    const val API_KEY = "apiKey"
    const val DEVICE_TYPE = "deviceType"
    const val DEVICE_ID = "deviceId"
    const val DEVICE_ID_VALUE = "device_id_value"
    const val DEVICE_OS_VERSION = "deviceOsVersion"
    const val DEVICE_BRAND = "deviceBrand"
    const val APP_VERSION = "appVersion"
    const val BUILD_NO = "buildNo"
    const val TBM = "TBM"
    const val API_KEY_VALUE = "cfg202106110012"
    const val DEVICE_TYPE_VALUE = "1"
    const val AUTHORIZATION = "Authorization"
    const val X_AUTHORIZATION = "x-authorization"
    const val ACCEPTED_LANGUAGE = "accepted-language"

    //RemoteConfig Key
    const val KEY_MID = "mid"
    const val KEY_TID = "tid"
    const val KEY_TERMINAL_ID = "terminal_id"

    private val decimalFormat = DecimalFormat("0.00")
    fun getDecimalFormater(): DecimalFormat {
        decimalFormat.minimumFractionDigits = 2
        decimalFormat.maximumFractionDigits = 2

        return decimalFormat
    }

    @SuppressLint("SimpleDateFormat")
    fun getDate(): String {
        return SimpleDateFormat("dd-MMM-yyyy").format(Date())
    }

    var deviceId = ""
    var employeeToken = ""
    var jwtToken = ""
    var nearPaySessionID = ""
    var paymentOrderId = ""
    var paymentCode = ""
}