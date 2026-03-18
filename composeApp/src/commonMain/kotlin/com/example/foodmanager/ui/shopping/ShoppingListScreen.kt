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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import com.example.foodmanager.domain.model.ShoppingItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    navController: NavController,
    viewModel: ShoppingViewModel
) {
    val shoppingList by viewModel.items.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    var showAddDialog by remember { mutableStateOf(false) }
    var newName by remember { mutableStateOf("") }
    var newQuantity by remember { mutableStateOf("") }
    var newUnit by remember { mutableStateOf("units") }
    var newCategory by remember { mutableStateOf("Other") }
    var expandedUnit by remember { mutableStateOf(false) }
    var expandedCategory by remember { mutableStateOf(false) }

    val quantityTypes = listOf("grams", "kilograms", "millilitres", "litres", "units", "pieces")
    val categoryOptions = listOf(
        "Vegetables", "Fruits", "Meat", "Dairy", "Bread", "Pasta", "Rice",
        "Frozen", "Other"
    )

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                // Added the sorting dropdown menu to the Top Bar
                actions = {
                    var showSortMenu by remember { mutableStateOf(false) }
                    IconButton(onClick = { showSortMenu = true }) {
                        Icon(Icons.Default.MoreVert, contentDescription = "Sort Options")
                    }
                    DropdownMenu(
                        expanded = showSortMenu,
                        onDismissRequest = { showSortMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Sort by Name") },
                            onClick = {
                                viewModel.setSortType(SortType.NAME)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Amount") },
                            onClick = {
                                viewModel.setSortType(SortType.AMOUNT)
                                showSortMenu = false
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Sort by Category") },
                            onClick = {
                                viewModel.setSortType(SortType.CATEGORY)
                                showSortMenu = false
                            }
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Create shopping item")
            }
        },
        bottomBar = {
            val hasCheckedItems = shoppingList.any { it.isChecked }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Button(
                    onClick = { viewModel.markCheckedItemsAsBought() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    enabled = hasCheckedItems && !isLoading
                ) {
                    Text("Mark Checked as Bought")
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {

            if (shoppingList.isEmpty() && !isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your shopping list is empty!")
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(shoppingList, key = { it.id ?: it.hashCode() }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onCheckedChange = {
                                viewModel.toggleItem(item)
                            }
                        )
                    }
                }
            }

            if (isLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }

        // Add item dialog box
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = { showAddDialog = false },
                title = { Text("Create shopping item") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newName,
                            onValueChange = { newName = it },
                            label = { Text("Item name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                        OutlinedTextField(
                            value = newQuantity,
                            onValueChange = { newQuantity = it },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )
                        OutlinedTextField(
                            value = newUnit,
                            onValueChange = {},
                            label = { Text("Quantity type") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Box {
                                    IconButton(onClick = { expandedUnit = true }) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Select quantity type"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedUnit,
                                        onDismissRequest = { expandedUnit = false }
                                    ) {
                                        quantityTypes.forEach { unit ->
                                            DropdownMenuItem(
                                                text = { Text(unit) },
                                                onClick = {
                                                    newUnit = unit
                                                    expandedUnit = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                        OutlinedTextField(
                            value = newCategory,
                            onValueChange = {},
                            label = { Text("Category") },
                            modifier = Modifier.fillMaxWidth(),
                            readOnly = true,
                            trailingIcon = {
                                Box {
                                    IconButton(onClick = { expandedCategory = true }) {
                                        Icon(
                                            Icons.Default.ArrowDropDown,
                                            contentDescription = "Select category"
                                        )
                                    }
                                    DropdownMenu(
                                        expanded = expandedCategory,
                                        onDismissRequest = { expandedCategory = false }
                                    ) {
                                        categoryOptions.forEach { cat ->
                                            DropdownMenuItem(
                                                text = { Text(cat) },
                                                onClick = {
                                                    newCategory = cat
                                                    expandedCategory = false
                                                }
                                            )
                                        }
                                    }
                                }
                            }
                        )
                    }
                },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val qty = newQuantity.toDoubleOrNull() ?: 0.0
                            viewModel.addItem(newName.trim(), qty, newUnit, newCategory.trim())
                            showAddDialog = false
                            newName = ""
                            newQuantity = ""
                            newUnit = "units"
                            newCategory = "Other"
                        }
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showAddDialog = false }) {
                        Text("Cancel")
                    }
                }
            )
        }

        if (errorMessage != null) {
            AlertDialog(
                onDismissRequest = { viewModel.clearError() },
                title = { Text("Database Error") },
                text = { Text(errorMessage ?: "An unknown error occurred.") },
                confirmButton = {
                    TextButton(onClick = { viewModel.clearError() }) {
                        Text("OK")
                    }
                }
            )
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