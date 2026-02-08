package com.example.foodmanager.model

data class ShoppingItem(
    val id: Int,
    val name: String,
    var isChecked: Boolean = false
)