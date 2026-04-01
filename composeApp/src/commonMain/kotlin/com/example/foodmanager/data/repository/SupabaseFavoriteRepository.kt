package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.favoriteKey
import com.example.foodmanager.domain.model.FavoriteFoodItem
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow

class SupabaseFavoriteRepository(
    private val supabase: SupabaseClient,
    private val settingsRepository: SettingsRepository
) : FavoriteRepository {

    private val tableName = "favorite_items"
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun getFavoriteItems(): Flow<List<FavoriteFoodItem>> =
        combine(refreshTrigger, settingsRepository.getCurrentHousehold) { _, household ->
            household
        }.flatMapLatest { currentHousehold ->
            flow {
                if (currentHousehold == null) {
                    emit(emptyList())
                    return@flow
                }

                try {
                    val favorites = supabase.postgrest[tableName].select {
                        filter { eq("household_id", currentHousehold.id) }
                    }.decodeList<FavoriteFoodItem>()

                    emit(favorites.sortedBy { it.name.lowercase() })
                } catch (e: Exception) {
                    println("Error fetching favorite items: ${e.message}")
                    emit(emptyList())
                }
            }
        }

    override suspend fun addFavoriteItem(item: FavoriteFoodItem) {
        try {
            val householdId = item.householdId
                ?: settingsRepository.getCurrentHousehold.firstOrNull()?.id
                ?: error("No current household")

            val existingFavorites = supabase.postgrest[tableName].select {
                filter {
                    eq("household_id", householdId)
                }
            }.decodeList<FavoriteFoodItem>()

            val requestedKey = favoriteKey(item.name, item.unit, item.category)
            val existingFavorite = existingFavorites.firstOrNull {
                favoriteKey(it.name, it.unit, it.category) == requestedKey
            }

            if (existingFavorite == null) {
                supabase.postgrest[tableName].insert(item.copy(id = null, householdId = householdId))
                refreshTrigger.emit(Unit)
            }
        } catch (e: Exception) {
            println("Error adding favorite item: ${e.message}")
            throw e
        }
    }

    override suspend fun deleteFavoriteItem(id: String) {
        try {
            supabase.postgrest[tableName].delete {
                filter { eq("id", id) }
            }
            refreshTrigger.emit(Unit)
        } catch (e: Exception) {
            println("Error deleting favorite item: ${e.message}")
            throw e
        }
    }
}
