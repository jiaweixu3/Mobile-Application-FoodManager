package com.example.foodmanager.data.repository


import com.example.foodmanager.domain.model.ShoppingItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

// Supabase declaration for Shopping class
class SupabaseShoppingRepository(
    private val supabase: SupabaseClient,
    private val settingsRepository: SettingsRepository
) : ShoppingRepository {

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getShoppingList(): Flow<List<ShoppingItem>> =
        // Combining with current household
        combine(refreshTrigger, settingsRepository.getCurrentHousehold){_, household ->
            household
        }.flatMapLatest { currentHousehold ->
            flow {
                // Handling empty cases
                if (currentHousehold == null) {
                    emit(emptyList())
                    return@flow
                }
                // Obtaining the shopping list from Supabase
                try{
                    val shoppingListId = resolveShoppingListId(currentHousehold.id)
                    if (shoppingListId == null) {
                        emit(emptyList())
                        return@flow
                    }

                    val items = supabase.postgrest["shopping_items"]
                        .select {
                            filter {
                                eq("shopping_list_id", shoppingListId)
                            }
                        }
                        .decodeList<ShoppingItem>()

                    emit(items)
            } catch ( e: Exception ){
                println("Unable to fetch shopping list")
                    emit(emptyList())
            }
        }}


    // Adding a shopping item
    override suspend fun addShoppingItem(newShoppingItem: ShoppingItem) {
        try {
            // Obtaining the current household
            val currentHousehold = settingsRepository.getCurrentHousehold.firstOrNull()?: error("No current household")

            // Obtaining the shopping list
            val shoppingListId = resolveShoppingListId(currentHousehold.id)?: error("No shopping list exists for the current user.")

            val itemToInsert = newShoppingItem.copy(shopping_list_id = shoppingListId)

            supabase.postgrest["shopping_items"].insert(itemToInsert)
            refreshTrigger.emit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR: ${e.message}")
            throw e
        }
    }

    // Updating a shopping item
    override suspend fun updateShoppingItem(updatedShoppingItem: ShoppingItem) {
        try {
            val itemId = updatedShoppingItem.id ?: error("Cannot update shopping item without an ID.")

            val updatePayload = mapOf("is_checked" to updatedShoppingItem.isChecked)

            supabase.postgrest["shopping_items"].update(updatePayload) {
                filter {
                    eq("id", itemId)
                }
            }

        } catch ( e: Exception ){
            println("Error updating shopping item ${updatedShoppingItem.id}: ${e.message}")
            e.printStackTrace()
        }
    }

    // Deleting shopping item
    override suspend fun deleteShoppingItem(id: Long) {
        try{
            supabase.postgrest["shopping_items"].delete {
                filter { eq("id", id) }
            }
            refreshTrigger.emit(Unit)
        } catch (e:Exception){
            println("Error Deleting Shopping Item")
        }
    }

    // Returning the id of a shopping list
    private suspend fun resolveShoppingListId(householdId:String ): String? {
        val shoppingLists =supabase.postgrest["shopping_lists"].select {
            filter{ eq("household_id", householdId)}
        }.decodeSingleOrNull<ShoppingListRecord>()

        return shoppingLists?.id
    }
}

@Serializable
private data class ShoppingListRecord(
    val id: String
)
