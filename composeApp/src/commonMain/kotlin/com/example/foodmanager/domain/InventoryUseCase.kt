package com.example.foodmanager.domain

import com.example.foodmanager.model.FoodItem

/**
 * Sorts inventory by expiry date (soonest first) and optionally filters by storage location.
 * Same-day expiring items keep stable order (sortedBy is stable).
 *
 * @param items Full inventory list
 * @param locationFilter null = show all; otherwise only items whose category equals this (e.g. "Fridge", "Pantry")
 */
fun sortAndFilterInventory(items: List<FoodItem>, locationFilter: String?): List<FoodItem> {
    val filtered = if (locationFilter == null) {
        items
    } else {
        items.filter { it.category == locationFilter }
    }
    return filtered.sortedBy { it.expiryDate }
}
