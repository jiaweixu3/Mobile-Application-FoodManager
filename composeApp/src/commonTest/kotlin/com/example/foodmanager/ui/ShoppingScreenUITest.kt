package com.example.foodmanager.ui

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import com.example.foodmanager.data.MockDb
import com.example.foodmanager.data.repository.MockInventoryRepository
import com.example.foodmanager.data.repository.MockShoppingRepository
import com.example.foodmanager.domain.useCase.MarkAsBoughtUseCase
import com.example.foodmanager.ui.shopping.ShoppingListScreen
import com.example.foodmanager.ui.shopping.ShoppingViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class ShoppingScreenUITest {
    private lateinit var  viewModel: ShoppingViewModel
    // Tracking navigation
    private var navigateBackCalled = false
    private var navigateToAddItemCalled = false
    private val mainDispatcher = StandardTestDispatcher()

    @BeforeTest
    fun setUp() {
        Dispatchers.setMain(mainDispatcher)
        val shoppingRepository = MockShoppingRepository()
        val inventoryRepository = MockInventoryRepository()
        val markAsBoughtUseCase = MarkAsBoughtUseCase(shoppingRepository, inventoryRepository)

        viewModel = ShoppingViewModel(
            repository = shoppingRepository,
            markAsBoughtUseCase = markAsBoughtUseCase
        )

        navigateBackCalled = false
        navigateToAddItemCalled = false
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    // Ensuring empty shopping list is handled
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testEmptyShoppingList() = runComposeUiTest{
        MockDb.clearShoppingState()

        setContent {
            ShoppingListScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigatetoAddItem = {}
            )
        }

        onNodeWithText("Shopping List").assertIsDisplayed() // top bar
        onNodeWithText("Your shopping list is empty!").assertIsDisplayed()
    }

    // Button is enabled when item is checked
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testBottomEnabled() = runComposeUiTest {
        MockDb.clearShoppingState()
        MockDb.addShoppingItem(
            com.example.foodmanager.domain.model.ShoppingItem(
                id = 99L,
                shopping_list_id = "shopping_list_1",
                name = "Test Apple",
                amount = 1.0,
                unit = "pcs",
                category = "Fruits",
                isChecked = true
            )
        )

        setContent {
            ShoppingListScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigatetoAddItem = {}
            )
        }

        val bottomButton = onNodeWithText("Mark Checked as Bought")
        bottomButton.assertIsEnabled()
    }

    // Shopping and navigation logic
    @OptIn(ExperimentalTestApi::class)
    @Test
    fun testShoppingNavigation() = runComposeUiTest{
        MockDb.resetShoppingState()
        setContent {
            ShoppingListScreen(
                viewModel = viewModel,
                onNavigateBack = {},
                onNavigatetoAddItem = { navigateToAddItemCalled = true }
            )
        }

        onNodeWithContentDescription("Create shopping item").performClick()
        assertTrue(navigateToAddItemCalled, "button did not affect navigation to Add Item screen")
    }

}
