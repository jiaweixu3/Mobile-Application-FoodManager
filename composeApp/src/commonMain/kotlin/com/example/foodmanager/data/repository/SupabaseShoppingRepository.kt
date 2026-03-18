package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.ShoppingItem
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

class SupabaseShoppingRepository(
    private val postgrest: Postgrest
) : ShoppingRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getShoppingList(): Flow<List<ShoppingItem>> = refreshTrigger.flatMapLatest {
        flow {
            val shoppingListId = resolveShoppingListId()
            if (shoppingListId == null) {
                emit(emptyList())
                return@flow
            }

            val items = postgrest.from("shopping_items")
                .select {
                    filter {
                        eq("shopping_list_id", shoppingListId)
                    }
                }
                .decodeList<ShoppingItem>()

            emit(items)
        }
    }

    override suspend fun addShoppingItem(newShoppingItem: ShoppingItem) {
        try {
            val shoppingListId = resolveShoppingListId()
                ?: error("No shopping list exists for the current user.")

            val itemToInsert = newShoppingItem.copy(shopping_list_id = shoppingListId)
            postgrest.from("shopping_items").insert(itemToInsert)
            refreshTrigger.emit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR: ${e.message}")
            throw e
        }
    }

    override suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        val itemId = updatedShoppingItem.id ?: error("Cannot update shopping item without an ID.")
        postgrest.from("shopping_items").update(updatedShoppingItem) {
            filter {
                eq("id", itemId)
            }
        }
        refreshTrigger.emit(Unit)
    }

    override suspend fun deleteShoppingItem(id: Int) {
        postgrest.from("shopping_items").delete {
            filter {
                eq("id", id)
            }
        }
        refreshTrigger.emit(Unit)
    }

    private suspend fun resolveShoppingListId(): String? {
        val shoppingLists = postgrest.from("shopping_lists")
            .select()
            .decodeList<ShoppingListRecord>()

        return shoppingLists.firstOrNull()?.id
    }
}

@Serializable
private data class ShoppingListRecord(
    val id: String
)
