package com.example.foodmanager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.foodmanager.data.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch



// Defines simple settings screen, for now it only contains log out.
@OptIn(ExperimentalMaterial3Api::class) // Needed for the Top App Bar to function correctly
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    logoutSuccess: () -> Unit, // Handles logging out logic
    onHouseholdSelected: () -> Unit,
    onNavigateToMembers: () -> Unit

) {
    // Available households, collects from Mock Db
    val availableHouseholds by viewModel.availableHouseholds.collectAsState()


    // Controls if dropdown menu is active or not
    var expandedDropdown by remember {mutableStateOf(false)}

    // Stores current households, first is active by default
    val currentHousehold by viewModel.currentHousehold.collectAsState()

    // Storing the name for the New Household
    var newHouseholdName by remember { mutableStateOf("") }
    var logoutErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    // Storing new name
    var editHouseholdName by remember { mutableStateOf("") }

    // Updating the text field when the name changes
    LaunchedEffect(currentHousehold){
        editHouseholdName = currentHousehold?.name?: ""
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Settings") })
        }
    ) { innerPadding ->
        // General column for the settings screen
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // HOUSEHOLD MANAGEMENT
            // Will not display anything if there are no available Households
            if (availableHouseholds.isEmpty()) {
                Text("No available households")
            } else {

                // Dropdown for chosing households
                ExposedDropdownMenuBox(
                    expanded = expandedDropdown,
                    onExpandedChange = { expandedDropdown = it },
                ) {
                    OutlinedTextField(
                        value = currentHousehold?.name ?: "", // Ensuring it handles NA values
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Current Household") },
                        trailingIcon = {
                            ExposedDropdownMenuDefaults.TrailingIcon(
                                // Arrow icon for selecting variables
                                expanded = expandedDropdown,
                            )
                        },
                        modifier = Modifier.menuAnchor()

                    )

                    // Dropwdown Menu
                    ExposedDropdownMenu(
                        expanded = expandedDropdown,
                        onDismissRequest = {
                            expandedDropdown = false
                        }
                    ) {
                        // Iterating through the available households
                        availableHouseholds.forEachIndexed { index, household ->
                            DropdownMenuItem(
                                text = {
                                    Row(
                                        horizontalArrangement = Arrangement.spacedBy(10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                    ) {
                                        Text(household.name)
                                    }
                                },
                                // Clicking the button will update the current variables
                                onClick = {
                                    val selectedHousehold = availableHouseholds[index]
                                    viewModel.onHouseholdChanged(selectedHousehold) // Applying Household selection change
                                    expandedDropdown = false
                                    onHouseholdSelected() // Navigates to the inventory of the new household
                                },

                                contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding
                            )
                        }
                    }
                }

            }

            // Separation
            Spacer(modifier = Modifier.padding(8.dp))

            // Displaying a household
            Row(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ){
                // Text box
                OutlinedTextField(
                    value = newHouseholdName,
                    onValueChange = {newHouseholdName = it},
                    label = { Text("New household") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                // Button for adding a new household
                Button(
                    onClick = {
                        viewModel.addNewHousehold(newHouseholdName) // Adding the new button
                        newHouseholdName = "" // Clear the box after adding
                    },
                    enabled = newHouseholdName.isNotBlank(), // Cant add if box is empty
                    modifier = Modifier.padding(start = 8.dp)
                ) {
                    Text("Add")
                }
            }

            // Separation
            Spacer(modifier = Modifier.padding(8.dp))

            // Updating the name of the household logic
            Text(
                text = "Edit Name",
                style = MaterialTheme.typography.titleMedium
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.6f)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ){
                OutlinedTextField(
                    value = editHouseholdName,
                    onValueChange = {editHouseholdName = it},
                    label = { Text("Rename Household")},
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                Button(
                    onClick = {
                        viewModel.updateHouseholdName(editHouseholdName)
                    },
                    // Enabled only if new name is not empty and different from the current name
                    enabled = editHouseholdName.isNotBlank() && editHouseholdName != currentHousehold?.name,
                    modifier = Modifier.padding(start = 8.dp)
                ){
                    Text("Save")
                }
            }
            // Separation
            Spacer(modifier = Modifier.padding(8.dp))

            // Seeing Household Members
            if (currentHousehold != null) {
                Button(
                    onClick = { onNavigateToMembers() }, // Llama a la función de navegación
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text("View Household Members")
                }
            }


            // Logout
            Button(
                onClick = {
                    coroutineScope.launch {
                        isLoggingOut = true
                        logoutErrorMessage = null
                        try {
                            supabase.auth.signOut()
                            logoutSuccess()
                        } catch (e: Exception) {
                            logoutErrorMessage = e.message ?: "Logout failed."
                        } finally {
                            isLoggingOut = false
                        }
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red),
                enabled = !isLoggingOut

            ) {
                Text(text = "Log Out")
            }

            if (logoutErrorMessage != null) {
                Spacer(modifier = Modifier.padding(8.dp))
                Text(text = logoutErrorMessage ?: "", color = MaterialTheme.colorScheme.error)
            }




        }
    }
}

// Function for ensuring email is valid
fun String.isValidEmail(): Boolean{
    val emailRegex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[a-zA-z]{2,}\$".toRegex()
    return this.matches(emailRegex)
}
