    package com.example.foodmanager.ui.inventory

    import androidx.compose.foundation.layout.Arrangement
    import androidx.compose.foundation.layout.Column
    import androidx.compose.foundation.layout.PaddingValues
    import androidx.compose.foundation.layout.Row
    import androidx.compose.foundation.layout.fillMaxSize
    import androidx.compose.foundation.layout.fillMaxWidth
    import androidx.compose.foundation.layout.padding
    import androidx.compose.foundation.lazy.LazyColumn
    import androidx.compose.foundation.lazy.items
    import androidx.compose.material3.Card
    import androidx.compose.material3.CardDefaults
    import androidx.compose.material3.CenterAlignedTopAppBar
    import androidx.compose.material3.ExperimentalMaterial3Api
    import androidx.compose.material3.Scaffold
    import androidx.compose.material3.Text
    import androidx.compose.runtime.Composable
    import androidx.compose.runtime.collectAsState
    import androidx.compose.runtime.getValue
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.text.font.FontWeight
    import androidx.compose.ui.unit.dp
    import androidx.compose.ui.unit.sp
    import androidx.lifecycle.viewmodel.compose.viewModel

    @Composable
    fun SummaryBox(
        count: Int,
        label: String,
        backgroundColor: Color,
        modifier: Modifier = Modifier
    ) {
        Card(
            modifier = modifier,
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = count.toString(),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White // White text looks good on Red/Green/Yellow
                )
                Text(
                    text = label,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White
                )
            }
        }
    }


    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun InventoryScreen() {
        // Get the ViewModel
        val viewModel: InventoryViewModel = viewModel { InventoryViewModel() }

        // Collect data
        val inventoryList by viewModel.inventory.collectAsState()

        // Calculate counts
        val expiredCount = inventoryList.count { calculateDaysRemaining(it.expiryDate) < 0 }
        val warningCount = inventoryList.count { calculateDaysRemaining(it.expiryDate) in 0..3 }
        val freshCount = inventoryList.count { calculateDaysRemaining(it.expiryDate) > 3 }

        Scaffold(
            topBar = {
                CenterAlignedTopAppBar(title = { Text("My Pantry")})
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp, 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    // weight(1f) makes them share the width equally
                    SummaryBox(
                        count = freshCount,
                        label = "Fresh",
                        backgroundColor = Color(0xFF4CAF50),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBox(
                        count = warningCount,
                        label = "Soon",
                        backgroundColor = Color(0xFFFFB300),
                        modifier = Modifier.weight(1f)
                    )
                    SummaryBox(
                        count = expiredCount,
                        label = "Expired",
                        backgroundColor = Color(0xFFE53935),
                        modifier = Modifier.weight(1f)
                    )
                }

                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize(),
                        contentPadding = PaddingValues(vertical = 8.dp)
                ) {
                    items(inventoryList) { foodItem ->
                        FoodCard(item = foodItem)
                    }
                }
            }
        }
    }