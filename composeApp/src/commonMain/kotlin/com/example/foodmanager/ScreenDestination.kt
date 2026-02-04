package com.example.foodmanager

// This file defines where to find each of the screens for navigation
// Uses Serializable as data type passing is safer and sealed to avoid compilation errors

import kotlinx.serialization.Serializable

sealed interface ScreenDestination {

    @Serializable
    data object Inventory: ScreenDestination
    @Serializable
    data object ShoppingList: ScreenDestination
    @Serializable
    data object AddItem : ScreenDestination}