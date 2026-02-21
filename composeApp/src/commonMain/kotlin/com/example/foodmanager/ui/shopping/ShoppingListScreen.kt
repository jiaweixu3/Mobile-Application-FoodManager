package com.example.foodmanager.ui.shopping

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.Modifier
import androidx.compose.material3.Scaffold
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen() {
    val viewModel = viewModel { ShoppingViewModel() }
    val items by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Shopping List")})
        }
    ){  innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)

        ) {
            items(items) { item ->
                ShoppingListRow(
                    item = item,
                    onCheckedChange = { viewModel.toggleItem(item) }
                )
            }
        }
    }
}