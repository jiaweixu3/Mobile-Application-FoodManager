package com.example.foodmanager.repositoryTest

import com.example.foodmanager.data.MockDb
import com.example.foodmanager.data.repository.MockSettingsRepository
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class MockSettingsRepositoryTest {
    private val repository = MockSettingsRepository()

    @BeforeTest
    fun setUp() {
        MockDb.resetHouseholdsState()
        MockDb.resetHouseholdMembersState()
        // House 1 will be the active one
        MockDb.storeHousehold(Household(id = "house_1", name = "House 1"))
    }

    // Obtaining list of households
    @Test
    fun testGetHouseholdsList() = runTest {
        val households = repository.getHouseholdsList().first()
        assertEquals(3, households.size, "Returns the original list")
    }

    // Obtaining the current household
    @Test
    fun testGetCurrentHousehold() = runTest {
        val current = repository.getCurrentHouseholdValue()

        assertNotNull(current)
        assertEquals("house_1", current.id, "Active ousehold is house_1")
    }

    // Storing new house as current
    @Test
    fun testStoreHousehold() = runTest {
        val house2 = Household(id = "house_2", name = "House 2")

        repository.storeHousehold(house2)
        val current = repository.getCurrentHousehold.first()

        assertNotNull(current)
        assertEquals(house2, current, "Current household is House 2")
    }

    // Creating a new household sets it to current
    @Test
    fun testAddAndStoreNewHousehold() = runTest {
        val newHouse = Household(id = "house_99", name = "House 99")

        repository.addHousehold(newHouse)

        val households = repository.getHouseholdsList().first()
        val current = repository.getCurrentHousehold.first()

        assertEquals(4, households.size, "Size of households should be 4")
        assertEquals("house_99", current?.id, "New house is now current")
    }

    // Updating a name changes it adequately
    @Test
    fun testUpdatingHouseholdName() = runTest {
        repository.updateHouseholdName("house_1", "Waterloo")

        val households = repository.getHouseholdsList().first()
        val current = repository.getCurrentHousehold.first()

        assertEquals("Waterloo", current?.name, "Names should match")
        assertEquals("Waterloo", households.first{ it.id == "house_1"}.name, "Updated name in the lsit")
    }

    // Code will return a string of length 6
    @Test
    fun testCode6Digits() = runTest {
        val code = repository.generateCode("house_1")

        assertEquals(6, code.length, "Code should be of size 6")
        assertTrue(code.all{it.isLetter()}, "All code should be letters")
    }

    // New member will join household adequately
    @Test
    fun testAddNewUser() = runTest {
        repository.joinHousehold("house_2")

        val current = repository.getCurrentHousehold.first()
        val members = repository.getCurrentHouseholdMembers().first()

        assertNotNull(current)
        assertEquals("house_2", current.id, " Changes to joined household")
        assertEquals(2, members.size, "New house size")
    }

    // Creating household automatically becomes an owner
    @Test
    fun testUserOwner() = runTest {
        val newHouse = Household(id = "house_99", name = "Waterloo")
        repository.addHousehold(newHouse)

        val members = repository.getCurrentHouseholdMembers().first()

        assertEquals(1, members.size, "One member for new household")
        assertTrue(members.first().role.equals("owner", ignoreCase = true), "Member will be owner")
    }

    // Displays the current members in a household
    @Test
    fun testCurrentHouseholdMembersAreReturnedForActiveHousehold() = runTest {
        val members = repository.getCurrentHouseholdMembers().first()

        assertEquals(2, members.size)
        assertEquals(setOf("House 1 Owner", "Member One"), members.map { it.displayName }.toSet())
    }

    @Test
    fun testDeletingHouseholdMemberUpdatesCurrentHouseholdMembers() = runTest {
        repository.deleteHouseholdMember("member_2")

        val members = repository.getCurrentHouseholdMembers().first()
        assertEquals(1, members.size)
        assertEquals("Owner", members.first().role)
    }
}
