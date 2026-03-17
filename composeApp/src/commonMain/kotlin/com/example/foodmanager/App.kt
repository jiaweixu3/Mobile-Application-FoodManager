package com.example.foodmanager

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.foodmanager.ui.theme.DarkBlue
import com.example.foodmanager.ui.theme.FoodManagerTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Icon
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.foodmanager.ui.shopping.ShoppingListScreen
import com.example.foodmanager.ui.additem.AddingItemScreen
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import com.example.foodmanager.ui.auth.LoginScreen
import com.example.foodmanager.ui.settings.SettingsScreen
import com.example.foodmanager.data.repository.MockShoppingRepository
import com.example.foodmanager.ui.shopping.ShoppingViewModel
import com.example.foodmanager.domain.useCase.MarkAsBoughtUseCase
import com.example.foodmanager.data.repository.MockInventoryRepository
import com.example.foodmanager.data.repository.MockSettingsRepository
import com.example.foodmanager.domain.useCase.ConsumeFoodItemUseCase
import com.example.foodmanager.ui.navigation.ScreenDestination
import com.example.foodmanager.ui.inventory.InventoryScreen
import com.example.foodmanager.ui.inventory.InventoryViewModel
import com.example.foodmanager.ui.settings.SettingsViewModel

// Storing all screens in the app in a list, for simplified looping
val screens = listOf(
    ScreenDestination.Inventory,
    ScreenDestination.ShoppingList,
    ScreenDestination.Settings
)

@Composable
fun App() {
    // Variable which tracks state whether the user is logged in or not
    var isloggedin by remember { mutableStateOf(false) }
    FoodManagerTheme {
        // Handling log in and log out logic
        if (!isloggedin) {
            LoginScreen {
                isloggedin = true
            }
        } else {
            MainAppLayout(
                isloggedout = { // If we log out
                    isloggedin = false
                }
            )
        }
    }
}


// Defines the main app layout
@Composable
fun MainAppLayout(isloggedout: () -> Unit = {}) {
    // Implementing NavHost to be able to navigate between different screens
    val navController = rememberNavController()


    // Initializing the Mock Repositories, using remember to avoid redrawing
    val shoppingRepo = remember { MockShoppingRepository() }
    val inventoryRepo = remember { MockInventoryRepository() }
    val settingsRepo = remember { MockSettingsRepository() }

    //  Create the Use Case and pass in the repositories you just created
    val markAsBoughtUseCase = remember {
        MarkAsBoughtUseCase(
            shoppingRepository = shoppingRepo,
            inventoryRepository = inventoryRepo
        )

    }

    // Create the ViewModel with both dependencies
    val shoppingViewModel = remember {
        ShoppingViewModel(repository = shoppingRepo, markAsBoughtUseCase = markAsBoughtUseCase)
    }


    //  Create the use case
    val consumeUseCase = remember {
        ConsumeFoodItemUseCase(inventoryRepo, shoppingRepo)
    }

    // Create the Inventory ViewModel used by the Inventory screen
    val inventoryViewModel = remember {
        InventoryViewModel(
            repository = inventoryRepo,
            shoppingRepository = shoppingRepo,
            consumeFoodItemUseCase = consumeUseCase
        )
    }

    // Creating the Settings ViewModel
    val settingsViewModel = remember {
        SettingsViewModel(
            settingsRepository = settingsRepo,
        )
    }


    // Stores the value of the current screen
    val currentscreen_ = navController.currentBackStackEntryAsState()
    val currentscreen = currentscreen_.value?.destination

    // Scaffold allows for navigating between screens
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBlue,

        // Creates a bar at the bottom which allows for switching between elements
        bottomBar = {
            NavigationBar {
                // Creating an item for each of the screens
                screens.forEach { screen ->
                    NavigationBarItem(
                        label = { Text(screen.title) },
                        icon = { Icon(screen.icon, contentDescription = screen.title) },
                        selected = currentscreen?.hasRoute(screen::class) == true, // Selected if the user is currently in this screen
                        onClick = { // Defines how the app will change screens when being clicked
                            navController.navigate(screen) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true // Stores previous state
                                }
                                launchSingleTop = true // Avoids multiple copies of the same destination
                                restoreState = true
                            }
                        }
                    )
                }
            }

        }

    ) { innerPadding: PaddingValues ->

        NavHost(
            navController = navController,
            startDestination = ScreenDestination.Inventory,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ScreenDestination.Inventory> {
                InventoryScreen(
                    viewModel = inventoryViewModel,
                    onNavigateToAddItem = {
                        navController.navigate(ScreenDestination.AddItem)
                    }
                )
            }


            composable<ScreenDestination.ShoppingList> {
                ShoppingListScreen(
                    navController = navController,
                    viewModel = shoppingViewModel
                )
            }

            composable<ScreenDestination.AddItem> { AddingItemScreen(navController) }

            composable<ScreenDestination.Settings> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    logoutSuccess = { isloggedout() },
                    onHouseholdSelected = {
                        // Navigation logic, to go back to inventory
                        navController.navigate(ScreenDestination.Inventory) {
                            // Goes to inventory window
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onNavigateToMembers = {
                        navController.navigate(ScreenDestination.HouseholdMembers)
                    }
                )
            }

            // New Screen
            composable<ScreenDestination.HouseholdMembers> {
                com.example.foodmanager.ui.household.HouseholdScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

        }
    }
}