package com.example.foodmanager.model

data class FoodItem(
    val id: String,
    val name: String,
    val expiryDate: String, // YY-MM-DD format
    val amount: Double,
    val unit: String,
    val category: String,

    // Nullable fields
    val barcode: String? = null,
    val photoUrl: String? = null
)