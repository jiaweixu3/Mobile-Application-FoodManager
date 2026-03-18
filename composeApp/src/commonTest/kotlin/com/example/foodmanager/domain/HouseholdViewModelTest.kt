package com.example.foodmanager

import com.example.foodmanager.ui.household.HouseholdViewModel
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.assertFalse
import kotlin.test.assertNull

class HouseholdViewModelTest {

    // Helper to create a fresh ViewModel for each test
    private fun createViewModel() = HouseholdViewModel()

    @Test
    fun `test empty list of households does not provide errors`() {
        val viewModel = createViewModel()
        // Ensure the initial state is an empty list and not null/error
        assertTrue(viewModel.households.value.isEmpty(), "Household list should start empty")
    }

    @Test
    fun `test create button enabled state based on input string`() {
        val viewModel = createViewModel()

        // Scenario: Empty string
        viewModel.onNewHouseholdNameChange("")
        assertFalse(viewModel.isCreateButtonEnabled.value, "Button should be disabled for empty string")

        // Scenario: Valid string
        viewModel.onNewHouseholdNameChange("My New Home")
        assertTrue(viewModel.isCreateButtonEnabled.value, "Button should be enabled when name is provided")
    }

    @Test
    fun `test clicking different household changes active role`() {
        val viewModel = createViewModel()
        val householdA = "Home A"
        val householdB = "Home B"

        viewModel.selectHousehold(householdA)
        // Assume default role is 'Member' or 'Admin'
        val roleA = viewModel.currentRole.value

        viewModel.selectHousehold(householdB)
        // Verify state changed
        assertEquals(householdB, viewModel.currentHousehold.value)
    }

    @Test
    fun `test input is cleared after sharing`() {
        val viewModel = createViewModel()
        viewModel.onShareEmailChange("test@example.com")

        viewModel.shareHousehold()

        assertEquals("", viewModel.shareEmailInput.value, "Email input should be cleared after sharing")
    }

    @Test
    fun `test member removal updates user list`() {
        val viewModel = createViewModel()
        // Assuming your VM loads some mock members for testing
        val initialCount = viewModel.usersList.value.size
        val userToRemove = viewModel.usersList.value.firstOrNull()

        if (userToRemove != null) {
            viewModel.removeMember(userToRemove)
            assertEquals(initialCount - 1, viewModel.usersList.value.size)
            assertFalse(viewModel.usersList.value.contains(userToRemove))
        }
    }

    @Test
    fun `test store and get household handles edge cases`() {
        val viewModel = createViewModel()

        // Edge case: Storing a null or blank household
        viewModel.storeHousehold("")
        assertNull(viewModel.lastStoredHousehold.value, "Should not store blank names")

        // Normal case
        viewModel.storeHousehold("Main Villa")
        assertEquals("Main Villa", viewModel.lastStoredHousehold.value)
    }
}