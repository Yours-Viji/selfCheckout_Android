package com.ezycart.model

data class ProductPriceInfo(
    val isMemberPrice: Boolean,
    val originalPrice: Double,
    val price: Double,
    val promoDetails: PromoDetails,
    val noOfItems:Int
)

data class PromoDetails(
    val applyPromoNow: Boolean,
    val buyGetType: Any,
    val endDate: String,
    val fixedPrice: Double,
    val imageUrl: String,
    val isGrouping: Int,
    val isTimebasedPromoProduct: Boolean,
    val normalPrice: Double,
    val products: List<Any>,
    val promoDescription: String,
    val promotionId: Int,
    val promotionNo: String,
    val promotionType: String,
    val quantity: Int,
    val quantity1: Int,
    val recurring: Boolean,
    val savingValue: Double,
    val savingValue1: Double,
    val specialPrice: Double,
    val startDate: String,
    val totalDiscount: Double
)