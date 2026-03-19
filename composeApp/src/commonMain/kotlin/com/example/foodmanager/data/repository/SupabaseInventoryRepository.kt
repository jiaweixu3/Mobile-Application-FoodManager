package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.model.Inventory
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull


class SupabaseInventoryRepository(
    private val supabase: SupabaseClient,
    private val settingsRepository: SettingsRepository) : InventoryRepository {

    private val tableName = "food_items"

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getInventory(): Flow<List<FoodItem>>  =
        // Combining with current household
        combine(refreshTrigger,settingsRepository.getCurrentHousehold){_, household ->
            household
        }.flatMapLatest { currentHousehold ->

            flow {
                // Handling no current household
                if (currentHousehold == null){
                    emit(emptyList())
                    return@flow
                }
                try {
                    // Finding the inventory of the specific household
                    val inventory = supabase.postgrest["inventories"].select{
                        filter { eq("household_id", currentHousehold.id) }
                    }.decodeSingleOrNull<Inventory>()

                    // Handling empty inventories
                    if (inventory == null) {
                        emit(emptyList())
                        return@flow
                    }

                    // Otherwise, obtaining food items for that specific inventory
                    val items = supabase.postgrest[tableName].select{
                        filter { eq("inventory_id", inventory.id) }
                    }.decodeList<FoodItem>()

                    emit(items)
                } catch (e: Exception) {
                    println("Error fetching: ${e.message}")
                    emit(emptyList())
                }
            }
        }


    override suspend fun addFoodItem(item: FoodItem) {
        try {
            // Obtaining the current household
            val currentHousehold = settingsRepository.getCurrentHousehold.firstOrNull()?: error("No active household")

            // Finding the correct inventory
            val inventory = supabase.postgrest["inventories"].select {
                filter { eq("household_id", currentHousehold.id) }
            }.decodeSingleOrNull<Inventory>() ?: error("No active inventory")

            // Applying the correct inventory to the food item
            val itemToAdd = item.copy(inventoryId = inventory.id)

            // Adding the new item
            supabase.postgrest[tableName].insert(itemToAdd)
            refreshTrigger.emit(Unit)
        } catch (e: Exception) {
            println("Error adding: ${e.message}")
        }
    }

    override suspend fun updateFoodItem(item: FoodItem) {
        try {
            supabase.postgrest[tableName].update(item) {
                filter {
                    item.id?.let { eq("id", it) }
                }
            }
            refreshTrigger.emit(Unit)
        } catch (e: Exception) {
            println("Error updating: ${e.message}")
        }
    }

    override suspend fun deleteFoodItem(itemId: Int) {
        try {
            supabase.postgrest[tableName].delete {
                filter {
                    eq("id", itemId)
                }
            }
            refreshTrigger.emit(Unit)
        } catch (e: Exception) {
            println("Error deleting: ${e.message}")
        }
    }
}