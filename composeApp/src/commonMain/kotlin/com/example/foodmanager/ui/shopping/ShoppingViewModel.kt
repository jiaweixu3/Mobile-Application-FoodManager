package com.example.foodmanager.ui.shopping

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.domain.useCase.MarkAsBoughtUseCase
import com.example.foodmanager.data.repository.ShoppingRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

//  Define the sorting options
enum class SortType {
    NAME, AMOUNT, CATEGORY
}

class ShoppingViewModel(
    private val repository: ShoppingRepository,
    private val markAsBoughtUseCase: MarkAsBoughtUseCase
) : ViewModel() {

    // UI State: Loading
    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // UI State: Error Messages
    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    // UI State: Current Sort Type (Default is by Name)
    private val _sortType = MutableStateFlow(SortType.NAME)
    val sortType: StateFlow<SortType> = _sortType.asStateFlow()

    // Main Data Flow: Combines the database list with the current sort selection
    val items: StateFlow<List<ShoppingItem>> = combine(
        repository.getShoppingList()
            .onStart {
                _isLoading.value = true
                _errorMessage.value = null
            }
            .catch { e ->
                _isLoading.value = false
                _errorMessage.value = e.message ?: "Failed to connect to the database."
            },
        _sortType
    ) { list, currentSort ->
        _isLoading.value = false
        // Apply the selected sorting logic
        when (currentSort) {
            SortType.NAME -> list.sortedBy { it.name.lowercase() }
            SortType.AMOUNT -> list.sortedBy { it.amount }
            SortType.CATEGORY -> list.sortedBy { it.category.lowercase() }
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Function to change the sort type from the UI
    fun setSortType(type: SortType) {
        _sortType.value = type
    }

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

    fun deleteItem(id: String) { // <-- Changed Int to String right here!
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

    fun clearError() {
        _errorMessage.value = null
    }
}