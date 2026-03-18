package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.ShoppingItem
import io.github.jan_tennert.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow

class SupabaseShoppingRepository(
    private val postgrest: Postgrest
) : ShoppingRepository {

    override fun getShoppingList(): Flow<List<ShoppingItem>> {
        // This listens to your "shopping_list" table in real-time.
        // Note: Make sure Realtime is enabled for this table in your Supabase Dashboard!
        return postgrest.from("shopping_list")
            .selectAsFlow(ShoppingItem::id)
    }

    override suspend fun addShoppingItem(newShoppingItem: ShoppingItem) {
        // Inserts the new item into the database
        postgrest.from("shopping_list").insert(newShoppingItem)
    }

    override suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        // Updates the item where the ID matches
        postgrest.from("shopping_list").update(updatedShoppingItem) {
            filter {
                eq("id", updatedShoppingItem.id)
            }
        }
    }

    override suspend fun deleteShoppingItem(id: Int) {
        // Deletes the row with the matching ID
        postgrest.from("shopping_list").delete {
            filter {
                eq("id", id)
            }
        }
    }
}