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

    // Adding a new household, the creator automatically becomes the owner
    override suspend fun addHousehold(newHousehold: Household) {
        try {

            val insertHousehold = HouseholdInsert(name = newHousehold.name)

            // Obtaining the new inserted household
            val insertedHousehold = supabase.postgrest[tableName]
                .insert(insertHousehold) {
                    select()
                }.decodeSingle<Household>()

            // Updating current household
            _currentHousehold.value = insertedHousehold

            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR adding household: ${e.message}")

        }
    }

    // Function for generating a code to then join a table
    override suspend fun generateCode(householdId: String): String {
        return try {
            // Generating a random string of length 6, this length could be changed
            val code = (1..6).map { ('A'..'Z').random() }.joinToString("")

            // Saving it in the database
            supabase.postgrest["households"].update(UpdatingJoinCode(code)) {
                filter { eq("id", householdId) }
            }

            // Updating code in the app
            val current = _currentHousehold.value
            if (current != null && current.id == householdId){
                _currentHousehold.value = current.copy(joinCode = code)
            }

            // Refreshing
            refreshTrigger.tryEmit(Unit)
            code
        } catch (e: Exception) {
            println("Supabase Error generating a code ${e.message}")
            ""
        }
    }

    // Function for joining a household using a code
    override suspend fun joinHousehold(joinCode: String) {
        try {
            // Calling the own supabase function for handling joining the household
            val result = supabase.postgrest.rpc(
                "join_household_by_code",
                buildJsonObject { // We have to pass a json
                    put("code_input", joinCode.uppercase())
                })

            // Updating current household
            val household = result.decodeSingle<Household>()
            _currentHousehold.value = household

            // Refreshing
            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("Supabase error joining a household")


        }
    }


    // Updating the name of a household
    override suspend fun updateHouseholdName(householdId: String, newName: String) {
        try {
            // Data we want to change
            val updateData = UpdateHouseholdName(name = newName)

            // Updating the actual table
            supabase.postgrest[tableName].update(updateData) {
                filter {
                    eq("id", householdId)
                }
            }

            // Refreshing the dropdown list
            refreshTrigger.tryEmit(Unit)

            // Updating the name of the household
            val currentHousehold = _currentHousehold.value
            if (currentHousehold != null && currentHousehold.id == householdId) {
                _currentHousehold.value = currentHousehold.copy(name = newName)
            }
        } catch (e: Exception) {
            println("Exception while updating household: ${e.message}")

        }
    }
}

// HELPER FUNCTIONS
// Handling inserting UUID
@Serializable
private data class HouseholdInsert(
    val name: String
)

// Hnadling updating the name
@Serializable
private data class UpdateHouseholdName(
    val name: String
)

// Updating the code for joining another database
@Serializable
private data class UpdatingJoinCode(
    val joinCode: String
)


