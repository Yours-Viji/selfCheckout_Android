package com.ezycart.data.remote.dto

data class CreateJwtTokenRequest (
    var terminalId :String,
    var terminalName :String,
    var terminalTrsm :String,
    var tId :String
)