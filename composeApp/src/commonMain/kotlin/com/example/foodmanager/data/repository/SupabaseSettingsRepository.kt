package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.Household
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable

// Supabase declaration for settings screen
class SupabaseSettingsRepository(private val supabase: SupabaseClient) : SettingsRepository {
    private val tableName = "households"

    // Storing the currently active household, null until selected otherwise
    private val _currentHousehold = MutableStateFlow<Household?>(null)
    override val getCurrentHousehold: Flow<Household?> = _currentHousehold

    // Updating the app continuously and refreshing automatically
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // Obtaining the list of households
    override fun getHouseholdsList(): Flow<List<Household>> = refreshTrigger.flatMapLatest {
        flow {
            try {
                // Obtaining households from Supabase
                val households = supabase.postgrest[tableName].select().decodeList<Household>()
                emit(households)

            } catch (e: Exception) { //Handling errors
                println("Exception while fetching households")
                emit(emptyList())
            }
        }
    }

    // Storing the current household
    override suspend fun storeHousehold(household: Household) {
        _currentHousehold.value = household
    }

    // Adding a new household
    override suspend fun addHousehold(newHousehold: Household) {
        try {
            val insertHousehold = HouseholdInsert(name = newHousehold.name)
            supabase.postgrest[tableName].insert(insertHousehold)
            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR adding household: ${e.message}")
            e.printStackTrace()
        }
    }

    // Sharing a household
    override suspend fun shareHousehold(householdId: String, email: String) {
        TODO("Not yet implemented")
    }
}

// Handling inserting UUID
@Serializable
private data class HouseholdInsert(
    val name: String
)