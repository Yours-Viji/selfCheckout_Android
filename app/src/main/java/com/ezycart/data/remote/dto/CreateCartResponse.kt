package com.ezycart.data.remote.dto

data class CreateCartResponse(
    val validationRange: Int,
    val validateMaxProduct: Int,
    val cartId:String,
    val token: String,
    val currency: String,
    val currencySymbol: String
)