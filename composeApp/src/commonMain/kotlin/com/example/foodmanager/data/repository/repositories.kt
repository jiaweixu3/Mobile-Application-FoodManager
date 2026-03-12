package com.example.foodmanager.data.repository

// This file defines the basic interfaces and the used functions
import com.example.foodmanager.data.MockDb
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.domain.model.FoodItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

// Inventory Repository
interface InventoryRepository {
    fun getInventory(): Flow<List<FoodItem>> // Flow allows Compose to update automatically

    // Suspend allows for the app to work while an action is being done in the background
    suspend fun addFoodItem(newItem: FoodItem)
    suspend fun deleteFoodItem(id: Int)
    suspend fun updateFoodItem(updatedItem: FoodItem)
}


// Shopping List Inventory
interface ShoppingListRepository {
    fun getShoppingList(): Flow<List<ShoppingItem>>

    suspend fun addShoppingItem(newShoppingItem: ShoppingItem)
    suspend fun deleteShoppingItem(id: Int)
    suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem)
}

// Settings Screen
interface SettingsRepository {
    // List of households
    fun getHouseholdsList(): Flow<List<String>>

    // Current displayed household
    val getCurrentHousehold: Flow<String?>

    // Storing the current selection
    suspend fun storeHousehold(household: String)
}


// Creating the classes that implement the interfaces

class MockInventoryRepository : InventoryRepository {
    // As there is inheritance, we have to override
    // We will use the different previously created functions
    override fun getInventory(): Flow<List<FoodItem>> {
        return MockDb.fooditems
    }

    override suspend fun addFoodItem(newItem: FoodItem) {
        MockDb.addFoodItem(newItem)
    }

    override suspend fun deleteFoodItem(id: Int) {
        MockDb.deleteFoodItem(item_id = id)
    }

    override suspend fun updateFoodItem(updatedItem: FoodItem) {
        MockDb.updateFoodItem(updatedItem)
    }
}

// Shopping Repository
class MockShoppingRepository : ShoppingListRepository {
    override fun getShoppingList(): Flow<List<ShoppingItem>> {
        return MockDb.shoppingitems
    }

    override suspend fun addShoppingItem(newShoppingItem: ShoppingItem) {
        MockDb.addShoppingItem(newShoppingItem)
    }

    override suspend fun deleteShoppingItem(id: Int) {
        MockDb.deleteShoppingItem(id)
    }

    override suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        MockDb.updateShoppingItem(updatedShoppingItem)
    }
}

// Settings Repository
class MockSettingsRepository : SettingsRepository {

    override fun getHouseholdsList(): Flow<List<String>> {
        return MockDb.householditems
    }

    override val getCurrentHousehold = MockDb.currentHousehold

    override suspend fun storeHousehold(household: String) {
        MockDb.storeHousehold(household)
    }

}
