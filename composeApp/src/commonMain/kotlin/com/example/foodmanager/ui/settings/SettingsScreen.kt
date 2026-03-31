package com.example.foodmanager.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.foodmanager.data.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

// Defines settings screen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    logoutSuccess: () -> Unit,
    onHouseholdSelected: () -> Unit,
    onNavigateToMembers: () -> Unit
) {
    val availableHouseholds by viewModel.availableHouseholds.collectAsState()
    var expandedDropdown by remember { mutableStateOf(false) }
    val currentHousehold by viewModel.currentHousehold.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    var newHouseholdName by remember { mutableStateOf("") }
    var logoutErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingOut by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()
    var editHouseholdName by remember { mutableStateOf("") }

    LaunchedEffect(currentHousehold) {
        editHouseholdName = currentHousehold?.name ?: ""
    }

    Scaffold(
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) },
        topBar = {
            CenterAlignedTopAppBar(title = { Text("Settings") })
        }
    ) { innerPadding ->
        LaunchedEffect(viewModel) {
            viewModel.uiEvent.collect { event ->
                when (event) {
                    is SettingsUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
                }
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // household management section
            SettingsCard(title = "Household Management") {
                if (availableHouseholds.isEmpty()) {
                    Text("No available households")
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = it },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = currentHousehold?.name ?: "",
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Active Household") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        )
                        ExposedDropdownMenu(
                            expanded = expandedDropdown,
                            onDismissRequest = { expandedDropdown = false }
                        ) {
                            availableHouseholds.forEach { household ->
                                DropdownMenuItem(
                                    text = { Text(household.name) },
                                    onClick = {
                                        viewModel.onHouseholdChanged(household)
                                        expandedDropdown = false
                                        onHouseholdSelected()
                                    }
                                )
                            }
                        }
                    }
                }
            }

            // create household section
            SettingsCard(title = "Create New Household") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = newHouseholdName,
                        onValueChange = { newHouseholdName = it },
                        label = { Text("Name") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.addNewHousehold(newHouseholdName)
                            newHouseholdName = ""
                        },
                        enabled = newHouseholdName.isNotBlank(),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Add")
                    }
                }
            }

            // edit household section
            SettingsCard(title = "Edit Current Household") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = editHouseholdName,
                        onValueChange = { editHouseholdName = it },
                        label = { Text("Rename to...") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Button(
                        onClick = { viewModel.updateHouseholdName(editHouseholdName) },
                        enabled = editHouseholdName.isNotBlank() && editHouseholdName != currentHousehold?.name,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Save")
                    }
                }
            }

            // invite and members section
            if (currentHousehold != null) {
                SettingsCard(title = "Invite & Members") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = { onNavigateToMembers() },
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(Modifier.width(8.dp))
                            Text("View Household Members", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider(modifier = Modifier.padding(vertical = 4.dp))

                        Text(
                            text = "Share Join Code",
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (currentHousehold?.joinCode != null) {
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Text(
                                    text = currentHousehold?.joinCode ?: "",
                                    modifier = Modifier.padding(horizontal = 24.dp, vertical = 8.dp),
                                    style = MaterialTheme.typography.headlineSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }

                        Button(
                            onClick = { viewModel.generateCodeHousehold() },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text(if (currentHousehold?.joinCode == null) "Generate Code" else "Regenerate Code")
                        }
                    }
                }
            }

            // join household section
            var joinInput by remember { mutableStateOf("") }
            SettingsCard(title = "Join a Household") {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    OutlinedTextField(
                        value = joinInput,
                        onValueChange = { input ->
                            joinInput = input
                                .filter(Char::isLetterOrDigit)
                                .uppercase()
                                .take(6)
                        },
                        label = { Text("6-character code") },
                        singleLine = true,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp)
                    )
                    Button(
                        onClick = {
                            viewModel.joinHousehold(joinInput)
                            joinInput = ""
                        },
                        enabled = joinInput.length == 6,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Join")
                    }
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
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                enabled = !isLoggingOut,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier.fillMaxWidth().padding(top = 16.dp)
            ) {
                Text(text = "Log Out")
            }
        }
    }
}

@Composable
fun SettingsCard(title: String, content: @Composable () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
