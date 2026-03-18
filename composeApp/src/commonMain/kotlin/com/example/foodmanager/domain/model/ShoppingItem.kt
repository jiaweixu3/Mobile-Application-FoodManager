package com.example.foodmanager.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: Int? = null, // Database auto-generates this as a numeric ID
    val shopping_list_id: String, // Must be a String for the UUID
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String,
    @SerialName("is_checked")
    val isChecked: Boolean = false,
    @SerialName("photo_url")
    val photoUrl: String? = null
)
