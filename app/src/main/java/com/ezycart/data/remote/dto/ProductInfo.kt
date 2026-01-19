package com.ezycart.model

data class ProductInfo(
    val aiTrainedStatus: Int,
    val alcoholFlag: String,
    val barcode: String,
    val hierarchy: Hierarchy,
    val imageUrl: String,
    val isFreshItem: Boolean,
    val isPrepaid: Boolean,
    val canAddToCart:Boolean,
    val productName: String,
    val skuNumber: String,
    val uom: String,
    val validateWG: Boolean,
    val weightRange: WeightRange,
    val isInfantMilk: Boolean,
    val isBakeryProduct: Boolean,
    val message:String
)

data class Hierarchy(
    val categoryCode: String,
    val categoryCodeDesc: String,
    val divisionCode: String,
    val divisionCodeDesc: String,
    val itemFamilyCode: String,
    val itemFamilyCodeDesc: String,
    val retailProductGroupCode: String,
    val retailProductGroupCodeDesc: String
)

data class WeightRange(
    val maxWeight: Int?,
    val startWeight: Int?
)