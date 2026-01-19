package com.ezycart.data.remote.interceptors

import android.util.Log
import com.ezycart.data.datastore.PreferencesManager
import com.ezycart.presentation.common.data.Constants
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton



@Singleton
class AuthInterceptor @Inject constructor(
    private val preferencesManager: PreferencesManager
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()

        val appMode = runBlocking { preferencesManager.getAppMode() }

        val requestBuilder = originalRequest.newBuilder()

        val isPaymentEndpoint = originalRequest.url.encodedPath.contains("/payment/")

        val authToken = runBlocking {
            preferencesManager.getAuthToken()
        }

        val xAuthToken = runBlocking {
            preferencesManager.getXAuthToken()
        }

        if (isPaymentEndpoint) {
            val jwtToken = runBlocking {
                preferencesManager.getJwtToken()
            }

            Log.d("AuthInterceptor", "Payment endpoint detected: ${originalRequest.url}")
            Log.d("AuthInterceptor", "JWT Token: ${if (jwtToken.isNullOrBlank()) "MISSING" else "PRESENT"}")

            jwtToken?.takeIf { it.isNotBlank() }?.let { token ->
                requestBuilder.header("jwt-Authorization", token)
                Log.d("AuthInterceptor", "JWT header added for: ${originalRequest.url}")
            } ?: run {
                Log.e("AuthInterceptor", "JWT token is missing for payment endpoint!")
            }
        }

        authToken?.takeIf { it.isNotBlank() }?.let { token ->
            requestBuilder.header(Constants.AUTHORIZATION, "Bearer $token")
        }

        xAuthToken?.takeIf { it.isNotBlank() }?.let { token ->
            requestBuilder.header(Constants.X_AUTHORIZATION, "Bearer $token")
        }


        // Common headers
        requestBuilder.apply {
            header(Constants.CONTENT_TYPE, Constants.CONTENT_TYPE_JSON)
            header(Constants.ACCEPT, Constants.APPLICATION_JSON)
            header(Constants.API_KEY, Constants.API_KEY_VALUE)
            header(Constants.DEVICE_TYPE, Constants.DEVICE_TYPE_VALUE)
            header(Constants.ACCEPTED_LANGUAGE, "English")
            header("deviceLocale", "English")
            header(Constants.DEVICE_ID, Constants.deviceId)
            header(Constants.DEVICE_OS_VERSION, android.os.Build.VERSION.RELEASE)
            header(Constants.DEVICE_BRAND, android.os.Build.BRAND)
            header(Constants.APP_VERSION, "1.0.0")
            header(Constants.BUILD_NO, "01")
            header("appMode", appMode.name)
        }

        return chain.proceed(requestBuilder.build())
    }
}
