package com.example.foodmanager.domain.useCase

import com.example.foodmanager.data.repository.InventoryRepository
import com.example.foodmanager.data.repository.ShoppingRepository
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.model.ShoppingItem

// This file handles when a user consumes a food item
class ConsumeFoodItemUseCase(
    private val inventoryRepository: InventoryRepository,
    private val shoppingRepository: ShoppingRepository
) {
    suspend operator fun invoke(
        foodItem: FoodItem,
        amountConsumed: Double,
        addToShoppingList: Boolean,
        shoppingQuantity: Double = 1.0
    ) {
        val newAmount = foodItem.amount - amountConsumed

        // Update or Delete from Inventory depending on the current amount
        if (newAmount <= 0.0) {
            inventoryRepository.deleteFoodItem(foodItem.id ?: 0)
        } else {
            val updatedFoodItem = foodItem.copy(amount = newAmount)
            inventoryRepository.updateFoodItem(updatedFoodItem)
        }

        // If user asks to add it to the shopping list, we include it
        if (addToShoppingList) {
            val newShoppingItem = ShoppingItem(
                shopping_list_id = "",
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
