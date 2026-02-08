package com.example.foodmanager.viewmodel

import androidx.lifecycle.ViewModel
import com.example.foodmanager.model.ShoppingItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class ShoppingViewModel : ViewModel() {
    private val _items = MutableStateFlow(
        listOf(
            ShoppingItem(1, "Apples", false),
            ShoppingItem(2, "Bananas", true),
            ShoppingItem(3, "Milk", false),
            ShoppingItem(4, "Eggs", true),
            ShoppingItem(5, "Bread", false)
        )
    )
    val items: StateFlow<List<ShoppingItem>> = _items.asStateFlow()

    fun toggleItem(item: ShoppingItem) {
        _items.update { currentList ->
            currentList.map {
                if (it.id == item.id) {
                    it.copy(isChecked = !it.isChecked)
                } else {
                    it
                }
            }
        }
    }
}