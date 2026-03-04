package com.example.foodmanager.ui.inventory

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.model.FoodItem
import com.example.foodmanager.repository.InventoryRepository
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
import com.example.foodmanager.domain.ConsumeFoodItemUseCase
import com.example.foodmanager.repository.ShoppingListRepository
import com.example.foodmanager.model.ShoppingItem
import kotlinx.coroutines.flow.first

class InventoryViewModel(
    private val repository: InventoryRepository,
    private val shoppingRepository: ShoppingListRepository,
    private val consumeFoodItemUseCase: ConsumeFoodItemUseCase
) : ViewModel() {

    // Raw data from repository
    private val _inventory = MutableStateFlow<List<FoodItem>>(emptyList())
    val inventory: StateFlow<List<FoodItem>> = _inventory.asStateFlow()

    // Selected storage location (Fridge, Pantry, etc.). null = show all
    private val _selectedLocation = MutableStateFlow<String?>(null)
    val selectedLocation: StateFlow<String?> = _selectedLocation.asStateFlow()

    private val _suggestedItem = MutableStateFlow<FoodItem?>(null)
    val suggestedItem: StateFlow<FoodItem?> = _suggestedItem

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

    fun dismissSuggestion() {
        _suggestedItem.value = null
    }
    fun consumeItem(item: FoodItem, consumed: Double, addToList: Boolean, buyQty: Double) {
        // safety check
        if (consumed > item.amount && !addToList) return
        viewModelScope.launch {
            // Reset the suggestion to null so the UI trigger is fresh
            _suggestedItem.value = null

            // Calculate the projected amount before the database update
            val newAmount = item.amount - consumed

            // Execute the use case (updates inventory and/or adds to shopping list)
            consumeFoodItemUseCase(item, consumed, addToList, buyQty)

            // Only trigger if the user didn't manually add
            // the item AND it has now reached zero (or less)
            if (!addToList && newAmount <= 0.0) {

                // Check the current shopping list to avoid duplicate suggestions
                val currentList = shoppingRepository.getShoppingList().first()

                val alreadyExists = currentList.any { listItem ->
                    listItem.name.equals(item.name, ignoreCase = true)
                }

                if (!alreadyExists) {
                    // Set the item to trigger the second dialogue in InventoryScreen
                    _suggestedItem.value = item
                }
            }
        }
    }
}