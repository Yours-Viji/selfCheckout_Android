package com.ezycart.payment.terminal

import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType

import okhttp3.RequestBody.Companion.toRequestBody


import android.content.Context
import kotlinx.coroutines.Dispatchers

import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException
import java.security.KeyStore
import java.security.SecureRandom
import java.security.cert.CertificateFactory
import java.util.concurrent.TimeUnit
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocketFactory
import javax.net.ssl.TrustManagerFactory
import javax.net.ssl.X509TrustManager

class GhlHttpClient(context: Context) {

    private val client: OkHttpClient

    init {
       /* val (sslSocketFactory, trustManager) = createSslSocketFactoryAndTrustManager(context)

        client = OkHttpClient.Builder()
            .sslSocketFactory(sslSocketFactory, trustManager)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()*/

         client = OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    suspend fun post(url: String, body: String): String =
        withContext(Dispatchers.IO) {
            val JSON = "application/json".toMediaType()
            val requestBody = body.toRequestBody(JSON)
            val request = Request.Builder()
                .url(url)
                .post(requestBody)
                .build()

            client.newCall(request).execute().use {
                it.body?.string() ?: throw IOException("Empty response")
            }
        }

    private fun createSslSocketFactoryAndTrustManager(context: Context): Pair<SSLSocketFactory, X509TrustManager> {
        val cf = CertificateFactory.getInstance("X.509")
        val caInput = context.assets.open("server_ca_cert.pem")
        val ca = caInput.use { cf.generateCertificate(it) }

        val keyStore = KeyStore.getInstance(KeyStore.getDefaultType())
        keyStore.load(null, null)
        keyStore.setCertificateEntry("ca", ca)

        val tmf = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        tmf.init(keyStore)
        val trustManager = tmf.trustManagers[0] as X509TrustManager

        val sslContext = SSLContext.getInstance("TLS")
        sslContext.init(null, arrayOf(trustManager), null)

        return sslContext.socketFactory to trustManager
    }

}
