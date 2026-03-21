package com.example.foodmanager.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Household(
    val id: String, // UUID
    val name: String,

    val joinCode: String? = null, // Allows sharing a household
    @SerialName("created_by") // Will handle writing it with different cases
    val createdBy: String? = null
)