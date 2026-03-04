package com.example.foodmanager.domain

// Validation can return two possible results
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

class ValidateFoodItemUseCase {

    // Rules
    fun execute(name: String, quantityStr: String, expiryDateMs: Long?): ValidationResult {
        // 1) Name cannot be empty
        if (name.isBlank()) {
            return ValidationResult.Error("Name cannot be empty.")
        }

        // 2) Quantity must be a valid number greater than 0
        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            return ValidationResult.Error("Quantity must be a number greater than 0.")
        }

        // 3) An expiry date must be selected
        if (expiryDateMs == null) {
            return ValidationResult.Error("An expiry date must be selected.")
        }

        // 4) Expired products are not allowed
        val yesterday = System.currentTimeMillis() - 86400000
        if (expiryDateMs < yesterday) {
            return ValidationResult.Error("You cannot add already expired items.")
        }

        return ValidationResult.Success
    }
}