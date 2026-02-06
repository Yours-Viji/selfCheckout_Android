package com.ezycart.data.remote.dto

data class HelpResponse(
    val `data`: HelpData
)
data class HelpData(
    val cartId: String,
    val cartZone: String,
    val description: String,
    val deviceId: String,
    val id: Int,
    val requestType: String,
    val status: String,
    val userId: Int
)