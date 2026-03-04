package com.example.foodmanager.domain

import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.repository.ShoppingListRepository
import com.example.foodmanager.repository.InventoryRepository
import kotlinx.coroutines.flow.first

class MarkAsBoughtUseCase(
    private val shoppingRepository: ShoppingListRepository,
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(item: ShoppingItem) {
        // 1. Remove from Shopping List
        shoppingRepository.deleteShoppingItem(item.id)

        // 2. Get current inventory using your specific method name
        // .first() takes the most recent list emitted by the Flow
        val currentPantry = inventoryRepository.getInventory().first()

        // 3. Search for a matching name
        // Explicitly naming the parameter 'foodItem' helps the compiler with type inference
        val existingItem = currentPantry.find { foodItem: FoodItem ->
            foodItem.name.equals(item.name, ignoreCase = true)
        }

        if (existingItem != null) {
            // MATCH FOUND: Merge the quantities
            val updatedItem = existingItem.copy(
                amount = existingItem.amount + item.amount
            )
            inventoryRepository.updateFoodItem(updatedItem)
        } else {
            // NO MATCH: Create a new inventory entry
            val newFoodItem = FoodItem(
                id = (1000..9999).random(),
                name = item.name,
                amount = item.amount,
                unit = item.unit,
                category = item.category,
                expiryDate = "2026-12-31"
            )
            inventoryRepository.addFoodItem(newFoodItem)
        }
    }
}