package com.example.foodmanager.domain.useCase

import com.example.foodmanager.data.repository.InventoryRepository
import com.example.foodmanager.data.repository.ShoppingRepository
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.ui.utils.CategoryConstants
import kotlinx.coroutines.flow.first

// Handles when an item is marked as bought
class MarkAsBoughtUseCase(
    private val shoppingRepository: ShoppingRepository,
    private val inventoryRepository: InventoryRepository
) {
    suspend operator fun invoke(item: ShoppingItem) {
        // Removing from shopping list
        item.id?.let { shoppingRepository.deleteShoppingItem(it) }

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
                id = null,
                inventoryId = "inv_1",
                name = item.name,
                expiryDate = CategoryConstants.getDefaultExpiryDate(item.category),
                amount = item.amount,
                unit = item.unit,
                category = item.category,
            )
            inventoryRepository.addFoodItem(newFoodItem)
        }
    }
}
