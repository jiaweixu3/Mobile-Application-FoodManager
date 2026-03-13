package com.example.foodmanager.domain.model

data class FoodItem(
    val id: Int,
    val inventory_id: String, //UUID
    val name: String,
    val expiryDate: String, // YYYY-MM-DD format
    val amount: Double,
    val unit: String,
    val category: String, // e.g. "Pasta", "Meat", "Dairy"

    // Nullable fields
    val barcode: String? = null,
    val photoUrl: String? = null
)