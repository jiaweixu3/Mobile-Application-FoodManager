    package com.example.foodmanager.ui.inventory

    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.material3.CenterAlignedTopAppBar
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.unit.dp
    import androidx.lifecycle.viewmodel.compose.viewModel
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InventoryScreen() {
        // Get the ViewModel
        val viewModel: InventoryViewModel = viewModel { InventoryViewModel() }

        // Collect data
        val inventoryList by viewModel.inventory.collectAsState()

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text("My Pantry")})
            }
        ) { innerPadding ->
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
            ) {
                items(inventoryList) { foodItem ->
                    FoodCard(item = foodItem)
                }
            }
        }
    }