package com.example.foodmanager

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.ui.inventory.FoodCard
import com.example.foodmanager.ui.inventory.InventoryScreen
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import com.example.foodmanager.ui.theme.DarkBlue
import com.example.foodmanager.ui.theme.LightBlue
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import com.example.foodmanager.ui.theme.FoodManagerTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.compose.material3.Icon
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import com.example.foodmanager.ui.theme.ShoppingListScreen
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.viewmodel.ShoppingViewModel
import com.example.foodmanager.ui.AddingItemScreen

// Storing all screens in the app in a list, for simplified looping
val screens = listOf(
    ScreenDestination.Inventory,
    ScreenDestination.ShoppingList,
    ScreenDestination.AddItem
)

@Composable
fun App() {
    FoodManagerTheme {
        // Implementing NavHost to be able to navigate between different screens
        val navController = rememberNavController()

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
                composable<ScreenDestination.ShoppingList> { ShoppingListScreen() }
                composable<ScreenDestination.AddItem> { AddingItemScreen(navController) }
            }

        }
    }
}
