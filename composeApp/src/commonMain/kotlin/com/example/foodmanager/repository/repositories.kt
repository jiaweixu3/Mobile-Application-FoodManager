package com.example.foodmanager.repository

// This file defines the basic interfaces and the used functions
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.model.FoodItem
import kotlinx.coroutines.flow.Flow

// Inventory Repository
interface InventoryRepository {
    fun getInventory(): Flow<List<FoodItem>> // Flow allows Compose to update automatically

    // Suspend allows for the app to work while an action is being done in the background
    suspend fun addIFoodItem(shoppingItem: FoodItem)
    suspend fun deleteFoodItem(id: String)
    suspend fun updateFoodItem(shoppingItem: FoodItem)
}


// Shopping List Inventory
interface  ShoppingListRepository {
    fun getShoppingList(): Flow<List<ShoppingItem>>

    suspend fun addShoppingItem(shoppingItem: ShoppingItem)
    suspend fun deleteShoppingItem(id: String)
    suspend fun updateShoppingItem(shoppingItem: ShoppingItem)
}