package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.FoodItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.ExperimentalCoroutinesApi

class SupabaseInventoryRepository(private val supabase: SupabaseClient) : InventoryRepository {

    private val tableName = "food_items"

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getInventory(): Flow<List<FoodItem>> = refreshTrigger.flatMapLatest {
        flow {
            try {
                val items = supabase.postgrest[tableName].select().decodeList<FoodItem>()
                emit(items)
            } catch (e: Exception) {
                println("Error fetching: ${e.message}")
                emit(emptyList())
            }
        }
    }

    override suspend fun addFoodItem(item: FoodItem) {
        try {
            supabase.postgrest[tableName].insert(item)
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
