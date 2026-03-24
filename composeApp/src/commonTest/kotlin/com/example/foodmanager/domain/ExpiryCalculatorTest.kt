package com.example.foodmanager

import kotlin.test.Test
import kotlin.test.assertEquals

// A simple calculator to determine how many days a category lasts
object ExpiryCalculator {
    fun getDaysUntilExpiry(category: String): Int {
        return when (category.lowercase().trim()) {
            "dairy" -> 7
            "meat" -> 3
            "vegetables", "fruits" -> 5
            "pasta", "canned" -> 365
            "bread" -> 4
            else -> 3 // Default fallback for unknown items
        }
    }
}

class ExpiryCalculatorTest {

    @Test
    fun `test Dairy category gives 7 days`() {
        val days = ExpiryCalculator.getDaysUntilExpiry("Dairy")
        assertEquals(7, days, "Dairy should default to 7 days")
    }

    @Test
    fun `test Meat category gives 3 days`() {
        val days = ExpiryCalculator.getDaysUntilExpiry("Meat")
        assertEquals(3, days, "Meat should default to 3 days")
    }

    @Test
    fun `test Vegetables and Fruits give 5 days`() {
        assertEquals(5, ExpiryCalculator.getDaysUntilExpiry("Vegetables"))
        assertEquals(5, ExpiryCalculator.getDaysUntilExpiry("Fruits"))
    }

    @Test
    fun `test Pantry items like Pasta give 365 days`() {
        val days = ExpiryCalculator.getDaysUntilExpiry("Pasta")
        assertEquals(365, days, "Pasta should last a whole year (365 days)")
    }

    @Test
    fun `test unknown category falls back to default 3 days`() {
        val days = ExpiryCalculator.getDaysUntilExpiry("Weird Alien Food")
        assertEquals(3, days, "Unknown categories should use the 3 day safety default")
    }

    @Test
    fun `test category ignores uppercase and extra spaces`() {
        // Edge cases for user input or weird database spacing
        assertEquals(7, ExpiryCalculator.getDaysUntilExpiry("   DAIRY   "))
        assertEquals(365, ExpiryCalculator.getDaysUntilExpiry("pAsTa"))
    }
}