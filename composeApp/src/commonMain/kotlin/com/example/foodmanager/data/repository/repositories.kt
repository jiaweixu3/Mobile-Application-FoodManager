package com.example.foodmanager.data.repository

// This file defines the basic interfaces and the used functions
import com.example.foodmanager.data.MockDb
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine


// Inventory Repository
interface InventoryRepository {
    fun getInventory(): Flow<List<FoodItem>> // Flow allows Compose to update automatically

    // Suspend allows for the app to work while an action is being done in the background
    suspend fun addFoodItem(newItem: FoodItem)
    suspend fun deleteFoodItem(id: Int)
    suspend fun updateFoodItem(updatedItem: FoodItem)
}


// Shopping List Inventory
interface ShoppingRepository {
    fun getShoppingList(): Flow<List<ShoppingItem>>

    suspend fun addShoppingItem(newShoppingItem: ShoppingItem)
    suspend fun deleteShoppingItem(id: Int)
    suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem)
}

// Settings Screen
interface SettingsRepository {
    // List of households
    fun getHouseholdsList(): Flow<List<Household>>

    // Current displayed household
    val getCurrentHousehold: Flow<Household?>

    // Storing the current selection
    suspend fun storeHousehold(household: Household)

    // Adding a new household
    suspend fun addHousehold(newHousehold: Household)

    // Sharing a household, for now its only via email
    suspend fun shareHousehold(householdId: String, email: String)
}


// Creating the classes that implement the interfaces

class MockInventoryRepository : InventoryRepository {
    // As there is inheritance, we have to override
    // We will use the different previously created functions

    // For the inventory, we also have to retrieve the current household
    override fun getInventory(): Flow<List<FoodItem>> {
        return combine(MockDb.currentHousehold, MockDb.fooditems){ actualHousehold, allItems ->
            // If there is no household, we do not return anything
            if (actualHousehold == null) return@combine emptyList()

            // Searching the inventory of the current household
            val actualInventory = MockDb.inventories.find { it.household_id == actualHousehold.id }

            // If inventory is empty, return empty list
            if (actualInventory == null) return@combine emptyList()

            // Returning the current list of items
            allItems.filter{it.inventoryId == actualInventory.id}
        }
    }

    override suspend fun addFoodItem(newItem: FoodItem) {
        // We have to add the invnetory of the new item

        // Obtaining the current Household, returning if household is empty
        val currentHouse = MockDb.currentHousehold.value ?: return

        // Obtaining the inventory
        val currentInventory = MockDb.inventories.find { it.household_id == currentHouse.id }?:return

        // Updating the item id
        val newUpdatedItem = newItem.copy(inventoryId = currentInventory.id)

        MockDb.addFoodItem(newUpdatedItem)
    }

    override suspend fun deleteFoodItem(id: Int) {
        MockDb.deleteFoodItem(item_id = id)
    }

    override suspend fun updateFoodItem(updatedItem: FoodItem) {
        MockDb.updateFoodItem(updatedItem)
    }
}

    // Shopping Repository
    class MockShoppingRepository : ShoppingRepository {
    // For the shopping list we also have to retrieve the current household
    override fun getShoppingList(): Flow<List<ShoppingItem>> {
        return combine(MockDb.currentHousehold, MockDb.shoppingitems){actualHousehold, allShoppingItems ->
        // If there is no household, we do not return anything
        if (actualHousehold == null) return@combine emptyList()

        val actualShoppingList = MockDb.shoppingLists.find { it.household_id == actualHousehold.id }

        // If list does not exist, return empty list
        if (actualShoppingList == null) return@combine emptyList()

            allShoppingItems.filter { it.shopping_list_id == actualShoppingList.id }
        }
    }

    override suspend fun addShoppingItem(newShoppingItem: ShoppingItem) {
        // We have to add the specific shopping list for the shopping item

        // Obtaining the current Household, returning if household is empty
        val currentHouse = MockDb.currentHousehold.value ?: return

        // Obtaining the shopping list
        val currentShoppingList = MockDb.shoppingLists.find { it.household_id == currentHouse.id }?:return

        // Updating the shopping list id
        val newUpdatedShoppingItem = newShoppingItem.copy(shopping_list_id = currentShoppingList.id )
        MockDb.addShoppingItem(newUpdatedShoppingItem)
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

    override fun getHouseholdsList(): Flow<List<Household>> {
        return MockDb.households
    }

    override val getCurrentHousehold = MockDb.currentHousehold

    override suspend fun storeHousehold(household: Household) {
        MockDb.storeHousehold(household)
    }

    override suspend fun addHousehold(newHousehold: Household) {
        MockDb.addHousehold(newHousehold)
    }

    override suspend fun shareHousehold(householdId: String, email: String) {
        TODO("Not yet implemented")
    }

}
