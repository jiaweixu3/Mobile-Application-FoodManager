package com.example.foodmanager.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.domain.useCase.MarkAsBoughtUseCase
import com.example.foodmanager.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val repository: ShoppingRepository,
    private val markAsBoughtUseCase: MarkAsBoughtUseCase
) : ViewModel() {

    val items: StateFlow<List<ShoppingItem>> = repository.getShoppingList()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            val updatedItem = item.copy(isChecked = !item.isChecked)
            repository.updateShoppingItem(updatedItem)
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            repository.deleteShoppingItem(id)
        }
    }

    fun addItem(name: String, amount: Double, unit: String, category: String) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            val newItem = ShoppingItem(
                shopping_list_id = "",
                name = name,
                amount = amount,
                unit = unit,
                category = category,
                isChecked = false
            )
            repository.addShoppingItem(newItem)
        }
    }

    fun markCheckedItemsAsBought() {
        viewModelScope.launch {
            val currentList = items.value
            val checkedItems = currentList.filter { it.isChecked }
            checkedItems.forEach { item ->
                markAsBoughtUseCase(item)
            }
        }
    }
}