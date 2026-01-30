package com.ezycart.services.usb

import com.ezycart.model.ProductInfo

class WeightValidationManager {
    private val THRESHOLD = 40 // Grams for Dry products
    private val FRESH_THRESHOLD = 40 // Grams for Fresh products

    // Store weights for fresh items to validate during removal later
    private val freshItemWeights = mutableMapOf<String, Double>()

    sealed class ValidationResult {
        object Success : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }

    /**
     * Logic for Adding Products
     */
    fun validateAddition(
        product: ProductInfo,
        deltaW1: Double, // Weight removed from Side A (should be negative)
        deltaW2: Double  // Weight added to Side B (should be positive)
    ): ValidationResult {
        val removedWeight = Math.abs(deltaW1)
        val addedWeight = Math.abs(deltaW2)

        // 1. First, check if removed weight matches added weight within 10g (General Hardware Sync)
        if (Math.abs(removedWeight - addedWeight) > FRESH_THRESHOLD) {
            return ValidationResult.Error("Weight mismatch between scales. Please reposition item.")
        }

        return if (product.isFreshItem) {
            // Fresh Product Logic: No software weight check, just store for later removal
            freshItemWeights[product.barcode] = addedWeight
            ValidationResult.Success
        } else {
            // Dry Product Logic: Check against Software Weight Range
            val minAllowed = (product.weightRange.startWeight ?: 0) - THRESHOLD
            val maxAllowed = (product.weightRange.maxWeight ?: 0) + THRESHOLD

            if (addedWeight in minAllowed.toDouble()..maxAllowed.toDouble()) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("Product weight mismatch. Expected ~${product.weightRange.startWeight}g, got ${addedWeight.toInt()}g")
            }
        }
    }
    fun productValidation(product: ProductInfo, deltaW2: Double ): ValidationResult{
        val addedWeight = Math.abs(deltaW2)

        val minAllowed = (product.weightRange.startWeight ?: 0) - THRESHOLD
        val maxAllowed = (product.weightRange.maxWeight ?: 0) + THRESHOLD

        if (addedWeight in minAllowed.toDouble()..maxAllowed.toDouble()) {
            return   ValidationResult.Success
        } else {
            return   ValidationResult.Error("Product weight mismatch. Expected ~${product.weightRange.startWeight}g, got ${addedWeight.toInt()}g")
        }
    }
    /**
     * Logic for Deleting Products
     */
    fun validateRemoval(
        product: ProductInfo,
        deltaW2: Double // Weight removed from Side B (should be negative)
    ): ValidationResult {
        val removedWeight = Math.abs(deltaW2)

        return if (product.isFreshItem) {
            val originalWeight = freshItemWeights[product.barcode] ?: 0.0
            if (Math.abs(removedWeight - originalWeight) <= FRESH_THRESHOLD) {
                freshItemWeights.remove(product.barcode)
                ValidationResult.Success
            } else {
                ValidationResult.Error("Removed weight does not match original fresh item weight.")
            }
        } else {
            val minAllowed = (product.weightRange.startWeight ?: 0) - THRESHOLD
            val maxAllowed = (product.weightRange.maxWeight ?: 0) + THRESHOLD

            if (removedWeight in minAllowed.toDouble()..maxAllowed.toDouble()) {
                ValidationResult.Success
            } else {
                ValidationResult.Error("Weight mismatch during removal.")
            }
        }
    }
}

data class WeightUpdate(
    val status: Int = 0,
    val w1: Double = 0.0,
    val w2: Double = 0.0,
    val delta_w1:Double = 0.0,
    val delta_w2:Double = 0.0,
    val loadcell_id:Int =0,
    val total_scanned: Double = 0.0
)