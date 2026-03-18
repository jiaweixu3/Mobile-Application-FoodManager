package com.example.foodmanager.ui.household

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.automirrored.filled.ArrowBack

// Mock Data
data class HouseholdMember(val id: Int, val name: String, val role: String)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdScreen(onBackClick: () -> Unit) {
    // Local state to visually "delete" members from the list
    var members by remember {
        mutableStateOf(listOf(
            HouseholdMember(1, "Álvaro", "Owner"),
            HouseholdMember(2, "Javi", "Admin"),
            HouseholdMember(3, "Sandra", "Viewer")
        ))
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text("Household Members") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(members) { member ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(16.dp).fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(text = member.name, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                                Text(text = member.role, color = Color.Gray, style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        // Delete button (currently mock functionality)
                        if (member.role != "Owner") { // The owner cannot be removed
                            IconButton(onClick = {
                                members = members.filter { it.id != member.id }
                            }) {
                                Icon(Icons.Default.Delete, contentDescription = "Remove", tint = Color.Red)
                            }
                        }
                    }
                }
            }
        }
    }
}