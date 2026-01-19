package com.ezycart.payment.nearpay

import android.app.Activity

interface NearPayService {

    fun initializeSdk(activity: Activity)

    fun paymentSdkSetUp()

    fun initTapOnPayTransaction(
        activity: Activity,
        referenceNumber: String,
        amount: String,
        emailId: String,
        mobileNumber: String,
        listener: NearPaymentListener?
    )

    fun createUserSession(sessionID : String)

    fun getUserSession()

    fun dismissUI()

    fun logout()
}

enum class AuthenticationType {
    JWT,EMAIL,MOBILE
}