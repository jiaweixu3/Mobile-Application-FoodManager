package com.example.foodmanager.ui.additem

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import com.example.foodmanager.domain.model.FavoriteFoodItem
import com.example.foodmanager.ui.navigation.AddItemDestination
import com.example.foodmanager.ui.utils.CategoryConstants
import java.util.TimeZone

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingItemScreen(
    navController: NavController,
    viewModel: AddItemViewModel,
    initialDestination: AddItemDestination
) {
    val defaultInitialMillis = CategoryConstants.getDefaultExpiryMillis("Other")

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("1") }
    var expiryDate by remember { mutableStateOf(dateFormat(defaultInitialMillis)) }
    var expiryDateMs by remember { mutableStateOf<Long?>(defaultInitialMillis) }
    var selectedCategory by remember { mutableStateOf("Other") }
    var selectedUnit by remember { mutableStateOf("units") }
    var selectedFavoriteLabel by remember { mutableStateOf("") }

    // New selector
    var destination by remember(initialDestination) { mutableStateOf(initialDestination) }
    var saveAsFavorite by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var expandedUnitDropdown by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current

    val categories = CategoryConstants.menuCategories
    val favoriteItems by viewModel.favoriteItems.collectAsState()
    val quantityTypes = listOf("grams", "kilograms", "millilitres", "litres", "units", "pieces")

    // Collect events from the ViewModel
    LaunchedEffect(Unit) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is AddItemUiEvent.ShowError -> snackbarHostState.showSnackbar(event.message)
                is AddItemUiEvent.NavigateBack -> navController.popBackStack()
            }
        }
    }

    // User Interface
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Add New Item") }) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            Text("Where to add this item?", style = MaterialTheme.typography.labelLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = destination == AddItemDestination.Inventory, onClick = { destination = AddItemDestination.Inventory })
                    Text("Inventory")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = destination == AddItemDestination.ShoppingList, onClick = { destination = AddItemDestination.ShoppingList })
                    Text("Shopping List")
                }
            }

            // 1. Product name handling
            CustomNotchedField(label = "Product Name") {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { saveAsFavorite = !saveAsFavorite }) {
                        Icon(
                            imageVector = if (saveAsFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = null,
                            tint = if (saveAsFavorite) Color(0xFFE53935) else Color.LightGray
                        )
                    }
                    TextField(
                        value = productName,
                        onValueChange = { productName = it },
                        placeholder = { Text("Enter name...") },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
                    )
                }
            }

            // 2. Choose a favorite
            CustomNotchedField(label = "Choose a favorite") {
                var expanded by remember { mutableStateOf(false) }
                ExposedDropdownMenuBox(expanded = expanded, onExpandedChange = { expanded = it }) {
                    TextField(
                        value = selectedFavoriteLabel,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        placeholder = { Text(if (favoriteItems.isEmpty()) "No favorite items yet" else "Select a favorite") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
                    )
                    ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                        if (favoriteItems.isEmpty()) {
                            DropdownMenuItem(text = { Text("No favorite items yet") }, onClick = { expanded = false }, enabled = false)
                        } else {
                            favoriteItems.forEach { favorite ->
                                DropdownMenuItem(
                                    text = { Text("${favorite.name} • ${favorite.amount} ${favorite.unit}") },
                                    onClick = {
                                        selectedFavoriteLabel = favorite.name
                                        productName = favorite.name
                                        quantity = favorite.amount.toString()
                                        selectedUnit = favorite.unit
                                        selectedCategory = favorite.category
                                        val defaultMillis = CategoryConstants.getDefaultExpiryMillis(favorite.category)
                                        expiryDateMs = defaultMillis
                                        expiryDate = dateFormat(defaultMillis)
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Product Quantity handling
                CustomNotchedField(
                    label = "Qty",
                    modifier = Modifier.wrapContentWidth()
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        IconButton(onClick = {
                            val current = quantity.toDoubleOrNull() ?: 1.0
                            if (current > 0.5) quantity = (current - 1).toString().replace(".0", "")
                        }) {
                            Text("-", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }

                        TextField(
                            value = quantity,
                            onValueChange = { quantity = it },
                            modifier = Modifier.width(50.dp),
                            singleLine = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal, imeAction = ImeAction.Next)
                        )

                        IconButton(onClick = {
                            val current = quantity.toDoubleOrNull() ?: 0.0
                            quantity = (current + 1).toString().replace(".0", "")
                        }) {
                            Text("+", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Quantity type selector
                CustomNotchedField(
                    label = "Unit",
                    modifier = Modifier.weight(1f)
                ) {
                    ExposedDropdownMenuBox(
                        expanded = expandedUnitDropdown,
                        onExpandedChange = { expandedUnitDropdown = it }
                    ) {
                        TextField(
                            value = selectedUnit,
                            onValueChange = {},
                            modifier = Modifier.fillMaxWidth().menuAnchor(),
                            readOnly = true,
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                focusedIndicatorColor = Color.Transparent,
                                unfocusedIndicatorColor = Color.Transparent
                            ),
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedUnitDropdown) }
                        )
                        ExposedDropdownMenu(expanded = expandedUnitDropdown, onDismissRequest = { expandedUnitDropdown = false }) {
                            quantityTypes.forEach { unit ->
                                DropdownMenuItem(text = { Text(unit) }, onClick = { selectedUnit = unit; expandedUnitDropdown = false })
                            }
                        }
                    }
                }
            }

            // Category Selector
            CustomNotchedField(label = "Category") {
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it }
                ) {
                    TextField(
                        value = selectedCategory,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth().menuAnchor(),
                        readOnly = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) }
                    )
                    ExposedDropdownMenu(expanded = expandedDropdown, onDismissRequest = { expandedDropdown = false }) {
                        categories.forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    expandedDropdown = false
                                    val defaultMillis = CategoryConstants.getDefaultExpiryMillis(category)
                                    expiryDateMs = defaultMillis
                                    expiryDate = dateFormat(defaultMillis)
                                }
                            )
                        }
                    }
                }
            }

            // Date Picker
            if (destination == AddItemDestination.Inventory) {
                CustomNotchedField(label = "Expiry Date") {
                    TextField(
                        value = expiryDate,
                        onValueChange = {},
                        modifier = Modifier.fillMaxWidth(),
                        readOnly = true,
                        colors = TextFieldDefaults.colors(
                            focusedContainerColor = Color.Transparent,
                            unfocusedContainerColor = Color.Transparent,
                            focusedIndicatorColor = Color.Transparent,
                            unfocusedIndicatorColor = Color.Transparent
                        ),
                        trailingIcon = { IconButton(onClick = { showDatePicker = true }) { Icon(Icons.Default.DateRange, contentDescription = null) } }
                    )
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = saveAsFavorite, onCheckedChange = { saveAsFavorite = it })
                Text("Save this item as favorite")
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                // Cancel Button
                TextButton(onClick = { navController.popBackStack() }) { Text("Cancel") }
                Spacer(modifier = Modifier.width(8.dp))
                // Save Button.
                Button(onClick = { viewModel.saveItem(productName, quantity, selectedCategory, selectedUnit, expiryDateMs, destination, saveAsFavorite) }) { Text("Save Item") }
            }
        }
    }

    // Calendar Logic
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    showDatePicker = false
                    datePickerState.selectedDateMillis?.let { expiryDateMs = it; expiryDate = dateFormat(it) }
                }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = datePickerState) }
    }
}

@Composable
fun CustomNotchedField(
    label: String,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    Box(modifier = modifier.padding(top = 8.dp)) {
        Box(
            modifier = Modifier
                .border(
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    shape = RoundedCornerShape(4.dp)
                )
                .padding(horizontal = 4.dp, vertical = 2.dp)
        ) {
            content()
        }

        Box(
            modifier = Modifier
                .offset(x = 12.dp, y = (-8).dp)
                .background(MaterialTheme.colorScheme.background)
                .padding(horizontal = 4.dp)
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }
    }
}

// Auxiliary function to turn milliseconds to dates
fun dateFormat(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}