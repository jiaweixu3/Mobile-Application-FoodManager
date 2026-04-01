package com.example.foodmanager.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.foodmanager.data.MockDb
import com.example.foodmanager.data.repository.MockSettingsRepository
import com.example.foodmanager.domain.model.Household
import com.example.foodmanager.ui.household.HouseholdScreen
import com.example.foodmanager.ui.household.HouseholdViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore
class SettingsScreenUITest {
    private lateinit var repository: MockSettingsRepository
    private lateinit var viewModel: HouseholdViewModel
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        MockDb.resetHouseholdsState()
        MockDb.resetHouseholdMembersState()

        repository = MockSettingsRepository()
        viewModel = HouseholdViewModel(repository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }



    // Members are correctly displayed
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testHouseholdMembersDisplayed() = runComposeUiTest {
        runTest {
            repository.storeHousehold(Household(id = "house_1", name = "House 1"))
        }

        setContent {
            HouseholdScreen(
                viewModel = viewModel,
                onBackClick = {}
            )
        }

        onNodeWithText("House 1").assertIsDisplayed() // Top bar title
        onNodeWithText("House 1 Owner").assertIsDisplayed() // Display name of owner
        onNodeWithText("Member One").assertIsDisplayed() // Display name of member

    }

    // Alert pops up when deleting someone
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testDeleteDialog() = runComposeUiTest {
        runTest{
            repository.storeHousehold(Household(id = "house_1", name = "House 1"))
        }

        setContent {
            HouseholdScreen(
                viewModel = viewModel,
                onBackClick = {}
            )
        }

        // Clicking delete
        onNodeWithContentDescription("Remove").performClick()

        // Alert dialog
        onNodeWithText("Remove Member One?").assertIsDisplayed()
        onNodeWithText("Cancel").assertIsDisplayed()
        onNodeWithText("Remove").assertIsDisplayed()
    }
}
