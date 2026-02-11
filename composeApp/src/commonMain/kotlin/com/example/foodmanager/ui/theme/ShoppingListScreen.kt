package com.example.foodmanager.ui.theme

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel // <--- Ensure this is imported
import androidx.compose.ui.Modifier
import com.example.foodmanager.viewmodel.ShoppingViewModel

@Composable
fun ShoppingListScreen() {
    val viewModel = viewModel { ShoppingViewModel() }
    val items by viewModel.items.collectAsState()

    LazyColumn(
        modifier = Modifier.fillMaxSize()
    ) {
        items(items) { item ->
            ShoppingListRow(
                item = item,
                onCheckedChange = { viewModel.toggleItem(item) }
            )
        }
    }
}