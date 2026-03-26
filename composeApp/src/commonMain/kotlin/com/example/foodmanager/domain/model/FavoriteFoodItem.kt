package com.example.foodmanager.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class FavoriteFoodItem(
    val id: String? = null,
    @SerialName("household_id")
    val householdId: String?,
    val name: String,
    val amount: Double,
    val unit: String,
    val category: String
)
