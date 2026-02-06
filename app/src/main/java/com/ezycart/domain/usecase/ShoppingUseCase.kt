package com.ezycart.domain.usecase

import com.ezycart.data.remote.dto.CreateCartResponse
import com.ezycart.data.remote.dto.HelpRequest
import com.ezycart.data.remote.dto.HelpResponse
import com.ezycart.data.remote.dto.InvoiceResponse
import com.ezycart.data.remote.dto.MemberLoginResponse
import com.ezycart.data.remote.dto.NetworkResponse
import com.ezycart.data.remote.dto.PaymentRequest
import com.ezycart.data.remote.dto.PaymentResponse
import com.ezycart.data.remote.dto.PaymentStatusResponse
import com.ezycart.data.remote.dto.ShoppingCartDetails
import com.ezycart.data.remote.dto.UpdatePaymentRequest
import com.ezycart.domain.repository.AuthRepository

import com.ezycart.model.ProductInfo
import com.ezycart.model.ProductPriceInfo
import org.json.JSONObject
import javax.inject.Inject

class ShoppingUseCase @Inject constructor(
    private val authRepository: AuthRepository
) {
    suspend operator fun invoke(): NetworkResponse<ShoppingCartDetails> {
        return authRepository.getShoppingCartDetails()
    }

    suspend  fun createNewShoppingCart(): NetworkResponse<CreateCartResponse> {
        return authRepository.createNewShoppingCart()
    }

    suspend  fun getProductDetails(barCode: String): NetworkResponse<ProductInfo> {
        return authRepository.getProductDetails(barCode)
    }

    suspend  fun getPriceDetails(barCode: String): NetworkResponse<ProductPriceInfo> {
        return authRepository.getPriceDetails(barCode)
    }

    suspend  fun makePayment(paymentRequest: PaymentRequest): NetworkResponse<PaymentResponse> {
        return authRepository.makePayment(paymentRequest)
    }

    suspend  fun updatePaymentStatus(status: UpdatePaymentRequest): NetworkResponse<PaymentStatusResponse> {
        return authRepository.updatePaymentStatus(status)
    }

    suspend  fun getPaymentSummary(): NetworkResponse<ShoppingCartDetails> {
        return authRepository.getPaymentSummary()
    }

    suspend  fun addToCart(barCode: String,quantity:Int): NetworkResponse<ShoppingCartDetails> {
        return authRepository.addProductToShoppingCart(barCode,quantity)
    }

    suspend  fun editProductInCart(barCode: String,quantity:Int,id:Int): NetworkResponse<ShoppingCartDetails> {
        return authRepository.editProductInCart(barCode,id,quantity)
    }

    suspend  fun deleteProductFromCart(barCode: String,id:Int): NetworkResponse<ShoppingCartDetails> {
        return authRepository.deleteProductFromShoppingCart(barCode,id)
    }


    suspend  fun getInvoicePdf(referenceNo: String): NetworkResponse<InvoiceResponse> {
        return authRepository.getInvoicePdf(referenceNo)
    }

    suspend  fun memberLogin(memberNumber: String): NetworkResponse<MemberLoginResponse> {
        return authRepository.memberLogin(memberNumber)
    }

    suspend  fun applyVoucher(voucherCode: String): NetworkResponse<ShoppingCartDetails> {
        return authRepository.applyVoucher(voucherCode)
    }

    suspend  fun deleteVoucher(voucherCode: String): NetworkResponse<ShoppingCartDetails> {
        return authRepository.deleteVoucher(voucherCode)
    }

    suspend  fun createHelpTicket(helpRequest: HelpRequest): NetworkResponse<HelpResponse> {
        return authRepository.createHelpTicket(helpRequest)
    }

}