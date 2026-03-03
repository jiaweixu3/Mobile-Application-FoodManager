package com.example.foodmanager.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Import the interface
import com.example.foodmanager.repository.InventoryRepository

class InventoryViewModel(
    // Put the interface in the constructor
    private val repository: InventoryRepository
) : ViewModel() {

    private val _inventory = MutableStateFlow<List<FoodItem>>(emptyList())
    val inventory: StateFlow<List<FoodItem>> = _inventory.asStateFlow()

    init {
        // Call the getInventory function from the repository
        viewModelScope.launch {
            repository.getInventory().collect { items ->
                _inventory.value = items
            }
        }
    }
}