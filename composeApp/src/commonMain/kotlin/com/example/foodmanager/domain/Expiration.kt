@file:Suppress("DEPRECATION")
package com.example.foodmanager.domain

import com.example.foodmanager.model.FoodItem
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/**
 * Pure function for testing: days until expiry given a reference "today".
 * Returns negative if expiry is in the past, 0 if same day, positive if in the future.
 * Returns 0 if date string is invalid.
 */
fun daysUntilExpiry(expiryDate: String, today: LocalDate): Int {
    return try {
        val expiry = LocalDate.parse(expiryDate)
        today.daysUntil(expiry)
    } catch (_: Exception) {
        0
    }
}

/**
 * Days remaining until expiry using current system date.
 */
fun calculateDaysRemaining(expiryDate: String): Int {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    return daysUntilExpiry(expiryDate, today)
}

/**
 * Days remaining until expiry for a [FoodItem]. Refactored from sprint1 to accept FoodItem.
 */
fun calculateDaysRemaining(item: FoodItem): Int = calculateDaysRemaining(item.expiryDate)
