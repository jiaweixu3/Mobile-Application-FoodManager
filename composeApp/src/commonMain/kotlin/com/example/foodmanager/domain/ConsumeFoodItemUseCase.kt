package com.example.foodmanager.domain

import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.repository.InventoryRepository
import com.example.foodmanager.repository.ShoppingListRepository

class ConsumeFoodItemUseCase(
    private val inventoryRepository: InventoryRepository,
    private val shoppingRepository: ShoppingListRepository
) {
    suspend operator fun invoke(
        foodItem: FoodItem,
        amountConsumed: Double,
        addToShoppingList: Boolean,
        shoppingQuantity: Double = 1.0
    ) {
        val newAmount = foodItem.amount - amountConsumed

        // Update or Delete from Inventory
        if (newAmount <= 0.0) {
            inventoryRepository.deleteFoodItem(foodItem.id)
        } else {
            val updatedFoodItem = foodItem.copy(amount = newAmount)
            inventoryRepository.updateFoodItem(updatedFoodItem)
        }

        // REMOVED: val isLow = newAmount <= 1.0

        // UPDATED: Now we only check if the user explicitly asked to add it
        if (addToShoppingList) {
            val newShoppingItem = ShoppingItem(
                id = (1000..9999).random(),
                name = foodItem.name,
                amount = shoppingQuantity,
                unit = foodItem.unit,
                category = foodItem.category,
                isChecked = false
            )
            shoppingRepository.addShoppingItem(newShoppingItem)
        }
    }
}