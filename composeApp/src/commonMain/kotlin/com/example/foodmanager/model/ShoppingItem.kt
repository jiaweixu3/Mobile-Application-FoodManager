package com.example.foodmanager.model

data class ShoppingItem(
    val id: Int,
    val name: String,
    val amount: Double,    // Descriptive: How much do we need?
    val unit: String,      // Descriptive: kg, L, Pack, etc.
    val category: String,  // Visual: Group items by "Fridge" or "Pantry"
    val isChecked: Boolean = false,
    val photoUrl: String? = null // Optional: Matches FoodItem for visual consistency
)