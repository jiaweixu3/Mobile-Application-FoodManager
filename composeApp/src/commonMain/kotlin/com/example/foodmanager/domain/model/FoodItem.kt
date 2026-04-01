package com.example.foodmanager.domain.model
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class FoodItem(
    val id: Int? = null, // Nullable so supabase can autogenerate one

    @SerialName("inventory_id")
    val inventoryId: String?, // Nullable in case there's no house ID yet

    val name: String,

    @SerialName("expiry_date")
    val expiryDate: String,

    val amount: Double,
    val unit: String,
    val category: String,
    val isFavorite: Boolean = false
)