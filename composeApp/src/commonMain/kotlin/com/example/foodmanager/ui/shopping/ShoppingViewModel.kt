package com.example.foodmanager.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.domain.useCase.MarkAsBoughtUseCase
import com.example.foodmanager.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ShoppingViewModel(
    private val repository: ShoppingRepository,
    private val markAsBoughtUseCase: MarkAsBoughtUseCase
) : ViewModel() {

    // 1. UI State: Loading
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    // 2. UI State: Error Messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    // 3. Main Data Flow (Now handles loading and catching database errors)
    val items: StateFlow<List<ShoppingItem>> = repository.getShoppingList()
        .onStart {
            _isLoading.value = true
            _errorMessage.value = null
        }
        .map { list ->
            _isLoading.value = false
            list
        }
        .catch { e ->
            _isLoading.value = false
            _errorMessage.value = e.message ?: "Failed to connect to the database."
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun toggleItem(item: ShoppingItem) {
        viewModelScope.launch {
            try {
                val updatedItem = item.copy(isChecked = !item.isChecked)
                repository.updateShoppingItem(updatedItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to update item."
            }
        }
    }

    fun deleteItem(id: Int) {
        viewModelScope.launch {
            try {
                repository.deleteShoppingItem(id)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to delete item."
            }
        }
    }

    fun addItem(name: String, amount: Double, unit: String, category: String) {
        if (name.isBlank() || amount <= 0.0) return
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val newItem = ShoppingItem(
                    shopping_list_id = "",
                    name = name,
                    amount = amount,
                    unit = unit,
                    category = category,
                    isChecked = false
                )
                repository.addShoppingItem(newItem)
            } catch (e: Exception) {
                _errorMessage.value = "Failed to add new item."
            } finally {
                // Ensure loading always stops, even if it fails
                _isLoading.value = false
            }
        }
    }

    fun markCheckedItemsAsBought() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                val currentList = items.value
                val checkedItems = currentList.filter { it.isChecked }
                checkedItems.forEach { item ->
                    markAsBoughtUseCase(item)
                }
            } catch (e: Exception) {
                _errorMessage.value = "Failed to move items to inventory."
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Call this from the UI (like an alert dialog "OK" button) to dismiss errors
    fun clearError() {
        _errorMessage.value = null
    }
}