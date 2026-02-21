package com.example.foodmanager.model

data class ShoppingItem(
    val id: Int,
    val name: String,
    val isChecked: Boolean = false
)