package com.example.foodmanager

// Kotlin file which describes the basic appearance of the screens of the app
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.ui.tooling.preview.Preview


@Preview
@Composable
fun InventoryScreen(){
    Text(text = "This displays the Inventory Screen",
        color = Color.White)
}

@Composable
fun ShoppingListScreen(){
    Text("This displays the Shopping List")
}

@Composable
fun AddingItemScreen(){
    Text("This adds a new item")
}