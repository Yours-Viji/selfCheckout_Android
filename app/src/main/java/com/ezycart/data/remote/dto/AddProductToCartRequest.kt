package com.ezycart.data.remote.dto

data class AddProductToCartRequest(
    val barcode:String,
    val quantity:Int,
)
