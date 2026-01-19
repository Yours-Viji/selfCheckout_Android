package com.ezycart.model


data class EmployeeLoginRequest(
    val employeePin:String,
    val merchantId:String,
    val outletId:String
   // var registrationToken=""
)
