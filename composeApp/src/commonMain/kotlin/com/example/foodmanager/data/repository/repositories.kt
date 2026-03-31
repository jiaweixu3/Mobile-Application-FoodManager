package com.example.foodmanager.data.repository

// This file defines the basic interfaces and the used functions
import com.example.foodmanager.data.MockDb
import com.example.foodmanager.domain.model.FavoriteFoodItem
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.model.Household
import com.example.foodmanager.domain.model.HouseholdMember
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlin.random.Random

sealed class HouseholdJoinResult {
    data class Success(val household: Household) : HouseholdJoinResult()
    data class Error(val message: String) : HouseholdJoinResult()
}


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
    suspend fun deleteShoppingItem(id: Long)
    suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem)
}

interface FavoriteRepository {
    fun getFavoriteItems(): Flow<List<FavoriteFoodItem>>

    suspend fun addFavoriteItem(item: FavoriteFoodItem)
    suspend fun deleteFavoriteItem(id: String)
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

    // Updating the name of a household
    suspend fun updateHouseholdName(householdId:String, newName: String)

    // Generating the code for joining
    suspend fun generateCode(householdId: String): String

    // Joining a new household
    suspend fun joinHousehold(joinCode: String): HouseholdJoinResult

    // Reading the current selection synchronously inside repositories
    suspend fun getCurrentHouseholdValue(): Household?
    suspend fun getCurrentUserId(): String?

    fun getCurrentHouseholdMembers(): Flow<List<HouseholdMember>>
    suspend fun deleteHouseholdMember(memberId: String)
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

    override suspend fun deleteShoppingItem(id: Long) {
        MockDb.deleteShoppingItem(id)
    }

    override suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        MockDb.updateShoppingItem(updatedShoppingItem)
    }
}

class InMemoryFavoriteRepository(
    private val settingsRepository: SettingsRepository
) : FavoriteRepository {
    override fun getFavoriteItems(): Flow<List<FavoriteFoodItem>> {
        return combine(settingsRepository.getCurrentHousehold, MockDb.favoriteItems) { actualHousehold, allFavorites ->
            val householdId = actualHousehold?.id
            allFavorites.filter { it.householdId == householdId }
        }
    }

    override suspend fun addFavoriteItem(item: FavoriteFoodItem) {
        val householdId = item.householdId ?: settingsRepository.getCurrentHouseholdValue()?.id
        MockDb.addFavoriteItem(
            item.copy(
                id = item.id ?: Random.nextLong().toString(),
                householdId = householdId
            )
        )
    }

    override suspend fun deleteFavoriteItem(id: String) {
        MockDb.deleteFavoriteItem(id)
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

    override suspend fun updateHouseholdName(householdId: String, newName: String) {
        TODO("Not yet implemented")
    }

    override suspend fun generateCode(householdId: String): String {
        TODO("Not yet implemented")
    }

    override suspend fun joinHousehold(joinCode: String): HouseholdJoinResult {
        TODO("Not yet implemented")
    }

    override suspend fun getCurrentHouseholdValue(): Household? {
        return MockDb.currentHousehold.value
    }

    override suspend fun getCurrentUserId(): String? {
        return "mock_user_1"
    }

    override fun getCurrentHouseholdMembers(): Flow<List<HouseholdMember>> {
        return combine(MockDb.currentHousehold, MockDb.householdMembers) { household, members ->
            val householdId = household?.id ?: return@combine emptyList()
            members.filter { it.householdId == householdId }
        }
    }

    override suspend fun deleteHouseholdMember(memberId: String) {
        MockDb.deleteHouseholdMember(memberId)
    }

}
