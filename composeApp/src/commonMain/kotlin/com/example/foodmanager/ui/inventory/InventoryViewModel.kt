package com.example.foodmanager.ui.inventory

import androidx.lifecycle.ViewModel
import com.example.foodmanager.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow

class InventoryViewModel: ViewModel() {
    private val _inventory = MutableStateFlow(
        listOf(
            FoodItem(
                id = "1",
                name = "Milk",
                expiryDate = "2026-02-01",
                amount = 1.0,
                unit = "Carton",
                category = "Fridge",
                barcode = "123456789",
                photoUrl = "milk.jpg"
            ),
            FoodItem(
                id = "2",
                name = "Spinach",
                expiryDate = "2026-02-05",
                amount = 200.0,
                unit = "g",
                category = "Fridge"
            ),
            FoodItem(
                id = "3",
                name = "Canned Beans",
                expiryDate = "2026-03-15",
                amount = 2.0,
                unit = "Cans",
                category = "Pantry"
            )
        )
    )
}