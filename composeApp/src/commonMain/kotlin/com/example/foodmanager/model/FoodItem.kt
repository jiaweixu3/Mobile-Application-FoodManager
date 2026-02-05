package com.example.foodmanager

data class FoodItem(
    val id: String,
    val name: String,
    val expiryDate: String, // YY-MM-DD format
    val quantity: String,
    val category: String
)