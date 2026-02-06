package com.ezycart.data.remote.dto

data class HelpRequest(
    var cartId :String,
    var description :String,
    var deviceId :String,
    var requestType :String,
    var trolleyId :String,
    var userId :Int,
    var cartZone:String,
    var merchantId: String,
    var outletId: String,
    var barcode:String,
    var productName:String,

    )
