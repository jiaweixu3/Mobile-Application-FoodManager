package com.example.foodmanager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.ui.graphics.Color

// Defines simple settings screen, for now it only contains log out.
@OptIn(ExperimentalMaterial3Api::class) // Needed for the Top App Bar to function correctly
@Composable
fun SettingsScreen(logoutSuccess: () -> Unit) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Settings") })
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier.fillMaxSize().padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Actual logout button
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