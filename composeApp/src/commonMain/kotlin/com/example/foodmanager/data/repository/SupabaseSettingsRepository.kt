package com.example.foodmanager.data.repository

import com.example.foodmanager.domain.model.Household
import com.example.foodmanager.domain.model.HouseholdMember
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.postgrest.postgrest
import io.github.jan.supabase.postgrest.query.Columns
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flow
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.put
import kotlinx.serialization.json.buildJsonObject

@OptIn(ExperimentalCoroutinesApi::class)
class SupabaseSettingsRepository(private val supabase: SupabaseClient) : SettingsRepository {
    private val tableName = "households"
    private val membersTableName = "household_members"
    private val legacyMembersTableName = "user_household"

    private val _currentHousehold = MutableStateFlow<Household?>(null)
    override val getCurrentHousehold: Flow<Household?> = _currentHousehold

    private val refreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }
    private val memberRefreshTrigger = MutableSharedFlow<Unit>(replay = 1).apply { tryEmit(Unit) }

    override fun getHouseholdsList(): Flow<List<Household>> = refreshTrigger.flatMapLatest {
        flow {
            try {
                val currentUser = supabase.auth.currentUserOrNull()
                if (currentUser == null) {
                    emit(emptyList())
                    return@flow
                }

                val memberships = getMembershipsForUser(currentUser.id)

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

            } catch (e: Exception) {
                println("Exception while fetching households: ${e.message}")
                emit(emptyList())
            }
        }
    }

    override suspend fun storeHousehold(household: Household) {
        ensureHouseholdResources(household.id)
        _currentHousehold.value = household
    }

    override suspend fun addHousehold(newHousehold: Household) {
        try {
            val insertHousehold = HouseholdInsert(name = newHousehold.name)
            val insertedHousehold = supabase.postgrest[tableName]
                .insert(insertHousehold) {
                    select()
                }.decodeSingle<Household>()

            ensureHouseholdResources(insertedHousehold.id)
            _currentHousehold.value = insertedHousehold

            ensureCurrentUserMembership(insertedHousehold.id, "Owner")

            refreshTrigger.tryEmit(Unit)
            memberRefreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("SUPABASE ERROR adding household: ${e.message}")
            throw e
        }
    }

    override suspend fun generateCode(householdId: String): String {
        return try {
            val existingHousehold = supabase.postgrest[tableName].select {
                filter { eq("id", householdId) }
            }.decodeSingle<Household>()

            existingHousehold.joinCode?.takeIf { it.isNotBlank() }?.let { existingCode ->
                if (_currentHousehold.value?.id == householdId) {
                    _currentHousehold.value = existingHousehold
                }
                refreshTrigger.tryEmit(Unit)
                return existingCode
            }

            val code = (1..6).map { ('0'..'9').random() }.joinToString("")

            supabase.postgrest["households"].update(UpdatingJoinCode(code)) {
                filter { eq("id", householdId) }
            }

            _currentHousehold.value = existingHousehold.copy(joinCode = code)

            refreshTrigger.tryEmit(Unit)
            code
        } catch (e: Exception) {
            println("Supabase Error generating a code: ${e.message}")
            throw e
        }
    }

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

            ensureHouseholdResources(household.id)
            _currentHousehold.value = household

            ensureCurrentUserMembership(household.id, "Member")

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
                val members = getMembersForHousehold(householdId)

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

    override suspend fun transferOwnershipAndLeave(currentUserId: String, newOwnerUserId: String) {
        try {
            val householdId = _currentHousehold.value?.id ?: return
            val signedInUserId = supabase.auth.currentUserOrNull()?.id ?: return
            val targetMember = ensureMembershipRowForUser(householdId, newOwnerUserId) ?: return

            supabase.postgrest[membersTableName].update(mapOf("role" to "Owner")) {
                filter {
                    eq("household_id", householdId)
                    eq("user_id", targetMember.userId)
                }
            }

            supabase.postgrest[membersTableName].delete {
                filter {
                    eq("household_id", householdId)
                    eq("user_id", currentUserId)
                }
            }

            _currentHousehold.value = loadFirstAvailableHouseholdForCurrentUser(signedInUserId)
            refreshTrigger.tryEmit(Unit)
            memberRefreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("Exception while transferring ownership: ${e.message}")
        }
    }

    override suspend fun deleteCurrentHousehold() {
        try {
            val householdId = _currentHousehold.value?.id ?: return
            val currentUserId = supabase.auth.currentUserOrNull()?.id

            supabase.postgrest[tableName].delete {
                filter { eq("id", householdId) }
            }

            _currentHousehold.value = currentUserId?.let { loadFirstAvailableHouseholdForCurrentUser(it) }
            refreshTrigger.tryEmit(Unit)
            memberRefreshTrigger.tryEmit(Unit)
        } catch (e: Exception) {
            println("Exception while deleting household: ${e.message}")
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

    private suspend fun ensureHouseholdResources(householdId: String) {
        ensureInventoryExists(householdId)
        ensureShoppingListExists(householdId)
    }

    private suspend fun ensureInventoryExists(householdId: String) {
        try {
            val existingInventory = supabase.postgrest["inventories"].select {
                filter { eq("household_id", householdId) }
            }.decodeSingleOrNull<HouseholdResourceRow>()

            if (existingInventory == null) {
                supabase.postgrest["inventories"].insert(HouseholdResourceInsert(householdId))
            }
        } catch (e: Exception) {
            println("Exception while ensuring inventory exists: ${e.message}")
        }
    }

    private suspend fun ensureShoppingListExists(householdId: String) {
        try {
            val existingShoppingList = supabase.postgrest["shopping_lists"].select {
                filter { eq("household_id", householdId) }
            }.decodeSingleOrNull<HouseholdResourceRow>()

            if (existingShoppingList == null) {
                supabase.postgrest["shopping_lists"].insert(HouseholdResourceInsert(householdId))
            }
        } catch (e: Exception) {
            println("Exception while ensuring shopping list exists: ${e.message}")
        }
    }

    private suspend fun loadFirstAvailableHouseholdForCurrentUser(currentUserId: String): Household? {
        val memberships = getMembershipsForUser(currentUserId)

        return memberships.firstNotNullOfOrNull { membership ->
            supabase.postgrest[tableName].select {
                filter {
                    eq("id", membership.householdId)
                }
            }.decodeSingleOrNull<Household>()
        }
    }

    private suspend fun getMembershipsForUser(userId: String): List<HouseholdMember> {
        val currentRows = runCatching {
            supabase.postgrest[membersTableName].select {
                filter { eq("user_id", userId) }
            }.decodeList<HouseholdMember>()
        }.getOrDefault(emptyList())

        val legacyRows = fetchLegacyMembershipsForUser(userId)
        return (currentRows + legacyRows).distinctBy { "${it.householdId}:${it.userId}" }
    }

    private suspend fun getMembersForHousehold(householdId: String): List<HouseholdMember> {
        val currentRows = fetchCurrentMembersForHousehold(householdId)

        val legacyRows = fetchLegacyMembersForHousehold(householdId)
        return (currentRows + legacyRows).distinctBy { it.userId }
    }

    private suspend fun fetchCurrentMembersForHousehold(householdId: String): List<HouseholdMember> {
        return runCatching {
            supabase.postgrest[membersTableName].select(
                columns = Columns.raw("id,household_id,user_id,email,display_name,role,users(id,email,username)")
            ) {
                filter { eq("household_id", householdId) }
            }.decodeList<CurrentMembershipRow>().map { it.toDomain() }
        }.getOrElse {
            supabase.postgrest[membersTableName].select {
                filter { eq("household_id", householdId) }
            }.decodeList<HouseholdMember>().map { member ->
                member.copy(
                    displayName = normalizeDisplayName(member.displayName, member.email, member.userId),
                    email = member.email.ifBlank { member.userId }
                )
            }
        }
    }

    private suspend fun fetchLegacyMembershipsForUser(userId: String): List<HouseholdMember> {
        return runCatching {
            supabase.postgrest[legacyMembersTableName].select(
                columns = Columns.raw("household_id,user_id,role,users(id,email,username)")
            ) {
                filter { eq("user_id", userId) }
            }.decodeList<LegacyMembershipRow>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    private suspend fun fetchLegacyMembersForHousehold(householdId: String): List<HouseholdMember> {
        return runCatching {
            supabase.postgrest[legacyMembersTableName].select(
                columns = Columns.raw("household_id,user_id,role,users(id,email,username)")
            ) {
                filter { eq("household_id", householdId) }
            }.decodeList<LegacyMembershipRow>().map { it.toDomain() }
        }.getOrDefault(emptyList())
    }

    private suspend fun ensureMembershipRowForUser(householdId: String, userId: String): HouseholdMember? {
        val existingCurrent = runCatching {
            supabase.postgrest[membersTableName].select {
                filter {
                    eq("household_id", householdId)
                    eq("user_id", userId)
                }
            }.decodeSingleOrNull<HouseholdMember>()
        }.getOrNull()

        if (existingCurrent != null) return existingCurrent

        val legacyMember = (fetchLegacyMembersForHousehold(householdId) + fetchCurrentMembersForHousehold(householdId))
            .firstOrNull { it.userId == userId } ?: return null

        val inserted = HouseholdMember(
            householdId = householdId,
            userId = userId,
            email = legacyMember.email.ifBlank { "unknown@foodmanager.app" },
            displayName = normalizeDisplayName(legacyMember.displayName, legacyMember.email, userId),
            role = legacyMember.role.ifBlank { "Member" }
        )

        runCatching {
            supabase.postgrest[membersTableName].insert(inserted)
        }

        return inserted
    }

    private fun normalizeDisplayName(displayName: String, email: String, userId: String): String {
        return displayName.takeIf {
            it.isNotBlank() && !it.equals(userId, ignoreCase = true)
        } ?: email.substringBefore("@").takeIf { it.isNotBlank() }
            ?: userId
    }
}

@Serializable
private data class HouseholdInsert(val name: String)

@Serializable
private data class UpdateHouseholdName(val name: String)

@Serializable
private data class UpdatingJoinCode(val joinCode: String)

@Serializable
private data class HouseholdResourceInsert(
    @kotlinx.serialization.SerialName("household_id")
    val householdId: String
)

@Serializable
private data class HouseholdResourceRow(
    val id: String
)

@Serializable
private data class CurrentMembershipRow(
    val id: String? = null,
    @kotlinx.serialization.SerialName("household_id")
    val householdId: String,
    @kotlinx.serialization.SerialName("user_id")
    val userId: String,
    val email: String? = null,
    @kotlinx.serialization.SerialName("display_name")
    val displayName: String? = null,
    val role: String,
    val users: LegacyUser? = null
) {
    fun toDomain(): HouseholdMember {
        val resolvedEmail = email?.takeIf { it.isNotBlank() } ?: users?.email.orEmpty()
        val resolvedDisplayName = displayName?.takeIf {
            it.isNotBlank() && !it.equals(userId, ignoreCase = true)
        } ?: users?.username?.takeIf { it.isNotBlank() }
            ?: resolvedEmail.substringBefore("@").takeIf { it.isNotBlank() }
            ?: userId

        return HouseholdMember(
            id = id,
            householdId = householdId,
            userId = userId,
            email = resolvedEmail,
            displayName = resolvedDisplayName,
            role = role
        )
    }
}

@Serializable
private data class LegacyMembershipRow(
    @kotlinx.serialization.SerialName("household_id")
    val householdId: String,
    @kotlinx.serialization.SerialName("user_id")
    val userId: String,
    val role: String,
    val users: LegacyUser? = null
) {
    fun toDomain(): HouseholdMember {
        val email = users?.email.orEmpty()
        val displayName = users?.username?.takeIf { it.isNotBlank() }
            ?: email.substringBefore("@").takeIf { it.isNotBlank() }
            ?: "Member"

        return HouseholdMember(
            householdId = householdId,
            userId = userId,
            email = email,
            displayName = displayName,
            role = role
        )
    }
}

@Serializable
private data class LegacyUser(
    val id: String? = null,
    val email: String? = null,
    val username: String? = null
)
