package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.Household
import com.example.foodmanager.domain.model.HouseholdMember
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
    private val membersTableName = "user_household"

    // Storing the currently active household, null until selected otherwise
    private val _currentHousehold = MutableStateFlow<Household?>(null)
    override val getCurrentHousehold: Flow<Household?> = _currentHousehold

    // Updating the app continuously and refreshing automatically
    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val memberRefreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    // Obtaining the list of households
    override fun getHouseholdsList(): Flow<List<Household>> = refreshTrigger.flatMapLatest {
        flow {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    emit(emptyList())
                    return@flow
                }

                val memberships = supabase.postgrest[membersTableName].select {
                    filter {
                        eq("user_id", currentUser.id)
                    }
                }.decodeList<HouseholdMember>()

                val households = memberships.mapNotNull { membership ->
                    try {
                        supabase.postgrest[tableName].select {
                            filter {
                                eq("id", membership.householdId)
                            }
                        }.decodeSingleOrNull<Household>()
                    } catch (_: Exception) {
                        null
                    }
                }

                emit(households)

            } catch (e: Exception) { //Handling errors
                println("Exception while fetching households: ${e.message}")
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

            ensureCurrentUserMembership(insertedHousehold.id, "Owner")

            refreshTrigger.tryEmit(Unit)
            memberRefreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR adding household: ${e.message}")

        }
    }

    // Function for generating a code to then join a table
    override suspend fun generateCode(householdId: String): String {
        return try {
            // Generating a 6-digit code for joining a household
            val code = (1..6).map { ('0'..'9').random() }.joinToString("")

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
    override suspend fun joinHousehold(joinCode: String): HouseholdJoinResult {
        try {
            val normalizedCode = joinCode.trim().uppercase()

            if (normalizedCode.length != 6) {
                return HouseholdJoinResult.Error("The household code must contain 6 characters.")
            }

            val household = supabase.postgrest.rpc(
                "join_household_by_code",
                buildJsonObject {
                    put("code_input", normalizedCode)
                }
            ).decodeAs<Household>()

            _currentHousehold.value = household

            ensureCurrentUserMembership(household.id, "Member")

            // Refreshing
            refreshTrigger.tryEmit(Unit)
            memberRefreshTrigger.tryEmit(Unit)
            return HouseholdJoinResult.Success(household)
        } catch (e: Exception) {
            val message = e.message ?: "Unable to join the household."
            println("Supabase error joining a household: $message")
            return HouseholdJoinResult.Error(message)
        }
    }

    override suspend fun getCurrentHouseholdValue(): Household? {
        return _currentHousehold.value
    }

    override suspend fun getCurrentUserId(): String? {
        return supabase.auth.currentUserOrNull()?.id
    }

    override fun getCurrentHouseholdMembers(): Flow<List<HouseholdMember>> = memberRefreshTrigger.flatMapLatest {
        flow {
            val householdId = _currentHousehold.value?.id
            if (householdId == null) {
                emit(emptyList())
                return@flow
            }

            try {
                val members = supabase.postgrest[membersTableName].select {
                    filter { eq("household_id", householdId) }
                }.decodeList<HouseholdMember>()

                emit(
                    members.sortedWith(
                        compareBy<HouseholdMember> { it.role != "Owner" }
                            .thenBy { it.displayName.lowercase() }
                            .thenBy { it.email.lowercase() }
                    )
                )
            } catch (e: Exception) {
                println("Exception while fetching household members: ${e.message}")
                emit(emptyList())
            }
        }
    }

    override suspend fun deleteHouseholdMember(memberId: String) {
        try {
            val currentUserId = supabase.auth.currentUserOrNull()?.id
            val memberToDelete = supabase.postgrest[membersTableName].select {
                filter { eq("id", memberId) }
            }.decodeSingleOrNull<HouseholdMember>()

            supabase.postgrest[membersTableName].delete {
                filter { eq("id", memberId) }
            }

            if (
                memberToDelete != null &&
                currentUserId != null &&
                memberToDelete.userId == currentUserId &&
                _currentHousehold.value?.id == memberToDelete.householdId
            ) {
                _currentHousehold.value = loadFirstAvailableHouseholdForCurrentUser(currentUserId)
            }

            refreshTrigger.tryEmit(Unit)
            memberRefreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("Exception while deleting household member: ${e.message}")
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

    private suspend fun ensureCurrentUserMembership(householdId: String, role: String) {
        val currentUser = supabase.auth.currentUserOrNull() ?: return
        val userId = currentUser.id
        val email = currentUser.email ?: "unknown@foodmanager.app"
        val displayName = email.substringBefore("@").ifBlank { email }

        try {
            val existingMembership = supabase.postgrest[membersTableName].select {
                filter {
                    eq("household_id", householdId)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<HouseholdMember>()

            if (existingMembership == null) {
                supabase.postgrest[membersTableName].insert(
                    HouseholdMember(
                        householdId = householdId,
                        userId = userId,
                        email = email,
                        displayName = displayName,
                        role = role
                    )
                )
            }
        } catch (e: Exception) {
            println("Exception while ensuring household membership: ${e.message}")
        }
    }

    private suspend fun loadFirstAvailableHouseholdForCurrentUser(currentUserId: String): Household? {
        val memberships = supabase.postgrest[membersTableName].select {
            filter {
                eq("user_id", currentUserId)
            }
        }.decodeList<HouseholdMember>()

        return memberships.firstNotNullOfOrNull { membership ->
            supabase.postgrest[tableName].select {
                filter {
                    eq("id", membership.householdId)
                }
            }.decodeSingleOrNull<Household>()
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


