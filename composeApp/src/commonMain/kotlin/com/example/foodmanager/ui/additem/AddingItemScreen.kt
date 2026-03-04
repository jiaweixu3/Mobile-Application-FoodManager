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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.foodmanager.data.repository.MockInventoryRepository

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddingItemScreen(
    navController: NavController,
    viewModel: AddItemViewModel =
        viewModel { AddItemViewModel(MockInventoryRepository()) }
) {

    var productName by remember { mutableStateOf("") }
    var quantity by remember { mutableStateOf("") }
    var expiryDate by remember { mutableStateOf("") }
    var expiryDateMs by remember { mutableStateOf<Long?>(null) }
    var selectedCategory by remember { mutableStateOf("Other") }
    var selectedUnit by remember { mutableStateOf("units") }

    var showDatePicker by remember { mutableStateOf(false) }
    var expandedDropdown by remember { mutableStateOf(false) }
    var expandedUnitDropdown by remember { mutableStateOf(false) }

    val snackbarHostState = remember { SnackbarHostState() }

    val focusManager = LocalFocusManager.current

    val categories = listOf(
        "Vegetables", "Fruits", "Meat", "Dairy", "Bread", "Pasta", "Rice",
        "Frozen", "Other"
    )
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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


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
                    keyboardType = KeyboardType.Number,
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
                    viewModel.saveItem(productName, quantity, selectedCategory, selectedUnit, expiryDateMs)
                }) {
                    Text("Save Item")
                }
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

// Auxiliary function to turn milliseconds to dates
fun dateFormat(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

