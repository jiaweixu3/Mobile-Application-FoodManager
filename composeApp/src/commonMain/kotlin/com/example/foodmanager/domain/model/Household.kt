package com.example.foodmanager.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class Household(
    val id: String, // UUID
    val name: String,

    val joinCode: String? = null // Allows sharing a household
)