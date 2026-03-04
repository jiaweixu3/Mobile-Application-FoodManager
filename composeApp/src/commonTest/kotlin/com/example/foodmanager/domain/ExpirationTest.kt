package com.example.foodmanager.domain

import com.example.foodmanager.model.FoodItem
import kotlinx.datetime.LocalDate
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for expiration logic. Verifies item expiration including same-day expiring.
 */
internal class ExpirationTest {

    @Test
    fun daysUntilExpiry_sameDay_returnsZero() {
        val today = LocalDate(2026, 3, 10)
        assertEquals(0, daysUntilExpiry("2026-03-10", today))
    }

    @Test
    fun daysUntilExpiry_future_returnsPositive() {
        val today = LocalDate(2026, 3, 10)
        assertEquals(5, daysUntilExpiry("2026-03-15", today))
        assertEquals(1, daysUntilExpiry("2026-03-11", today))
    }

    @Test
    fun daysUntilExpiry_past_returnsNegative() {
        val today = LocalDate(2026, 3, 10)
        assertEquals(-1, daysUntilExpiry("2026-03-09", today))
        assertEquals(-10, daysUntilExpiry("2026-02-28", today))
    }

    @Test
    fun daysUntilExpiry_invalidFormat_returnsZero() {
        val today = LocalDate(2026, 3, 10)
        assertEquals(0, daysUntilExpiry("not-a-date", today))
        assertEquals(0, daysUntilExpiry("", today))
    }

    @Test
    fun calculateDaysRemaining_withFoodItem_delegatesToExpiryDate() {
        val item = FoodItem(
            id = 1,
            name = "Milk",
            expiryDate = "2026-03-15",
            amount = 1.0,
            unit = "L",
            category = "Fridge"
        )
        // Uses current system date; we only verify it returns an integer (no throw)
        val result = calculateDaysRemaining(item)
        // Result depends on "today" at run time; just ensure it's consistent with string version
        assertEquals(calculateDaysRemaining(item.expiryDate), result)
    }
}
