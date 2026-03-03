package com.example.foodmanager.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.model.FoodItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

// Import the interface
import com.example.foodmanager.domain.sortAndFilterInventory
import com.example.foodmanager.repository.InventoryRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

class InventoryViewModel(
    private val repository: InventoryRepository
) : ViewModel() {

    // Raw data from repository
    private val _inventory = MutableStateFlow<List<FoodItem>>(emptyList())
    val inventory: StateFlow<List<FoodItem>> = _inventory.asStateFlow()

    // Selected storage location (Fridge, Pantry, etc.). null = show all
    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation: StateFlow<String?> = _selectedLocation.asStateFlow()

    // Public list the UI should show: sorted by expiry and filtered by location
    val visibleInventory: StateFlow<List<FoodItem>> =
        combine(_inventory, _selectedLocation) { items, location ->
            sortAndFilterInventory(items, location)
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = emptyList()
        )

    init {
        viewModelScope.launch {
            repository.getInventory().collect { items ->
                _inventory.value = items
            }
        }
    }

    // Call this from the UI to change location filter
    fun setLocationFilter(location: String?) {
        _selectedLocation.value = location
    }
}