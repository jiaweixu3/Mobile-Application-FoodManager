package com.example.foodmanager.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodmanager.domain.calculateDaysRemaining
import com.example.foodmanager.domain.model.FoodItem
import androidx.compose.runtime.collectAsState
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton

@Composable
fun FoodCard(
    item: FoodItem,
    viewModel: InventoryViewModel, // Added
    modifier: Modifier = Modifier
) {
    var showConsumeDialog by remember { mutableStateOf(false) }
    val suggestedItem by viewModel.suggestedItem.collectAsState()
    // Calculate days until expiration
    val daysRemaining = calculateDaysRemaining(item)

    // Color status
    val statusColor = when {
        daysRemaining < 0 -> Color.Red      // Expired
        daysRemaining <= 3 -> Color(0xFFFFCC00)  // Close expiration
        else -> Color.Green                 // Fresh
    }

    // Container
    Card(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface) // White background usually
    ) {
        // Make rows of 3 columns
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Image Box
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(Color.LightGray),
                contentAlignment = Alignment.Center
            ) {
                // Show the first letter of the item, if it had one
                // the image of the item should be shown here
                Text(
                    text = item.name.first().toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            // Text and Item Details
            Column(
                modifier = Modifier.weight(1f) // Takes up remaining space
            ) {
                Text(
                    text = item.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${item.amount} ${item.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
            }

            // Expiration Section
            Column(
                horizontalAlignment = Alignment.End
            ) {
                // Color based on expiration
                Text(
                    text = if (daysRemaining < 0) "Expired" else "$daysRemaining days",
                    color = statusColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp
                )
                Text(
                    text = item.expiryDate,
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { showConsumeDialog = true }, // Shows consume popup box
                    modifier = Modifier.height(36.dp)
                ) {
                    Text("Consume", fontSize = 12.sp)
                }
            }
        }
    }
    // Show the dialog if the state is true
    if (showConsumeDialog) {
        ConsumeItemDialog(
            foodItem = item,
            onDismiss = { showConsumeDialog = false },
            onConfirm = { consumed, buy, addToList ->
                // Updates the viewModel
                viewModel.consumeItem(item, consumed, addToList, buy)
                showConsumeDialog = false
            }
        )
    }
}

// Consume Dialog Composable
@Composable
fun ConsumeItemDialog(
    foodItem: FoodItem,
    onDismiss: () -> Unit,
    onConfirm: (consumed: Double, buyAmount: Double, addToList: Boolean) -> Unit
) {
    var consumedAmountText by remember { mutableStateOf("1.0") }
    var buyAmountText by remember { mutableStateOf("1.0") }
    var addToShoppingList by remember { mutableStateOf(false) }

    // Validation logic
    val consumedValue = consumedAmountText.toDoubleOrNull() ?: 0.0
    val isError = consumedValue > foodItem.amount
    val isInputValid = consumedAmountText.isNotEmpty() && consumedValue > 0 && !isError

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Consume ${foodItem.name}") },
        text = {
            Column {
                Text(
                    text = "Current amount: ${foodItem.amount} ${foodItem.unit}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (isError) MaterialTheme.colorScheme.error else Color.Unspecified
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = consumedAmountText,
                    onValueChange = { consumedAmountText = it },
                    label = { Text("Amount used") },
                    isError = isError,
                    supportingText = {
                        if (isError) {
                            Text("Cannot exceed ${foodItem.amount} ${foodItem.unit}")
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Checkbox(
                        checked = addToShoppingList,
                        onCheckedChange = { addToShoppingList = it }
                    )
                    Text("Add to shopping list?")
                }

                // Only shows if the checkbox is checked
                if (addToShoppingList) {
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = buyAmountText,
                        onValueChange = { buyAmountText = it },
                        label = { Text("Quantity to buy next") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                    )
                }
            }
        },
        confirmButton = {
            Button(
                enabled = isInputValid, // Button only works if input is valid
                onClick = {
                    val consumed = consumedAmountText.toDoubleOrNull() ?: 0.0
                    val buy = buyAmountText.toDoubleOrNull() ?: 1.0
                    onConfirm(consumed, buy, addToShoppingList)
                }
            ) {
                Text("Confirm")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}