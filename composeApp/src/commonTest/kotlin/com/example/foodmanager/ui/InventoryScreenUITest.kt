package com.example.foodmanager.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.foodmanager.data.repository.InMemoryFavoriteRepository
import com.example.foodmanager.data.repository.MockInventoryRepository
import com.example.foodmanager.data.repository.MockSettingsRepository
import com.example.foodmanager.data.repository.MockShoppingRepository
import com.example.foodmanager.domain.useCase.ConsumeFoodItemUseCase
import com.example.foodmanager.ui.inventory.InventoryScreen
import com.example.foodmanager.ui.inventory.InventoryViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
@Ignore
class InventoryScreenUITest {
    private lateinit var viewModel: InventoryViewModel
    private var navigationAddItem = false // Tracker for navigation logic
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)

        val inventoryRepository = MockInventoryRepository()
        val shoppingRepository = MockShoppingRepository()
        val settingsRepository = MockSettingsRepository()
        val favoriteRepository = InMemoryFavoriteRepository(settingsRepository)
        val consumeUseCase = ConsumeFoodItemUseCase(inventoryRepository, shoppingRepository)

        viewModel = InventoryViewModel(
            repository = inventoryRepository,
            favoriteRepository = favoriteRepository,
            shoppingRepository = shoppingRepository,
            consumeFoodItemUseCase = consumeUseCase
        )

        navigationAddItem = false
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Basic layout appears
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testBasicLayoutAppears() = runComposeUiTest {
        setContent {
            InventoryScreen(
                viewModel = viewModel,
                onNavigateToAddItem = {}
            )
        }

        onNodeWithText("My Pantry").assertIsDisplayed()

        // Summary boxes
        onNodeWithText("Fresh").assertIsDisplayed()
        onNodeWithText("Soon").assertIsDisplayed()
        onAllNodesWithText("Expired").onFirst().assertIsDisplayed()

        onAllNodesWithText("2", substring = true).onFirst().assertExists()
    }

    // Filtering and sorting
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testFilterAndSort() = runComposeUiTest {
        setContent {
            InventoryScreen(
                viewModel = viewModel,
                onNavigateToAddItem = {}
            )
        }

        onNodeWithText("Sort by").assertIsDisplayed()
        onNodeWithText("Filter by").assertIsDisplayed()
    }

    // Navigation works correctly
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testTriggersNavigation() = runComposeUiTest {
        setContent {
            InventoryScreen(
                viewModel = viewModel,
                onNavigateToAddItem = { navigationAddItem = true }
            )
        }

        onNodeWithContentDescription("Add Item").performClick()
        assertTrue(navigationAddItem, "Clicking the button did not affect navigation")
    }
}
