package com.example.foodmanager.domain.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class HouseholdMember(
    val id: String? = null,
    @SerialName("household_id")
    val householdId: String,
    @SerialName("user_id")
    val userId: String,
    val email: String,
    @SerialName("display_name")
    val displayName: String,
    val role: String
)
