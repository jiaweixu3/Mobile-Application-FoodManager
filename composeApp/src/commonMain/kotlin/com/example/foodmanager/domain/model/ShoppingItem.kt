package com.example.foodmanager.domain.model

data class ShoppingItem(
    val id: Int,
    val shopping_list_id: String, // UUID
    val name: String,
    val amount: Double,
    val unit: String,      // e.g: kg, L, Pack, etc.
    val category: String,  // e.g: "Pasta", "Dairy"
    val isChecked: Boolean = false,
    val photoUrl: String? = null 
)