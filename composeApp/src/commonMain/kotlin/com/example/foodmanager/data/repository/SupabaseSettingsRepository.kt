package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.Household
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns // REQUIRED for the join query
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
            throw e
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
            throw e
        }
    }

    override suspend fun joinHousehold(joinCode: String) {
        try {
            supabase.postgrest.rpc(
                "join_household_by_code",
                buildJsonObject {
                    put("code_input", joinCode.uppercase())
                }
            )

            refreshTrigger.tryEmit(Unit)

            val newHouseholdList = supabase.postgrest[tableName].select {
                filter {
                    eq("joinCode", joinCode.uppercase())
                }
            }.decodeList<Household>()

            val joinedHousehold = newHouseholdList.firstOrNull()

            if (joinedHousehold != null) {
                _currentHousehold.value = joinedHousehold
            } else {
                throw Exception("Invalid Code")
            }

        } catch (e: Exception) {
            println("Supabase error joining a household: ${e.message}")
            throw e
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
            throw e
        }
    }

    // Fetches members using your user_household join table
    override suspend fun getHouseholdMembers(householdId: String): List<HouseholdMember> {
        return try {
            val joinRecords = supabase.postgrest["user_household"].select(
                columns = Columns.raw("*, users(*)")
            ) {
                filter {
                    eq("household_id", householdId)
                }
            }.decodeList<UserHouseholdJoin>()

            joinRecords.mapNotNull { record ->
                record.users?.let { user ->
                    HouseholdMember(
                        id = user.id,
                        email = user.email,
                        name = user.username,
                        role = record.role
                    )
                }
            }
        } catch (e: Exception) {
            println("Supabase Error fetching members: ${e.message}")
            emptyList()
        }
    }

    // 👇 Added the removeMember function here 👇
    override suspend fun removeMember(memberId: String) {
        val currentHouseholdId = _currentHousehold.value?.id ?: return

        try {
            // Delete from the join table where the user matches AND the household matches
            supabase.postgrest["user_household"].delete {
                filter {
                    eq("user_id", memberId)
                    eq("household_id", currentHouseholdId)
                }
            }
        } catch (e: Exception) {
            println("Supabase Error removing member: ${e.message}")
            throw e
        }
    }
}

// --- Data Classes ---

@Serializable
private data class HouseholdInsert(val name: String)

@Serializable
private data class UpdateHouseholdName(val name: String)

@Serializable
private data class UpdatingJoinCode(val joinCode: String)

// --- Member Data Classes ---

@Serializable
data class UserDto(
    val id: String,
    val email: String? = null,
    val username: String? = null
)

@Serializable
data class UserHouseholdJoin(
    val user_id: String,
    val household_id: String,
    val role: String,
    val users: UserDto? = null
)

@Serializable
data class HouseholdMember(
    val id: String,
    val email: String?,
    val name: String?,
    val role: String?
)