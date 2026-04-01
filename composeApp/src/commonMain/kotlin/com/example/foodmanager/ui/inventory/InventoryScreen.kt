package com.example.foodmanager.ui.inventory

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.rememberDatePickerState
import com.example.foodmanager.domain.calculateDaysRemaining
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.domain.useCase.InventorySortOption
import com.example.foodmanager.ui.additem.dateFormat
import com.example.foodmanager.ui.utils.CategoryConstants

@Composable
fun SummaryBox(
    count: Int,
    label: String,
    backgroundColor: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = count.toString(),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
            Text(
                text = label,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InventoryScreen(
    viewModel: InventoryViewModel,
    onNavigateToAddItem: () -> Unit
) {

    val categoryScrollState = rememberScrollState()

    androidx.compose.runtime.LaunchedEffect(Unit) {
        viewModel.refreshInventory()
    }

    // Collect data
    val inventoryList by viewModel.visibleInventory.collectAsState()
    val suggestedItem by viewModel.suggestedItem.collectAsState()

    // Listen for Loading and Error states
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()

    // Calculate counts
    val expiredCount = inventoryList.count { calculateDaysRemaining(it) < 0 }
    val warningCount = inventoryList.count { calculateDaysRemaining(it) in 0..3 }
    val freshCount = inventoryList.count { calculateDaysRemaining(it) > 3 }

    var editingItem by remember { mutableStateOf<FoodItem?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("My Pantry") }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onNavigateToAddItem) {
                Icon(Icons.Default.Add, contentDescription = "Add Item")
            }
        }
    ) { innerPadding ->

        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.TopCenter
        ) {

            // Wrapped the content in a Box to handle the loading spinner overlay correctly
            Box(modifier = Modifier.widthIn(max = 600.dp).fillMaxSize()) {

                Column(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp, 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        SummaryBox(
                            count = freshCount,
                            label = "Fresh",
                            backgroundColor = Color(0xFF4CAF50),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryBox(
                            count = warningCount,
                            label = "Soon",
                            backgroundColor = Color(0xFFFFB300),
                            modifier = Modifier.weight(1f)
                        )
                        SummaryBox(
                            count = expiredCount,
                            label = "Expired",
                            backgroundColor = Color(0xFFE53935),
                            modifier = Modifier.weight(1f)
                        )
                    }

                    // Category filtering (Pasta, Meat, etc.)
                    val categories = listOf("All") + CategoryConstants.menuCategories
                    val selectedSortOption by viewModel.selectedSortOption.collectAsState()
                    var sortMenuExpanded by remember { mutableStateOf(false) }
                    val selectedCategory by viewModel.selectedCategory.collectAsState()
                    var categoryMenuExpanded by remember { mutableStateOf(false) }

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(categoryScrollState)
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ExposedDropdownMenuBox(
                            expanded = sortMenuExpanded,
                            onExpandedChange = { sortMenuExpanded = it },
                            modifier = Modifier.weight(1f) // Moved weight up here
                        ) {
                            OutlinedTextField(
                                value = selectedSortOption.label,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Sort by") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = sortMenuExpanded)
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth(), // Changed to fillMaxWidth
                                shape = RoundedCornerShape(24.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = sortMenuExpanded,
                                onDismissRequest = { sortMenuExpanded = false }
                            ) {
                                InventorySortOption.values().forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option.label) },
                                        onClick = {
                                            viewModel.setSortOption(option)
                                            sortMenuExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }

                        ExposedDropdownMenuBox(
                            expanded = categoryMenuExpanded,
                            onExpandedChange = { categoryMenuExpanded = it },
                            modifier = Modifier.weight(1f) // Moved weight up here
                        ) {
                            val categoryLabel = selectedCategory ?: "All"
                            OutlinedTextField(
                                value = categoryLabel,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Filter by") },
                                trailingIcon = {
                                    ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryMenuExpanded)
                                },
                                modifier = Modifier.menuAnchor().fillMaxWidth(), // Changed to fillMaxWidth
                                shape = RoundedCornerShape(24.dp)
                            )
                            ExposedDropdownMenu(
                                expanded = categoryMenuExpanded,
                                onDismissRequest = { categoryMenuExpanded = false }
                            ) {
                                categories.forEach { label ->
                                    val value = if (label == "All") null else label
                                    DropdownMenuItem(
                                        text = { Text(label) },
                                        onClick = {
                                            viewModel.setCategoryFilter(value)
                                            categoryMenuExpanded = false
                                        },
                                        contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                                    )
                                }
                            }
                        }
                    }

                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(inventoryList) { foodItem ->
                            Box(modifier = Modifier.clickable { editingItem = foodItem }) {
                                FoodCard(item = foodItem, viewModel = viewModel)
                            }
                        }
                    }
                }

                // Show loading spinner in the center of the screen
                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
            }
        }
    }

    // Suggestion Dialog
    suggestedItem?.let { foodToRestock ->
        var buyQuantity by remember { mutableStateOf("1.0") }

        AlertDialog(
            onDismissRequest = { viewModel.dismissSuggestion() },
            title = { Text("Out of ${foodToRestock.name}!") },
            text = {
                Column {
                    Text("Add ${foodToRestock.name} to your shopping list?")
                    Spacer(modifier = Modifier.padding(8.dp))
                    OutlinedTextField(
                        value = buyQuantity,
                        onValueChange = { newValue -> buyQuantity = newValue },
                        label = { Text("Quantity to buy") },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val qty = buyQuantity.toDoubleOrNull() ?: 1.0
                        viewModel.consumeItem(foodToRestock, 0.0, true, qty)
                        viewModel.dismissSuggestion()
                    }
                ) {
                    Text("Add to List")
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.dismissSuggestion() }) {
                    Text("No thanks")
                }
            }
        )
    }

    // Error message dialog box
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

    if (editingItem != null) {
        val item = editingItem!!

        var editName by remember(item) { mutableStateOf(item.name) }
        var editAmount by remember(item) { mutableStateOf(item.amount.toString()) }
        var editExpiry by remember(item) { mutableStateOf(item.expiryDate) }
        var editUnit by remember(item) { mutableStateOf(item.unit) }
        var editCategory by remember(item) { mutableStateOf(item.category) }

        var showDatePicker by remember { mutableStateOf(false) }
        var unitExpanded by remember { mutableStateOf(false) }
        var categoryExpanded by remember { mutableStateOf(false) }

        val units = listOf("grams", "kilograms", "millilitres", "litres", "units", "pieces")

        AlertDialog(
            onDismissRequest = { editingItem = null },
            title = { Text("Edit ${item.name}", fontWeight = FontWeight.Bold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editName,
                        onValueChange = { editName = it },
                        label = { Text("Name") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedTextField(
                            value = editAmount,
                            onValueChange = { editAmount = it },
                            label = { Text("Qty") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                        )

                        ExposedDropdownMenuBox(
                            expanded = unitExpanded,
                            onExpandedChange = { unitExpanded = it },
                            modifier = Modifier.weight(1.5f)
                        ) {
                            OutlinedTextField(
                                value = editUnit,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Unit") },
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = unitExpanded) },
                                modifier = Modifier.menuAnchor()
                            )
                            ExposedDropdownMenu(
                                expanded = unitExpanded,
                                onDismissRequest = { unitExpanded = false }
                            ) {
                                units.forEach { u ->
                                    DropdownMenuItem(
                                        text = { Text(u) },
                                        onClick = {
                                            editUnit = u
                                            unitExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = categoryExpanded,
                        onExpandedChange = { categoryExpanded = it }
                    ) {
                        OutlinedTextField(
                            value = editCategory,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Category") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = categoryExpanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = categoryExpanded,
                            onDismissRequest = { categoryExpanded = false }
                        ) {
                            CategoryConstants.menuCategories.forEach { cat ->
                                DropdownMenuItem(
                                    text = { Text(cat) },
                                    onClick = {
                                        editCategory = cat
                                        categoryExpanded = false
                                    }
                                )
                            }
                        }
                    }

                    OutlinedTextField(
                        value = editExpiry,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Expiry Date") },
                        modifier = Modifier.fillMaxWidth(),
                        trailingIcon = {
                            IconButton(onClick = { showDatePicker = true }) {
                                Icon(Icons.Default.DateRange, contentDescription = "Select Date")
                            }
                        }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val updated = item.copy(
                        name = editName,
                        amount = editAmount.toDoubleOrNull() ?: item.amount,
                        unit = editUnit,
                        category = editCategory,
                        expiryDate = editExpiry
                    )
                    viewModel.updateItem(updated)
                    editingItem = null
                }) { Text("Save") }
            },
            dismissButton = {
                TextButton(onClick = { editingItem = null }) { Text("Cancel") }
            }
        )

        if (showDatePicker) {
            val datePickerState = rememberDatePickerState()
            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            editExpiry = dateFormat(millis)
                        }
                        showDatePicker = false
                    }) { Text("OK") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}