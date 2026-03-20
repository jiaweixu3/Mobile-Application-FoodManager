package com.example.foodmanager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Inventory(
    val id : String, // UUID
    val household_id: String,
)