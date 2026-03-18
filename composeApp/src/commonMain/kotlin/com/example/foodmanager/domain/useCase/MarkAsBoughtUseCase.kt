package com.example.foodmanager.domain.useCase

import com.example.foodmanager.data.repository.InventoryRepository
import com.example.foodmanager.data.repository.ShoppingListRepository
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.model.ShoppingItem
import kotlinx.coroutines.flow.first

// Handles when an item is marked as bought
class MarkAsBoughtUseCase(
    private val shoppingRepository: ShoppingListRepository,
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(item: ShoppingItem) {
        // Removing from shopping list
        shoppingRepository.deleteShoppingItem(item.id)

        // Obtaining the current inventory
        val currentPantry = inventoryRepository.getInventory().first()

        // Looking for the same item
        val existingItem = currentPantry.find { foodItem: FoodItem ->
            foodItem.name.equals(item.name, ignoreCase = true)
        }

        // If item exists, we update amount, if not, we create a new inventory entry
        if (existingItem != null) {
            val updatedItem = existingItem.copy(
                amount = existingItem.amount + item.amount
            )
            inventoryRepository.updateFoodItem(updatedItem)
        } else {
            val newFoodItem = FoodItem(
                id = (1000..9999).random(),
                inventoryId = "inv_1",
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