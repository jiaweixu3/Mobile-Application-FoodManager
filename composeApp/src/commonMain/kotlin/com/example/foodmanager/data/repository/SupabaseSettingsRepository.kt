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
            // Obtaining the current user
            val currentUser = supabase.auth.currentUserOrNull()?: throw Exception("User not logged in")

            val insertHousehold = HouseholdInsert(name = newHousehold.name)

            // Obtaining the new inserted household
            val insertedHousehold = supabase.postgrest[tableName]
                .insert(insertHousehold){
                    select()
                }.decodeSingle<Household>()

            // The new household owner is the current user
            val householdOwner = InsertUserHousehold(
                user_id = currentUser.id,
                household_id = insertedHousehold.id,
                role = "owner"
            )

            // Inserting it into the actual supabase table
            supabase.postgrest["USER_HOUSEHOLD"].insert(householdOwner)

            // Updating current household
            _currentHousehold.value = insertedHousehold

            refreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR adding household: ${e.message}")

        }
    }



    // Updating the name of a household
    override suspend fun updateHouseholdName(householdId:String, newName: String) {
        try{
            // Data we want to change
            val updateData = UpdateHouseholdName(name = newName)

            // Updating the actual table
            supabase.postgrest[tableName].update(updateData){
                filter{
                    eq("id", householdId)
                }
            }

            // Refreshing the dropdown list
            refreshTrigger.tryEmit(Unit)

            // Updating the name of the household
            val currentHousehold = _currentHousehold.value
            if (currentHousehold != null && currentHousehold.id == householdId){
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
    val join_code: String
)

// Inserting a new user to a household
private data class InsertUserHousehold(
    val user_id: String,
    var household_id: String,
    var role: String = "editor"
)