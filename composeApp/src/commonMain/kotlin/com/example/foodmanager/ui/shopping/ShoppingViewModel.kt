package com.example.foodmanager.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.domain.MarkAsBoughtUseCase
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.repository.ShoppingListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val repository: ShoppingListRepository,
    private val markAsBoughtUseCase: MarkAsBoughtUseCase
) : ViewModel() {

    // UI State
    val items: StateFlow<List<ShoppingItem>> = repository.getShoppingList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Toggles the checkbox state
    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(isChecked = !item.isChecked)
            repository.updateShoppingItem(updatedItem)
        }
    }

    // Deletes an item if swiped/clicked delete
    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    // Adds a brand new item directly to the shopping list
    fun addItem(name: String, amount: Double, unit: String, category: String) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            val newItem = ShoppingItem(
                id = (1000..9999).random(),
                name = name,
                amount = amount,
                unit = unit,
                category = category,
                isChecked = false
            )
            repository.addShoppingItem(newItem)
        }
    }

    // Moves checked items to inventory
    fun markCheckedItemsAsBought() {
        viewModelScope.launch {
            // Get the current list of items from the UI state
            val currentList = items.value

            // Filter out only the items that have their checkbox ticked
            val checkedItems = currentList.filter { it.isChecked }

            // Loop through the checked items and move them to the pantry
            checkedItems.forEach { item ->
                markAsBoughtUseCase(item)
            }
        }
    }
}