package com.example.foodmanager.domain

import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.useCase.InventorySortOption
import com.example.foodmanager.domain.useCase.sortAndFilterInventory
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Unit tests for inventory sorting by expiry and filtering by product category.
 * Handles cases such as same-day expiring items.
 */
internal class InventorySortFilterTest {

    private fun item(id: Int, name: String, expiryDate: String, category: String) = FoodItem(
        id = id,
        inventoryId = "inv_1",
        name = name,
        expiryDate = expiryDate,
        amount = 1.0,
        unit = "unit",
        category = category
    )

    @Test
    fun sortAndFilterInventory_noFilter_returnsSortedByExpiry() {
        val items = listOf(
            item(1, "Late", "2026-04-01", "Pasta"),
            item(2, "Soon", "2026-02-15", "Meat"),
            item(3, "Mid", "2026-03-01", "Meat")
        )
        val result = sortAndFilterInventory(items, null, InventorySortOption.EXPIRY)
        assertEquals(3, result.size)
        assertEquals("2026-02-15", result[0].expiryDate)
        assertEquals("2026-03-01", result[1].expiryDate)
        assertEquals("2026-04-01", result[2].expiryDate)
    }

    @Test
    fun sortAndFilterInventory_sameDayExpiring_stableOrder() {
        val items = listOf(
            item(1, "First", "2026-03-10", "Dairy"),
            item(2, "Second", "2026-03-10", "Dairy")
        )
        val result = sortAndFilterInventory(items, null, InventorySortOption.EXPIRY)
        assertEquals(2, result.size)
        assertEquals("2026-03-10", result[0].expiryDate)
        assertEquals("2026-03-10", result[1].expiryDate)
        assertEquals("First", result[0].name)
        assertEquals("Second", result[1].name)
    }

    @Test
    fun sortAndFilterInventory_filterMeat_onlyMeatItems() {
        val items = listOf(
            item(1, "Milk", "2026-02-01", "Dairy"),
            item(2, "Steak", "2026-03-15", "Meat"),
            item(3, "Chicken", "2026-02-10", "Meat")
        )
        val result = sortAndFilterInventory(items, "Meat", InventorySortOption.EXPIRY)
        assertEquals(2, result.size)
        assertEquals("Chicken", result[0].name)
        assertEquals("Steak", result[1].name)
    }

    @Test
    fun sortAndFilterInventory_filterPantry_onlyPantryItems() {
        val items = listOf(
            item(1, "Milk", "2026-02-01", "Dairy"),
            item(2, "Spaghetti", "2026-03-15", "Pasta"),
            item(3, "Penne", "2026-04-01", "Pasta")
        )
        val result = sortAndFilterInventory(items, "Pasta", InventorySortOption.EXPIRY)
        assertEquals(2, result.size)
        assertEquals("Spaghetti", result[0].name)
        assertEquals("Penne", result[1].name)
    }

    @Test
    fun sortAndFilterInventory_filterWithNoMatch_returnsEmpty() {
        val items = listOf(
            item(1, "Milk", "2026-02-01", "Dairy")
        )
        val result = sortAndFilterInventory(items, "Pasta", InventorySortOption.EXPIRY)
        assertEquals(0, result.size)
    }

    @Test
    fun sortAndFilterInventory_emptyList_returnsEmpty() {
        val result = sortAndFilterInventory(emptyList(), null, InventorySortOption.EXPIRY)
        assertEquals(0, result.size)
    }
}
