package com.ezycart.payment.nearpay

import android.app.Activity
import com.ezycart.BuildConfig
import com.ezycart.presentation.common.data.Constants
import io.nearpay.sdk.Environments
import io.nearpay.sdk.NearPay
import io.nearpay.sdk.data.models.Session
import io.nearpay.sdk.data.models.SessionInfo
import io.nearpay.sdk.utils.PaymentText
import io.nearpay.sdk.utils.enums.AuthenticationData
import io.nearpay.sdk.utils.enums.DismissFailure
import io.nearpay.sdk.utils.enums.LogoutFailure
import io.nearpay.sdk.utils.enums.NetworkConfiguration
import io.nearpay.sdk.utils.enums.PurchaseFailure
import io.nearpay.sdk.utils.enums.SessionFailure
import io.nearpay.sdk.utils.enums.SetupFailure
import io.nearpay.sdk.utils.enums.TransactionData
import io.nearpay.sdk.utils.enums.UIPosition
import io.nearpay.sdk.utils.listeners.CheckSessionListener
import io.nearpay.sdk.utils.listeners.DismissListener
import io.nearpay.sdk.utils.listeners.LogoutListener
import io.nearpay.sdk.utils.listeners.PurchaseListener
import io.nearpay.sdk.utils.listeners.SessionListener
import io.nearpay.sdk.utils.listeners.SetupListener

import java.util.Locale
import java.util.UUID


class NearPayServiceImpl : NearPayService {

    private val tag = "NearPay SDK Log ==>>"
    private lateinit var nearPay: NearPay
    private var paymentListener: NearPaymentListener? = null
    private val jwtToken = "eyJhbGciOiJSUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7Im9wcyI6ImF1dGgiLCJjbGllbnRfdXVpZCI6ImVmZmUwMDU0LWI2NDctNDRhOC04OTZhLTBiMzUzM2RhMzY0MSIsInRlcm1pbmFsX2lkIjoiMDIxMTIwNjMwMDExMjA2MyJ9LCJpYXQiOjE3NDAwMTkxMjh9.dkcnpsophU4JnPSfdQ_99BKPvPgdHu2ETNJ8Qe0R-IXMy1j8Z14lAbrNJqZFvu26OPR2ujltH1KZHgsLsz3AxFn_2JbP37234BcAWm1GTAB1mLlKIU4-hyD_ORNout_HFKz0n0GDt3pyicEBBosJVQEeqfWnNGM6mOWvZHaOHzAu9DyYGhuPM9waH81O-vVFB2ipsgIj-U9c_tpPftds-jj2YFN0YYgIDBAKjoUA_jC7WDNz9CJwGUBd_k7k9-PDOnQ4uIGTL8TMSn_E_6iMc2ZwG-Gjop87I_SnHKPq6IffGPmX9XGGpCp8XlYK6BTdGSiHSvOAqLkPAZ4rrbbzOQ"
    override fun initializeSdk(activity: Activity) {

        nearPay = NearPay.Builder()
            .context(activity)
            .authenticationData(AuthenticationData.Jwt(getJwtToken()))
            .environment(Environments.SANDBOX)
            .locale(Locale.getDefault())
            .networkConfiguration(NetworkConfiguration.DEFAULT)
            .uiPosition(UIPosition.BOTTOM_END)
            .paymentText(PaymentText("يرجى تمرير الطاقة", "Please tap your card"))
            .loadingUi(true)
            .build()
    }

    override fun paymentSdkSetUp() {
        nearPay.setup(object : SetupListener {
            override fun onSetupCompleted() {
                // if you wish to get the receipt in Json format use nearPay.toJson()

            }

            override fun onSetupFailed(setupFailure: SetupFailure) {
                when (setupFailure) {
                    is SetupFailure.AlreadyInstalled -> {
                        // when the payment plugin is already installed  .
                    }

                    is SetupFailure.NotInstalled -> {
                        // when the installtion failed .
                    }

                    is SetupFailure.AuthenticationFailed -> {
                        // when the authentication failed .
                        // You can use the following method to update your JWT
                        //nearPay.updateAuthentication(AuthenticationData.Jwt("JWT HERE"))
                        //createAuthentication()
                    }

                    is SetupFailure.InvalidStatus -> {
                        // Please note that you can get the status using setupFailure.status
                    }

                    else -> {

                    }
                }
            }
        })
    }

    override fun initTapOnPayTransaction(
        activity: Activity,
        referenceNumber: String,
        payableAmount: String,
        emailId: String,
        mobileNumber: String,
        listener: NearPaymentListener?
    ) {
        this.paymentListener = listener
       // val amount: Long = if (BuildConfig.IS_LIVE) payableAmount.toLong() else "100".toLong()
        val amount: Long =payableAmount.toLong()
        val customerReferenceNumber = referenceNumber.ifEmpty {
            "9ace70b7-977d-4094-b7f4-4ecb17de9867"
        }
        val enableReceiptUi = true
        val enableReversal = false
        val finishTimeOut: Long = 70
        val requestId = UUID.randomUUID()
        val enableUiDismiss = true

        nearPay.purchase(
            amount,
            customerReferenceNumber,
            enableReceiptUi,
            enableReversal,
            finishTimeOut,
            requestId,
            enableUiDismiss,
            object :
                PurchaseListener {
                override fun onPurchaseApproved(transactionData: TransactionData) {
                    paymentListener?.onPaymentSuccess(transactionData)


                }

                override fun onPurchaseFailed(purchaseFailure: PurchaseFailure) {
                    val errorMessage = when (purchaseFailure) {
                        is PurchaseFailure.PurchaseDeclined -> "Payment declined"
                        is PurchaseFailure.PurchaseRejected -> "Payment rejected"
                        is PurchaseFailure.AuthenticationFailed -> "Authentication failed"
                        is PurchaseFailure.InvalidStatus -> "Invalid status"
                        is PurchaseFailure.GeneralFailure -> "General failure"
                        else -> "Unknown error"
                    }
                    paymentListener?.onPaymentFailed(errorMessage)
                    }

            })
    }

    override fun createUserSession(customerSessionID: String) {
        val sessionID = customerSessionID.ifEmpty {
            "9ace70b7-977d-4094-b7f4-4ecb17de6753"
        }
        val requestId = UUID.randomUUID()
        val enableReceiptUi = true
        val enableReversal = true
        val finishTimeOut: Long = 70
        val enableUiDismiss = true

        nearPay.session(
            sessionID,
            requestId,
            enableReceiptUi,
            enableReversal,
            finishTimeOut,
            enableUiDismiss,
            object :
                SessionListener {
                override fun onSessionClosed(session: Session?) {
                    // you can use the object "TransactionData" to get the TransactionData .
                    // if you wish to get the receipt in Json format use nearPay.toJson()
                    //session.transaction.id

                }

                override fun onSessionOpen(transactionData: TransactionData) {
                    // you can use the object "TransactionData" to get the TransactionData .
                    //session.transaction.id
                }

                override fun onSessionFailed(sessionFailure: SessionFailure) {
                    when (sessionFailure) {
                        is SessionFailure.AuthenticationFailed -> {
                            // when the authentication is failed
                        }

                        is SessionFailure.GeneralFailure -> {
                            // when there is general error
                        }

                        is SessionFailure.FailureMessage -> {
                            // when there is FailureMessage
                        }

                        is SessionFailure.InvalidStatus -> {
                            // Please note that you can get the status using sessionFailure.status
                        }
                    }

                }
            })

    }

    override fun getUserSession() {
        nearPay.getUserSession(object : CheckSessionListener {
            override fun onSessionFree() {

            }

            override fun onSessionFailed(sessionFailure: SessionFailure) {
                when (sessionFailure) {

                    is SessionFailure.AuthenticationFailed -> {
                        // message
                    }

                    is SessionFailure.FailureMessage -> {
                        // message
                    }

                    SessionFailure.GeneralFailure -> {
                    }

                    is SessionFailure.InvalidStatus -> {
                        // message
                    }
                }
            }

            override fun onSessionBusy(message: String) {

            }

            override fun getSessionInfo(info: SessionInfo) {
            }
        }
        )
    }

    override fun dismissUI() {
        nearPay.dismiss(object : DismissListener {
            override fun onDismiss(isDismissed: Boolean) {
            }

            override fun onDismissFailure(dismissFailure: DismissFailure) {
            }

        })
    }

    override fun logout() {
        nearPay.logout(object : LogoutListener {
            override fun onLogoutCompleted() {
                //write your message here
            }

            override fun onLogoutFailed(logoutError: LogoutFailure) {
                when (logoutError) {
                    LogoutFailure.AlreadyLoggedOut -> {
                        // when the user is already logged out
                    }

                    LogoutFailure.GeneralFailure -> {
                        // when the error is general error
                    }

                    else -> {

                    }
                }
            }
        })
    }

    private fun createAuthentication(authenticationType: AuthenticationType, loginData: String) {
        when (authenticationType) {
            AuthenticationType.JWT -> authenticateByJwt(loginData)
            AuthenticationType.EMAIL -> authenticateByEmail(loginData)
            else -> authenticateByMobile(loginData)
        }
    }

    private fun authenticateByJwt(loginData: String): AuthenticationData.Jwt {
        return  AuthenticationData.Jwt(loginData)

    }

    private fun authenticateByEmail(loginData: String) {
        AuthenticationData.Email(loginData)
    }

    private fun authenticateByMobile(loginData: String) {
        AuthenticationData.Mobile(loginData)
    }

    private fun getJwtToken():String{
        return Constants.jwtToken.ifEmpty {
            jwtToken
        }
    }
}