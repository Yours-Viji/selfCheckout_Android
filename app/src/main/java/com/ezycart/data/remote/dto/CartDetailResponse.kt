package com.ezycart.data.remote.dto


data class ShoppingCartDetails(
    val cartId: String,
    val cartItems: List<CartItem>,
    val finalAmount: Double,
    val totalDiscount: Double,
    val totalItems: Int,
    val totalPrice: Double,
    val promotionSave: Double,
    val totalTax: Double,
    val prePaidAmount:Double,
    val hasAlcoholProduct: Boolean,
    var ageVerified: Boolean,
    val token: String,
    val totalSubsidy:Double,
    val couponAmount:Double,
    val vourcherAmount:Double,
    val isBackOfficeApproved:Boolean,

    val appliedCoupons: List<DiscountDetails>,
    val appliedVouchers: List<DiscountDetails>,


)
data class CartItem(
    val barcode: String,
    val reqBarcode: String,
    val canChangeQty: Boolean,
    val canRemove: Boolean,
    val discountPrice: Double,
    val finalPrice: Double,
    val freeItem: List<FreeItem>,
    val hasPromo: Boolean,
    val id: Int,
    val originalPrice: Double,
    val productName: String,
    val promoDescription: String,
    val promoType: String,
    val promotionNo: String,
    val quantity: Int,
    val unitPrice: Double,
    val uom: String,
   /* var weight: Int,
    val unitWeight: Int,*/
    val productWeight: Int,
    val validateWG: Boolean,
    val isFreshItem: Boolean,
    val isAlcoholProduct: Boolean,
    val isInfantMilk: Boolean,
    val isBakeryProduct: Boolean,
    val isPrepaid:Boolean,
    val isIceNeeded: Boolean,
    val noOfItems: Int,
    val promoImageUrl: String,
    val imageUrl: String?,
    val applyNormalPromo: Boolean,
    val displayQty: Int,
    val isFreeItem: Boolean,
    val isMixAndMatchPromo: Boolean,
    val isSpecialPromoApplied: Boolean,
    val recallInfo: RecallInfo,
    val specialPrice: Double,
    val specialPromoCode: String,
    val stkNo: String,
    val subsidyInclude: String,
    val vatExclude: String,
    val finalPriceBeforeDiscount: Double,
    val isMemberPrice:Boolean,
    val itemCategory: String,
    val itemCategoryDesc: String,
    val weightRange: WeightRange,
    val isTimebasedPromoProduct:Boolean,
    val applyPromoNow:Boolean,
)

data class FreeItem(
    val barcode: String,
    val canChangeQty: Boolean,
    val canRemove: Boolean,
    val id: Int,
    val price: Double,
    val productName: String,
    val promoItem: Boolean,
    val uom: String,
    val weight: Int,
    val imageUrl: String
)

data class RecallInfo(
    val canAddToCart: Boolean,
    val isRecallProduct: Boolean,
    val recallMessage: String,
    val recallType: String
)
data class DiscountDetails(
    val code:String,
    val discountType:String,
    val discountValue:Double,
    val barcode:String,
    val stkNo:String
)
data class WeightRange(
    val maxWeight: Int?,
    val startWeight: Int?
)
