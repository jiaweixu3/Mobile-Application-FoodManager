package com.example.foodmanager.domain

import com.example.foodmanager.model.FoodItem

/**
 * Sorts inventory by expiry date (soonest first) and optionally filters by product category
 * (e.g. "Pasta", "Meat", "Dairy").
 * Same-day expiring items keep stable order (sortedBy is stable).
 *
 * @param items Full inventory list
 * @param categoryFilter null = show all; otherwise only items whose category equals this
 */
fun sortAndFilterInventory(items: List<FoodItem>, categoryFilter: String?): List<FoodItem> {
    val filtered = if (categoryFilter == null) {
        items
    } else {
        items.filter { it.category == categoryFilter }
    }
    return filtered.sortedBy { it.expiryDate }
}
