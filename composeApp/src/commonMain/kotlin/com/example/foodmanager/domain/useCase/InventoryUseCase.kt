package com.example.foodmanager.domain.useCase

import com.example.foodmanager.domain.model.FoodItem

enum class InventorySortOption(val label: String) {
    EXPIRY("Expiry"),
    NAME("Name"),
    AMOUNT("Amount"),
    CATEGORY("Category")
}

/**
 * Sorts inventory by expiry date (soonest first) and optionally filters by product category
 * (e.g. "Pasta", "Meat", "Dairy").
 * Same-day expiring items keep stable order (sortedBy is stable).
 *
 * @param items Full inventory list
 * @param categoryFilter null = show all; otherwise only items whose category equals this
 */
fun sortAndFilterInventory(
    items: List<FoodItem>,
    categoryFilter: String?,
    sortOption: InventorySortOption
): List<FoodItem> {
    val filtered = if (categoryFilter == null) {
        items
    } else {
        items.filter { it.category == categoryFilter }
    }
    return when (sortOption) {
        InventorySortOption.EXPIRY -> filtered.sortedBy { it.expiryDate }
        InventorySortOption.NAME -> filtered.sortedBy { it.name.lowercase() }
        InventorySortOption.AMOUNT -> filtered.sortedBy { it.amount }
        InventorySortOption.CATEGORY -> filtered.sortedBy { it.category.lowercase() }
    }
}
