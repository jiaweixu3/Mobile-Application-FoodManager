package com.example.foodmanager // Make sure this package matches yours!

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.ui.inventory.FoodCard

@Composable
fun App() {
    MaterialTheme {
        // Temporary column to stack a few cards
        Column(modifier = Modifier.padding(16.dp)) {

            // Expired Milk
            FoodCard(
                item = FoodItem(
                    id = "1",
                    name = "Milk",
                    expiryDate = "2026-02-01",
                    amount = 1.0,
                    unit = "L",
                    category = "Fridge"
                )
            )

            // Apples
            FoodCard(
                item = FoodItem(
                    id = "2",
                    name = "Apples",
                    expiryDate = "2026-02-10",
                    amount = 5.0,
                    unit = "pcs",
                    category = "Pantry"
                )
            )

            FoodCard(
                item = FoodItem(
                    id = "3",
                    name = "Meat",
                    expiryDate = "2026-03-05",
                    amount = 1.0,
                    unit = "kg",
                    category = "Pantry"
                )
            )
        }
    }
}