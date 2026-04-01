package com.example.foodmanager.ui.settings

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.foodmanager.data.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    logoutSuccess: () -> Unit,
    onHouseholdSelected: () -> Unit,
    onNavigateToMembers: () -> Unit
) {
    val availableHouseholds by viewModel.availableHouseholds.collectAsState()
    val currentHousehold by viewModel.currentHousehold.collectAsState()
    val joinMessage by viewModel.joinMessage.collectAsState()
    val userEmail by viewModel.userEmail.collectAsState()
    val passwordMessage by viewModel.passwordMessage.collectAsState()

    var expandedDropdown by remember { mutableStateOf(false) }
    var newHouseholdName by remember { mutableStateOf("") }
    var editHouseholdName by remember { mutableStateOf("") }
    var newPassword by remember { mutableStateOf("") }
    var joinInput by remember { mutableStateOf("") }
    var logoutErrorMessage by remember { mutableStateOf<String?>(null) }
    var isLoggingOut by remember { mutableStateOf(false) }

    val clipboardManager = LocalClipboardManager.current
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    LaunchedEffect(currentHousehold) {
        editHouseholdName = currentHousehold?.name.orEmpty()
    }

    LaunchedEffect(viewModel) {
        viewModel.uiEvent.collect { event ->
            when (event) {
                is SettingsUiEvent.ShowMessage -> snackbarHostState.showSnackbar(event.message)
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = { CenterAlignedTopAppBar(title = { Text("Settings") }) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(scrollState)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsCard(title = "Account Details") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Signed in as:",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = userEmail,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold
                    )

                    HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = newPassword,
                            onValueChange = {
                                newPassword = it
                                viewModel.clearPasswordMessage()
                            },
                            label = { Text("New Password") },
                            singleLine = true,
                            visualTransformation = PasswordVisualTransformation(),
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(24.dp)
                        )
                        Button(
                            onClick = {
                                viewModel.changePassword(newPassword)
                                newPassword = ""
                            },
                            enabled = newPassword.isNotBlank(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Update")
                        }
                    }

                    if (passwordMessage != null) {
                        Text(
                            text = passwordMessage.orEmpty(),
                            color = if (passwordMessage?.contains("Successfully") == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                }
            }

            SettingsCard(title = "Household Management") {
                if (availableHouseholds.isEmpty()) {
                    Text("No available households")
                } else {
                    ExposedDropdownMenuBox(
                        expanded = expandedDropdown,
                        onExpandedChange = { expandedDropdown = it }
                    ) {
                        OutlinedTextField(
                            value = currentHousehold?.name.orEmpty(),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Active Household") },
                            trailingIcon = {
                                ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedDropdown)
                            },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            shape = RoundedCornerShape(24.dp)
                        )
                        DropdownMenu(
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
                        enabled = currentHousehold != null &&
                            editHouseholdName.isNotBlank() &&
                            editHouseholdName != currentHousehold?.name,
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Text("Save")
                    }
                }
            }

            if (currentHousehold != null) {
                SettingsCard(title = "Invite & Members") {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        TextButton(
                            onClick = onNavigateToMembers,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(Icons.Default.Info, contentDescription = null)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("View Household Members", fontWeight = FontWeight.Bold)
                        }

                        HorizontalDivider()

                        Text(
                            text = "Share Join Code",
                            style = MaterialTheme.typography.labelLarge
                        )

                        currentHousehold?.joinCode?.let { code ->
                            Surface(
                                color = MaterialTheme.colorScheme.secondaryContainer,
                                shape = RoundedCornerShape(24.dp),
                                modifier = Modifier.align(Alignment.CenterHorizontally)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    modifier = Modifier.padding(start = 24.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
                                ) {
                                    Text(
                                        text = code,
                                        style = MaterialTheme.typography.headlineSmall,
                                        fontWeight = FontWeight.ExtraBold
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    IconButton(
                                        onClick = {
                                            clipboardManager.setText(AnnotatedString(code))
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("Code copied")
                                            }
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Copy code")
                                    }
                                }
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

            SettingsCard(title = "Join a Household") {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = joinInput,
                            onValueChange = { input ->
                                joinInput = input.filter(Char::isLetterOrDigit).uppercase().take(6)
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
                            enabled = joinInput.isNotBlank(),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Text("Join")
                        }
                    }

                    if (joinMessage != null) {
                        Text(
                            text = joinMessage.orEmpty(),
                            color = if (joinMessage?.contains("successfully", ignoreCase = true) == true) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.error
                            },
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(top = 8.dp, start = 4.dp)
                        )
                    }
                }
            }

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
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Log Out")
            }

            if (logoutErrorMessage != null) {
                Text(
                    text = logoutErrorMessage.orEmpty(),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(start = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
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
            Box(modifier = Modifier.padding(16.dp)) {
                content()
            }
        }
    }
}
