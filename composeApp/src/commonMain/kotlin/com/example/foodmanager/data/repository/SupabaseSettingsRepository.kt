package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.Household
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject

class SupabaseSettingsRepository(private val supabase: SupabaseClient) : SettingsRepository {
    private val tableName = "households"

    private val _currentHousehold = MutableStateFlow<Household?>(null)
    override val getCurrentHousehold: Flow<Household?> = _currentHousehold

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun getHouseholdsList(): Flow<List<Household>> = refreshTrigger.flatMapLatest {
        flow {
            try {
                val households = supabase.postgrest[tableName].select().decodeList<Household>()
                emit(households)
            } catch (e: Exception) {
                println("Exception while fetching households: ${e.message}")
                emit(emptyList())
            }
        }
    }

    override suspend fun storeHousehold(household: Household) {
        _currentHousehold.value = household
    }

    override suspend fun addHousehold(newHousehold: Household) {
        try {
            val insertHousehold = HouseholdInsert(name = newHousehold.name)
            val insertedHousehold = supabase.postgrest[tableName]
                .insert(insertHousehold) {
                    select()
                }.decodeSingle<Household>()

            _currentHousehold.value = insertedHousehold
            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR adding household: ${e.message}")
            throw e // Pass error to ViewModel
        }
    }

    override suspend fun generateCode(householdId: String): String {
        return try {
            val code = (1..6).map { ('A'..'Z').random() }.joinToString("")
            supabase.postgrest["households"].update(UpdatingJoinCode(code)) {
                filter { eq("id", householdId) }
            }

            val current = _currentHousehold.value
            if (current != null && current.id == householdId){
                _currentHousehold.value = current.copy(joinCode = code)
            }

            refreshTrigger.tryEmit(Unit)
            code
        } catch (e: Exception) {
            println("Supabase Error generating a code: ${e.message}")
            throw e // Pass error to ViewModel
        }
    }

    // FIXED: Now throws the error so the ViewModel can see it!
    override suspend fun joinHousehold(joinCode: String) {
        try {
            val result = supabase.postgrest.rpc(
                "join_household_by_code",
                buildJsonObject {
                    put("code_input", joinCode.uppercase())
                })

            val household = result.decodeSingle<Household>()
            _currentHousehold.value = household
            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("Supabase error joining a household: ${e.message}")
            throw e // This allows the 'catch' in SettingsViewModel to work!
        }
    }

    override suspend fun updateHouseholdName(householdId: String, newName: String) {
        try {
            val updateData = UpdateHouseholdName(name = newName)
            supabase.postgrest[tableName].update(updateData) {
                filter { eq("id", householdId) }
            }

            refreshTrigger.tryEmit(Unit)

            val currentHousehold = _currentHousehold.value
            if (currentHousehold != null && currentHousehold.id == householdId) {
                _currentHousehold.value = currentHousehold.copy(name = newName)
            }
        } catch (e: Exception) {
            println("Exception while updating household: ${e.message}")
            throw e // Pass error to ViewModel
        }
    }
}

@Serializable
private data class HouseholdInsert(val name: String)

@Serializable
private data class UpdateHouseholdName(val name: String)

@Serializable
private data class UpdatingJoinCode(val joinCode: String)