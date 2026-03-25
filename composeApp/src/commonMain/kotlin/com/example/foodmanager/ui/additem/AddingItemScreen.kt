package com.example.foodmanager.ui.additem

import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.Alignment
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

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var expiryDateMs by remember { mutableStateOf<Long?>(null) }
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
                is AddItemUiEvent.ShowError -> {
                    snackbarHostState.showSnackbar(event.message)
                }
                is AddItemUiEvent.NavigateBack -> {
                    navController.popBackStack()
                }
            }
        }
    }

    // User Interface
    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Add New Item") })
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Where to add this item?", style = MaterialTheme.typography.labelLarge)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = destination == AddItemDestination.Inventory,
                        onClick = { destination = AddItemDestination.Inventory }
                    )
                    Text("Inventory")
                }
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(
                        selected = destination == AddItemDestination.ShoppingList,
                        onClick = { destination = AddItemDestination.ShoppingList }
                    )
                    Text("Shopping List")
                }
            }

            FavoriteItemsSection(
                favorites = favoriteItems,
                selectedFavoriteLabel = selectedFavoriteLabel,
                onFavoriteSelected = { favorite ->
                    selectedFavoriteLabel = favorite.name
                    productName = favorite.name
                    quantity = favorite.amount.toString()
                    selectedUnit = favorite.unit
                    selectedCategory = favorite.category

                    val defaultMillis = CategoryConstants.getDefaultExpiryMillis(favorite.category)
                    expiryDateMs = defaultMillis
                    expiryDate = dateFormat(defaultMillis)
                }
            )


            // Product name handling
            OutlinedTextField(
                value = productName,
                onValueChange = { productName = it },
                label = { Text("Product Name") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next)
            )

            // Product Quantity handling
            OutlinedTextField(
                value = quantity,
                onValueChange = { quantity = it },
                label = { Text("Quantity") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Decimal,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            // Category Selector
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedCategory,
                    onValueChange = {},
                    label = { Text("Category") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            "Select Category",
                            Modifier.clickable { expandedDropdown = true }
                        )
                    }
                )


                DropdownMenu(
                    expanded = expandedDropdown,
                    onDismissRequest = { expandedDropdown = false }
                ) {
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

            // Quantity type selector (grams, litres, units, etc.)
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = selectedUnit,
                    onValueChange = {},
                    label = { Text("Quantity Type") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        Icon(
                            Icons.Filled.ArrowDropDown,
                            "Select Quantity Type",
                            Modifier.clickable { expandedUnitDropdown = true }
                        )
                    }
                )

                DropdownMenu(
                    expanded = expandedUnitDropdown,
                    onDismissRequest = { expandedUnitDropdown = false }
                ) {
                    quantityTypes.forEach { unit ->
                        DropdownMenuItem(
                            text = { Text(unit) },
                            onClick = {
                                selectedUnit = unit
                                expandedUnitDropdown = false
                            }
                        )
                    }
                }
            }

            // Date Picker

            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = expiryDate,
                    onValueChange = {},
                    label = { Text("Expiry Date") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { showDatePicker = true }) {
                            Icon(Icons.Default.DateRange, contentDescription = null)
                        }
                    }
                )
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                // Cancel Button
                TextButton(onClick = { navController.popBackStack() }) {
                    Text("Cancel")
                }
                Spacer(modifier = Modifier.width(8.dp))

                // Save Button.
                Button(onClick = {
                    viewModel.saveItem(
                        name = productName,
                        quantity = quantity,
                        category = selectedCategory,
                        unit = selectedUnit,
                        expiryDateMs = expiryDateMs,
                        destination = destination,
                        saveAsFavorite = saveAsFavorite
                    )
                }) {
                    Text("Save Item")
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Checkbox(
                    checked = saveAsFavorite,
                    onCheckedChange = { saveAsFavorite = it }
                )
                Text("Save this item as favorite")
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
                    val selectedMillis = datePickerState.selectedDateMillis
                    if (selectedMillis != null) {
                        expiryDateMs = selectedMillis
                        expiryDate = dateFormat(selectedMillis)
                    }
                }) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FavoriteItemsSection(
    favorites: List<FavoriteFoodItem>,
    selectedFavoriteLabel: String,
    onFavoriteSelected: (FavoriteFoodItem) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text("Favorite items", style = MaterialTheme.typography.labelLarge)
        var expanded by remember { mutableStateOf(false) }
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = it }
        ) {
            OutlinedTextField(
                value = selectedFavoriteLabel,
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor(),
                readOnly = true,
                label = { Text("Choose a favorite") },
                placeholder = {
                    Text(
                        if (favorites.isEmpty()) "No favorite items yet" else "Select a saved favorite item"
                    )
                },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) }
            )

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                if (favorites.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No favorite items yet") },
                        onClick = { expanded = false },
                        enabled = false
                    )
                } else {
                    favorites.forEach { favorite ->
                        DropdownMenuItem(
                            text = { Text("${favorite.name} • ${favorite.amount} ${favorite.unit}") },
                            onClick = {
                                onFavoriteSelected(favorite)
                                expanded = false
                            }
                        )
                    }
                }
            }
        }
        Text(
            text = "Choosing a favorite only pre-fills the form. You can still change quantity and expiry date before saving.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

// Auxiliary function to turn milliseconds to dates
fun dateFormat(millis: Long): String {
    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    formatter.timeZone = TimeZone.getTimeZone("UTC")
    return formatter.format(Date(millis))
}

