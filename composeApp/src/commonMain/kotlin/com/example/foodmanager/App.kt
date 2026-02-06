package com.example.foodmanager

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeContentPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.foodmanager.ui.theme.DarkBlue
import com.example.foodmanager.ui.theme.LightBlue
import org.jetbrains.compose.resources.painterResource
import androidx.compose.material3.Surface
import androidx.compose.ui.graphics.BlendMode.Companion.Screen
import foodmanager.composeapp.generated.resources.Res
import foodmanager.composeapp.generated.resources.compose_multiplatform
import com.example.foodmanager.ui.theme.FoodManagerTheme
import org.jetbrains.compose.ui.tooling.preview.Preview


@Composable
fun App() {
    FoodManagerTheme {
        // Applies the Dark Color to the back of the page
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = DarkBlue
        ) {
            // Implementing NavHost to be able to navigate between different screens
            val navController = rememberNavController()
            NavHost(
                navController = navController,
                startDestination = ScreenDestination.Inventory
            ) {
                composable<ScreenDestination.Inventory> { InventoryScreen() }
                composable<ScreenDestination.ShoppingList> { ShoppingListScreen() }
                composable<ScreenDestination.AddItem> { AddingItemScreen() }
            }

        }
    }
}
