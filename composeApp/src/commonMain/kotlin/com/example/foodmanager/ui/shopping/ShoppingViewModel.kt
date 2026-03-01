package com.example.foodmanager.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.model.ShoppingItem
import com.example.foodmanager.repository.ShoppingListRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch


class ShoppingViewModel(private val repository: ShoppingListRepository) : ViewModel() {

    // 1. "Use of getShoppingList() into the UI state"
    // This converts the Flow from MockDb into a StateFlow the UI can watch.
    val items: StateFlow<List<ShoppingItem>> = repository.getShoppingList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList() // Starts empty until MockDb loads
        )

    // 2. "Refactor code to update the item's state in the MockDB"
    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(isChecked = !item.isChecked)
            repository.updateShoppingItem(updatedItem)
        }
    }

    // Add this for your delete functionality if needed
    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }
}