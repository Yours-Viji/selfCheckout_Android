package com.ezycart.data.remote.dto

data class CreateCartRequest (
    val name :String,
    var deviceId :String,
    var merchantId :String,
    var outletId :String,
    var userId :String,
    var trolleyNo:String,
    var appMode:String,
    var employeeId :String,
    var memberNumber:String,
)
