package com.example.foodmanager

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.foodmanager.ui.inventory.InventoryScreen
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
import com.example.foodmanager.repository.MockShoppingRepository
import com.example.foodmanager.ui.shopping.ShoppingViewModel


// Storing all screens in the app in a list, for simplified looping
val screens = listOf(
    ScreenDestination.Inventory,
    ScreenDestination.ShoppingList,
    ScreenDestination.AddItem,
    ScreenDestination.Settings
)

@Composable
fun App() {
    // Variable which tracks state whether the user is logged in or not
    var isloggedin by remember {mutableStateOf(false)}
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

    // 2. Initialize the connection here so it persists while the app is open
    // Since we aren't using a Factory yet, we manually inject the Mock Repository
    val shoppingRepository = remember { MockShoppingRepository() }
    val shoppingViewModel = remember { ShoppingViewModel(repository = shoppingRepository) }

    // Stores the value of the current screen
    val currentscreen_ = navController.currentBackStackEntryAsState()
    val currentscreen = currentscreen_.value?.destination

    // Scaffold allows for navigating between screens
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = DarkBlue,

        // Creates a bar at the bottom which allows for switching between elements
        bottomBar = { NavigationBar {
            // Creating an item for each of the screens
            screens.forEach { screen ->
                NavigationBarItem(
                    label = {Text(screen.title)},
                    icon = {Icon(screen.icon, contentDescription = screen.title)},
                    selected = currentscreen?.hasRoute(screen::class) == true , // Selected if the user is currently in this screen
                    onClick = { // Defines how the app will change screens when being clicked
                        navController.navigate(screen){
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
            composable<ScreenDestination.Inventory> { InventoryScreen() }


            composable<ScreenDestination.ShoppingList> {
                ShoppingListScreen(
                    navController = navController,
                    viewModel = shoppingViewModel
                )
            }

            composable<ScreenDestination.AddItem> { AddingItemScreen(navController) }
            composable<ScreenDestination.Settings> { SettingsScreen(logoutSuccess = {isloggedout()}) }
        }
    }
}