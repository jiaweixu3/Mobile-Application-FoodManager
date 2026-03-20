package com.example.foodmanager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ShoppingList(
    val id: String, //UUID
    val household_id: String
)