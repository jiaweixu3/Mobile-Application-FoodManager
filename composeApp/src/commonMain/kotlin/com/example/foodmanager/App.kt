package com.example.foodmanager

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.PaddingValues
import com.example.foodmanager.ui.additem.AddingItemScreen
import com.example.foodmanager.data.supabase
import com.example.foodmanager.data.repository.FavoriteRepository
import com.example.foodmanager.data.repository.InventoryRepository
import com.example.foodmanager.data.repository.ShoppingRepository
import com.example.foodmanager.data.repository.SupabaseFavoriteRepository
import com.example.foodmanager.data.repository.SupabaseInventoryRepository
import com.example.foodmanager.data.repository.SupabaseSettingsRepository
import com.example.foodmanager.data.repository.SupabaseShoppingRepository
import com.example.foodmanager.domain.useCase.ConsumeFoodItemUseCase
import com.example.foodmanager.domain.useCase.MarkAsBoughtUseCase
import com.example.foodmanager.ui.auth.LoginScreen
import com.example.foodmanager.ui.household.HouseholdViewModel
import com.example.foodmanager.ui.inventory.InventoryScreen
import com.example.foodmanager.ui.inventory.InventoryViewModel
import com.example.foodmanager.ui.navigation.AddItemDestination
import com.example.foodmanager.ui.navigation.ScreenDestination
import com.example.foodmanager.ui.settings.SettingsScreen
import com.example.foodmanager.ui.settings.SettingsViewModel
import com.example.foodmanager.ui.shopping.ShoppingListScreen
import com.example.foodmanager.ui.shopping.ShoppingViewModel
import com.example.foodmanager.ui.theme.DarkBlue
import com.example.foodmanager.ui.theme.FoodManagerTheme
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus

// Storing all screens in the app in a list, for simplified looping
val screens = listOf(
    ScreenDestination.Inventory,
    ScreenDestination.ShoppingList,
    ScreenDestination.Settings
)

@Composable
fun App() {
    val sessionStatus by supabase.auth.sessionStatus.collectAsState()
    FoodManagerTheme {
        // Handling log in and log out logic
        when (sessionStatus) {
            is SessionStatus.Authenticated -> MainAppLayout()
            SessionStatus.Initializing -> Text("Loading session...")
            else -> LoginScreen {}
        }
    }
}


// Defines the main app layout
@Composable
fun MainAppLayout(isloggedout: () -> Unit = {}) {
    // Implementing NavHost to be able to navigate between different screens
    val navController = rememberNavController()


    // Initializing the Mock Repositories, using remember to avoid redrawing
    val settingsRepo = remember { SupabaseSettingsRepository(supabase) }
    val shoppingRepo: ShoppingRepository = remember { SupabaseShoppingRepository(supabase, settingsRepo) }
    val inventoryRepo: InventoryRepository = remember { SupabaseInventoryRepository(supabase, settingsRepo) }
    val favoriteRepo: FavoriteRepository = remember { SupabaseFavoriteRepository(supabase, settingsRepo) }


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
            favoriteRepository = favoriteRepo,
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

    val householdViewModel = remember {
        HouseholdViewModel(settingsRepository = settingsRepo)
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
            startDestination = ScreenDestination.Inventory, // El destino inicial
            modifier = Modifier.padding(innerPadding)
        ) {
            composable<ScreenDestination.Inventory> {
                InventoryScreen(
                    viewModel = inventoryViewModel,
                    onNavigateToAddItem = {
                        navController.navigate(ScreenDestination.AddInventoryItem)
                    }
                )
            }

            composable<ScreenDestination.AddInventoryItem> {
                val addItemViewModel = remember {
                    com.example.foodmanager.ui.additem.AddItemViewModel(
                        inventoryRepository = inventoryRepo,
                        shoppingRepository = shoppingRepo,
                        favoriteRepository = favoriteRepo,
                        settingsRepository = settingsRepo
                    )
                }

                AddingItemScreen(
                    navController = navController,
                    viewModel = addItemViewModel,
                    initialDestination = AddItemDestination.Inventory
                )
            }

            composable<ScreenDestination.AddShoppingItem> {
                val addItemViewModel = remember {
                    com.example.foodmanager.ui.additem.AddItemViewModel(
                        inventoryRepository = inventoryRepo,
                        shoppingRepository = shoppingRepo,
                        favoriteRepository = favoriteRepo,
                        settingsRepository = settingsRepo
                    )
                }

                AddingItemScreen(
                    navController = navController,
                    viewModel = addItemViewModel,
                    initialDestination = AddItemDestination.ShoppingList
                )
            }

            composable<ScreenDestination.ShoppingList> {
                ShoppingListScreen(
                    viewModel = shoppingViewModel,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigatetoAddItem = { navController.navigate(ScreenDestination.AddShoppingItem) }
                )
            }

            composable<ScreenDestination.Settings> {
                SettingsScreen(
                    viewModel = settingsViewModel,
                    logoutSuccess = { isloggedout() },
                    onHouseholdSelected = {
                        navController.navigate(ScreenDestination.Inventory) {
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

            composable<ScreenDestination.HouseholdMembers> {
                com.example.foodmanager.ui.household.HouseholdScreen(
                    viewModel = householdViewModel,
                    onBackClick = { navController.popBackStack() }
                )
            }
        }
    }
}