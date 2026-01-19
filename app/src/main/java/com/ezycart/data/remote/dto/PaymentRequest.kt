package com.ezycart.data.remote.dto

data class PaymentRequest (
     var channelCode :String,
     var paymentId :String
)

data class UpdatePaymentRequest (
     var referenceNo:String,
     var statusCode :String,
     var statusMessage :String
)
