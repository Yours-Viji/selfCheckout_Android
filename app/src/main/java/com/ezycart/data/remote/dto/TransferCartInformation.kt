package com.ezycart.data.remote.dto

data class TransferCartInformation(
    val date:String,
    val userId:String,
    val appMode:String,
    val oldCartId:String,
    val currentCartId:String
)