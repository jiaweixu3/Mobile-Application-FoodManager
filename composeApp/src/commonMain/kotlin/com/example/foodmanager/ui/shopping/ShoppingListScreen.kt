package com.example.foodmanager.ui.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import com.example.foodmanager.model.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    // Pass the ViewModel here
    viewModel: ShoppingViewModel
) {
    //  Observe the state from the ViewModel/MockDb
    val shoppingList by viewModel.items.collectAsState()

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { /* navController.navigate("addItem") */ }) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        },

        bottomBar = {
            // Check if any items in the list are currently checked
            val hasCheckedItems = shoppingList.any { it.isChecked }

            // We use a Surface/Box to give it a solid background behind the list
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.markCheckedItemsAsBought() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    // The button will be grayed out if nothing is checked
                    enabled = hasCheckedItems
                ) {
                    Text("Mark Checked as Bought")
                }
            }
        }
    ) { paddingValues ->
        // ... The rest of your code stays exactly the same!
        if (shoppingList.isEmpty()) {
            Box(modifier = Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                Text("Your shopping list is empty!")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(shoppingList, key = { it.id }) { item ->
                    ShoppingItemRow(
                        item = item,
                        onCheckedChange = {
                            viewModel.toggleItem(item)
                        }
                    )
                }
            }
        }
    }
}


@Composable
fun ShoppingItemRow(item: ShoppingItem, onCheckedChange: (Boolean) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = item.isChecked,
                onCheckedChange = onCheckedChange
            )

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.bodyLarge,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                )
                Text(
                    text = "${item.category} • ${item.amount} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}