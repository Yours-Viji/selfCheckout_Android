package com.ezycart.data.remote.dto

data class WavPayQrResponse(
    val order_id: String,
    val qr_code: String,
    val redirect_url: String,
    val reference: String,
    val approval_code: String,
)