package com.example.foodmanager.ui.inventory

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.foodmanager.model.FoodItem
/*import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.TimeZone*/

@Composable
fun FoodCard(
    item: FoodItem,
    modifier: Modifier = Modifier
) {
    // Calculate days until expiration
    val daysRemaining = calculateDaysRemaining(item.expiryDate)

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
            }
        }
    }
}

fun calculateDaysRemaining(expiryDate: String): Int {
    /*try {
        // Convert the String into a Date object
        val expiryDate = LocalDate.parse(expiryDate)
        // Get current date
        val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
        // Return the difference between both
        return today.daysUntil(expiryDate)

    } catch (e: Exception) {
        // Return 0 if the format of the date is wrong
        return 0
    }*/

    // Has to be updated by library crashes
    return when (expiryDate) {
        "2026-02-01" -> -5
        "2026-02-05" -> 2
        "2026-03-15" -> 30
        else -> 10
    }
}