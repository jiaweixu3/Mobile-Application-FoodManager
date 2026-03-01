package com.example.foodmanager.domain

import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.repository.InventoryRepository
import com.example.foodmanager.repository.ShoppingListRepository

class MarkAsBoughtUseCase(
    private val shoppingRepo: ShoppingListRepository,
    private val inventoryRepo: InventoryRepository
) {
    suspend fun execute(shoppingItem: ShoppingItem) {
        // 1. Remove from Shopping List repository
        shoppingRepo.deleteShoppingItem(shoppingItem.id)

        // 2. Map the data to a new FoodItem
        val foodItem = FoodItem(
            id = (0..Int.MAX_VALUE).random(),
            name = shoppingItem.name,
            amount = shoppingItem.amount,
            unit = shoppingItem.unit,
            category = shoppingItem.category,
            expiryDate = "2026-12-31" // Default placeholder for now
        )

        // 3. Push to the Inventory repository
        inventoryRepo.addFoodItem(foodItem)
    }
}