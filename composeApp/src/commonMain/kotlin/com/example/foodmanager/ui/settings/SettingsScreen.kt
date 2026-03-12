package com.example.foodmanager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

// Defines simple settings screen, for now it only contains log out.
@OptIn(ExperimentalMaterial3Api::class) // Needed for the Top App Bar to function correctly
@Composable
fun SettingsScreen(
    logoutSuccess: () -> Unit, // Handles logging out logic
    onHouseholdSelected: (String) -> Unit = {} // Handles current household
) {
    // Available households, for now this is mock data
    val availableHouseholds = listOf("house 1", "house 2", "house 3")


    // Controls if dropdown menu is active or not
    var expandedDropdown by remember {mutableStateOf(false)}

    // Stores current households, first is active by default
    var currentHousehold by remember { mutableStateOf(availableHouseholds[0]) }

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
            // Household Management
            Text(
                text = "Households",
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier
                    .padding(bottom = 16.dp)

            )
            ExposedDropdownMenuBox(
               expanded = expandedDropdown,
                onExpandedChange = { expandedDropdown = it },
            ){
                OutlinedTextField(
                    value = currentHousehold,
                    onValueChange = {},
                    readOnly = true,
                    label = {Text("Current Household")}
                )
            }
            // Separation
            Spacer(modifier = Modifier.padding(8.dp))



            // Logout
            Button(
                onClick = {
                    logoutSuccess()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color.Red)

            ) {
                Text(text = "Log Out")
            }




        }
    }
}