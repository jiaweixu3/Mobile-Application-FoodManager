package com.example.foodmanager.model

data class ShoppingItem(
    val id: String, // Switched to String to match the FoodItem id
    val name: String,
    val isChecked: Boolean = false
)