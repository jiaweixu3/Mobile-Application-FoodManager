package com.example.foodmanager

// This file defines where to find each of the screens for navigation
// Uses Serializable as data type passing is safer and sealed to avoid compilation errors

import kotlinx.serialization.Serializable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.ui.graphics.vector.ImageVector


// Sealed allows for a fixed number of screen, avoids future bugs as well as it checks all elements
// Log In screen is not considered here as it is not included in NavHost, it is before other screens
sealed interface ScreenDestination {
    // All screens must have a title and an image, for easier navigation
    val title: String
    val icon: ImageVector
    @Serializable
    data object Inventory: ScreenDestination{
        override val title =  "Inventory"
        override val icon =  Icons.Filled.List
    }

    @Serializable
    data object ShoppingList: ScreenDestination{
        override val title = "ShoppingList"
        override val icon = Icons.Filled.ShoppingCart
    }

    @Serializable
    data object AddItem : ScreenDestination{
        override val title = "AddItem"
        override val icon = Icons.Filled.Add
    }
}