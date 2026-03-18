package com.example.foodmanager.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ShoppingItem(
    val id: Int = 0, // Default to 0 so you can create new items before DB insertion
    val shopping_list_id: String, // UUID
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String,
    @SerialName("is_checked") // Maps Kotlin camelCase to Supabase snake_case
    val isChecked: Boolean = false,
    @SerialName("photo_url")
    val photoUrl: String? = null
)