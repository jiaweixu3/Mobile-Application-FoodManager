package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.ShoppingItem
import io.github.jan.supabase.postgrest.Postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class SupabaseShoppingRepository(
    private val postgrest: Postgrest
) : ShoppingRepository {

    // Removed the @OptIn and real-time flow
    override fun getShoppingList(): Flow<List<ShoppingItem>> = flow {
        // Fetch from the database
        val items = postgrest.from("shopping_items")
            .select()
            .decodeList<ShoppingItem>()

        emit(items)
    }

    override suspend fun addShoppingItem(newShoppingItem: ShoppingItem) {
        try {
            // Try to insert the item
            postgrest.from("shopping_items").insert(newShoppingItem)
        } catch (e: Exception) {
            // If it fails, print the EXACT reason to your Run window!
            println("SUPABASE ERROR: ${e.message}")
            throw e // Keep this so the UI still knows it failed
        }
    }

    override suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        // Updates the item where the ID matches
        postgrest.from("shopping_items").update(updatedShoppingItem) {
            filter {
                eq("id", updatedShoppingItem.id ?: "")
            }
        }
    }

    override suspend fun deleteShoppingItem(id: String) {
        // Deletes the row with the matching ID
        postgrest.from("shopping_items").delete {
            filter {
                eq("id", id)
            }
        }
    }
}