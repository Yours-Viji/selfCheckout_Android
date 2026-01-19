package com.ezycart.data.remote.api

import retrofit2.Retrofit

object ApiService {
    fun createAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }
}