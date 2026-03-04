package com.example.foodmanager.domain.useCase


// This file handles validating a new food item when it is inserted.
// Validation can return two possible results, success or error
sealed class ValidationResult {
    object Success : ValidationResult()
    data class Error(val message: String) : ValidationResult()
}

class ValidateFoodItemUseCase {

    // Rules
    fun execute(name: String, quantityStr: String, expiryDateMs: Long?): ValidationResult {
        // Name cannot be empty
        if (name.isBlank()) {
            return ValidationResult.Error("Name cannot be empty.")
        }

        // Amount must be positive
        val quantity = quantityStr.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            return ValidationResult.Error("Quantity must be a number greater than 0.")
        }

        // Empty expiry date is not allowed
        if (expiryDateMs == null) {
            return ValidationResult.Error("An expiry date must be selected.")
        }

        // Expired products are not allowed
        val yesterday = System.currentTimeMillis() - 86400000
        if (expiryDateMs < yesterday) {
            return ValidationResult.Error("You cannot add already expired items.")
        }

        return ValidationResult.Success
    }
}