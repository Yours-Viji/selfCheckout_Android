package com.ezycart.data.remote.dto

data class MemberLoginResponse(
    val isMember: Boolean,
    val memberNo: String,
    val memberType: String,
    val nameOnCard: String,
    val totalPoints: Double,
    val totalSpend: Double,
    var skipIntro:Boolean
)