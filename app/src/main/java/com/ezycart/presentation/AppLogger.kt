package com.ezycart.presentation

import com.ezycart.data.remote.dto.CmsLogRequest
import com.ezycart.domain.usecase.ShoppingUseCase
import com.ezycart.presentation.common.data.Constants
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AppLogger @Inject constructor(
    private val shoppingUseCase: ShoppingUseCase
)  {

    suspend fun sendLogData(
        appMerchantId: String,
        appOutletId: String,
        barCodeData: String,
        productWeight: String,
        weightScaleCurrentLoad: String,
        logEvent: LogEvent,
        cartId: String,
        cartTotalItems: Int,
        loadCellWeightInfo: String,
        softwareWeightInfo: String
    ) {

            runCatching {
                val cmsLogRequest = CmsLogRequest().apply {
                    cart_id = cartId
                    method = logEvent.method
                    level = logEvent.level.apiValue
                    merchant_id = appMerchantId
                    outlet_id = appOutletId
                    barcode = barCodeData
                    message = logEvent.message
                    action = logEvent.message

                    ai_response = "-"
                    product_weight = productWeight
                    weighscale_weight = weightScaleCurrentLoad
                    merchantId = appMerchantId
                    outletId = appOutletId

                    is_admin_logged_in = Constants.isAdminLogin
                    admin_id = Constants.adminPin

                    action_time = getCurrentDateTime()
                    totalItems = cartTotalItems

                    software_total_weight = softwareWeightInfo
                    loadcell_total_weight = loadCellWeightInfo

                    appMode = "sco"
                    language = Constants.getSelectedLanguageCode()
                }

                shoppingUseCase.sendLogsToBackEnd(cmsLogRequest)

            }.onFailure {
                // Intentionally ignored
                // Logging must never affect app flow
            }

    }


    private fun getCurrentDateTime(): String {
        var formattedDate = ""
        try {
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            formattedDate = currentDateTime.format(formatter)
        } catch (e: Exception) {
        }
        return formattedDate
    }

}
enum class LogLevel(val apiValue: String) {
    ERROR("Error"),
    INFO("Info"),
    WARNING("Warning")
}

enum class LogEvent(
    val level: LogLevel,
    val method: String,
    val message: String
) {
// ───────────── MEMBER ─────────────
MEMBER_LOGIN(
    level = LogLevel.INFO,
    method = "member_login",
    message = "Member Login"
),
    // ───────────── PAYMENT ─────────────
    TRAY_EMPTY_BEFORE_PAYMENT(
        level = LogLevel.ERROR,
        method = "tray_empty_before_payment",
        message = "Tray Empty Before Payment"
    ),
    PAYMENT_ERROR(
        level = LogLevel.ERROR,
        method = "payment_error_log",
        message = "Payment Blocked"
    ),

    PAYMENT_SUCCESS(
        level = LogLevel.INFO,
        method = "payment_success",
        message = "Payment Success"
    ),

    PAYMENT_SDK_ERROR(
        level = LogLevel.ERROR,
        method = "Payment SDK Error",
        message = "payment_sdk_error"
    ),

    // ───────────── ADMIN ─────────────

    ADMIN_LOGIN(
        level = LogLevel.INFO,
        method = "admin_login",
        message = "Admin Login"
    ),

    PRODUCT_ADDED_BY_ADMIN(
        level = LogLevel.INFO,
        method = "Product added to cart by Admin",
        message = "product_added_by_admin"
    ),
    HELP_CALLED(
        level = LogLevel.INFO,
        method = "Help Called",
        message = "help_called"
    ),
    ADMIN_ASSISTED_PAYMENT(
        level = LogLevel.INFO,
        method = "admin_assisted_payment",
        message = "Admin Assisted Payment"
    ),
    // ───────────── PRODUCT / BACKEND ─────────────

    BACKEND_RETURNED_ZERO_WEIGHT(
        level = LogLevel.WARNING,
        method = "Backend Returned Zero Weight",
        message = "backend_returned_zero_weight"
    ),

    PRODUCT_WEIGHT_MISMATCH(
        level = LogLevel.ERROR,
        method = "Product Weight Mismatch",
        message = "product_weight_mismatch"
    ),

    PRODUCT_NOT_FOUND(
        level = LogLevel.ERROR,
        method = "Product Not Found",
        message = "product_not_found"
    ),
    PRODUCT_NOT_SCANNED(
        level = LogLevel.ERROR,
        method = "Product Not Scanned",
        message = "product_not_scanned"
    ),
    VOUCHER_APPLIED(
        level = LogLevel.INFO,
        method = "Voucher Discount Applied",
        message = "voucher_discount_applied"
    ),
    VOUCHER_DELETED(
        level = LogLevel.INFO,
        method = "Voucher Discount Deleted",
        message = "voucher_discount_deleted"
    ),
    // ───────────── HARDWARE ─────────────

    PRINTER_PAPER_EMPTY(
        level = LogLevel.ERROR,
        method = "Printer Paper Empty",
        message = "printer_paper_empty"
    ),

    RECEIPT_PRINTER_ERROR(
        level = LogLevel.ERROR,
        method = "Receipt Printer Error",
        message = "receipt_printer_error"
    ),

    HARDWARE_ISSUE(
    level = LogLevel.ERROR,
    method = "Hardware Error",
    message = "hardware_error"
    );
}