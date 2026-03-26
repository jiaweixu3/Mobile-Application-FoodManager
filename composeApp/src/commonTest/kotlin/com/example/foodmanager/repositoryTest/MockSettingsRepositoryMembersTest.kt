package com.example.foodmanager.repositoryTest

import com.example.foodmanager.data.MockDb
import com.example.foodmanager.data.repository.MockSettingsRepository
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class MockSettingsRepositoryMembersTest {
    private val repository = MockSettingsRepository()

    @BeforeTest
    fun setUp() {
        MockDb.resetHouseholdMembersState()
        MockDb.storeHousehold(Household(id = "house_1", name = "House 1"))
    }

    @Test
    fun `current household members are returned for active household`() = runTest {
        val members = repository.getCurrentHouseholdMembers().first()

        assertEquals(2, members.size)
        assertEquals(setOf("House 1 Owner", "Member One"), members.map { it.displayName }.toSet())
    }

    @Test
    fun `deleting household member updates current household members`() = runTest {
        repository.deleteHouseholdMember("member_2")

        val members = repository.getCurrentHouseholdMembers().first()
        assertEquals(1, members.size)
        assertEquals("Owner", members.first().role)
    }
}
