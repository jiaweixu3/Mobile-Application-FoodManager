package com.example.foodmanager.ui.shopping

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextDecoration
import androidx.navigation.NavController
import androidx.compose.ui.text.font.FontWeight
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.ui.navigation.AddItemDestination
import com.example.foodmanager.ui.navigation.ScreenDestination

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ShoppingListScreen(
    viewModel: ShoppingViewModel,
    onNavigateBack: () -> Unit,
    onNavigatetoAddItem: () -> Unit
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

    // State to hold validation errors inside the dialog
    var dialogError by remember { mutableStateOf<String?>(null) }

    val quantityTypes = listOf("grams", "kilograms", "millilitres", "litres", "units", "pieces")
    val categoryOptions = CategoryConstants.menuCategories

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Shopping List") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
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
            FloatingActionButton(
                onClick = {
                    showAddDialog = true
                    dialogError = null // Clear any old errors when opening the dialog
                },
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Create shopping item")
            }
        },
        bottomBar = {
            val hasCheckedItems = shoppingList.any { it.isChecked }

            Surface(
                modifier = Modifier.fillMaxWidth(),
                shadowElevation = 8.dp
            ) {
                Box(
                    modifier = Modifier.fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Button(
                        onClick = { viewModel.markCheckedItemsAsBought() },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        shape = RoundedCornerShape(24.dp),
                        enabled = hasCheckedItems && !isLoading
                    ) {
                        Text("Mark Checked as Bought")
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            if (shoppingList.isNotEmpty()) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(shoppingList, key = { it.id ?: it.hashCode() }) { item ->
                        ShoppingItemRow(
                            item = item,
                            onCheckedChange = { isChecked ->
                                viewModel.toggleItem(item, isChecked)
                            }
                        )
                    }
                }
            } else if (!isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Your shopping list is empty!")
                }
            }
            if (isLoading && shoppingList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            }
        }

        // Add item dialog box
        if (showAddDialog) {
            AlertDialog(
                onDismissRequest = {
                    showAddDialog = false
                    dialogError = null
                },
                title = { Text("Create shopping item") },
                text = {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        // Display the error message at the top of the form if it exists
                        if (dialogError != null) {
                            Text(
                                text = dialogError ?: "",
                                color = MaterialTheme.colorScheme.error,
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(bottom = 4.dp)
                            )
                        }

                        OutlinedTextField(
                            value = newName,
                            onValueChange = {
                                newName = it
                                dialogError = null // Clear error when typing
                            },
                            label = { Text("Item name") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        )
                        OutlinedTextField(
                            value = newQuantity,
                            onValueChange = {
                                newQuantity = it
                                dialogError = null // Clear error when typing
                            },
                            label = { Text("Quantity") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            shape = RoundedCornerShape(24.dp)
                        )
                        ExposedDropdownMenuBox(
                            expanded = expandedUnit,
                            onExpandedChange = { expandedUnit = it }
                        ) {
                            OutlinedTextField(
                                value = newUnit,
                                onValueChange = {},
                                label = { Text("Quantity type") },
                                modifier = Modifier.fillMaxWidth().menuAnchor(),
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnit) },
                                shape = RoundedCornerShape(24.dp)
                            )
                            ExposedDropdownMenu(
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
                            )
                        }
                    }
                },
                confirmButton = {
                    Button(
                        onClick = {

                            val parsedQty = newQuantity.toDoubleOrNull()

                            when {
                                newName.isBlank() -> dialogError = "Item name cannot be blank."
                                parsedQty == null -> dialogError = "Please enter a valid number for quantity."
                                parsedQty <= 0 -> dialogError = "Quantity must be greater than zero."
                                else -> {
                                    // Passed validation, send to ViewModel
                                    viewModel.addItem(newName.trim(), parsedQty, newUnit, newCategory.trim())

                                    // Reset fields and close dialog
                                    showAddDialog = false
                                    newName = ""
                                    newQuantity = ""
                                    newUnit = "units"
                                    newCategory = "Other"
                                    dialogError = null
                                }
                            }
                        },
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Add")
                    }
                },
                dismissButton = {
                    TextButton(onClick = {
                        showAddDialog = false
                        dialogError = null
                    }) {
                        Text("Cancel")
                    }
                }
                if (isLoading && shoppingList.isEmpty()) {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
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
}

@Composable
fun ShoppingItemRow(
    item: ShoppingItem,
    onCheckedChange: (Boolean) -> Unit,
    onDelete: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
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
                    fontWeight = FontWeight.Bold,
                    textDecoration = if (item.isChecked) TextDecoration.LineThrough else null
                )
                Text(
                    text = "${item.category} • ${item.amount} ${item.unit}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(
                onClick = { showDeleteDialog = true }
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Delete ${item.name}",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Delete ${item.name}?") },
            text = { Text("This item will be removed from your shopping list.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete()
                        showDeleteDialog = false
                    }
                ) {
                    Text("Delete", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancel")
                }
            }
        )
    }
}