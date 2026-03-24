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
import com.example.foodmanager.ui.settings.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HouseholdScreen(
    viewModel: SettingsViewModel,
    onBackClick: () -> Unit
) {
    // 1. Observe the REAL data from Supabase!
    val members by viewModel.members.collectAsState()
    val isLoading by viewModel.isLoadingMembers.collectAsState()

    // 👇 THIS IS THE MISSING PIECE! 👇
    // This tells the ViewModel to actually fetch the data the moment the screen opens
    LaunchedEffect(Unit) {
        viewModel.getHouseholdMembers()
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
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 2. Show a loading spinner while Supabase fetches the data
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
            }
            // 3. Show a message if no one is in the list
            else if (members.isEmpty()) {
                Text(
                    text = "No members found.",
                    modifier = Modifier.align(Alignment.Center),
                    color = Color.Gray
                )
            }
            // 4. Display the real members using YOUR design!
            else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(members) { member ->
                        // Handle potential nulls from the database cleanly
                        val role = member.role ?: "member"
                        val displayName = member.name ?: member.email ?: "Unknown User"

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(16.dp)
                                    .fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(Icons.Default.Person, contentDescription = null, modifier = Modifier.size(40.dp))
                                    Spacer(modifier = Modifier.width(16.dp))
                                    Column {
                                        Text(
                                            text = displayName,
                                            fontWeight = FontWeight.Bold,
                                            style = MaterialTheme.typography.titleMedium
                                        )
                                        Text(
                                            text = role.replaceFirstChar { it.uppercase() },
                                            color = Color.Gray,
                                            style = MaterialTheme.typography.bodyMedium
                                        )
                                    }
                                }

                                // Delete button logic
                                if (role.lowercase() != "owner") {
                                    IconButton(onClick = {
                                        // TODO: Add a function like viewModel.removeMember(member.id) here!
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
    }
}