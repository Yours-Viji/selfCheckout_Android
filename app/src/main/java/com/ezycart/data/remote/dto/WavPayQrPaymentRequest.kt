package com.ezycart.data.remote.dto

data class WavPayQrPaymentRequest (
    var channelCode :String,
    var paymentId :String,
    var payCode:String
)