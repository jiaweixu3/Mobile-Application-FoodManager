package com.example.foodmanager.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.domain.useCase.ValidateFoodItemUseCase
import com.example.foodmanager.domain.useCase.ValidationResult
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.data.repository.InventoryRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

// Events observed by the UI
sealed class AddItemUiEvent {
    data class ShowError(val message: String) : AddItemUiEvent()
    object NavigateBack : AddItemUiEvent()
}

class AddItemViewModel(
    private val repository: InventoryRepository,
    private val validateFoodItem: ValidateFoodItemUseCase = ValidateFoodItemUseCase()
) : ViewModel() {

    // Stream used to send one-time events to the UI (errors or navigation)
    private val _uiEvent = MutableSharedFlow<AddItemUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    fun saveItem(name: String, quantity: String, category: String, unit: String, expiryDateMs: Long?) {
        // Validating the input
        val validation = validateFoodItem.execute(name, quantity, expiryDateMs)

        // If error, we stop the process
        if (validation is ValidationResult.Error) {
            viewModelScope.launch {
                _uiEvent.emit(AddItemUiEvent.ShowError(validation.message))
            }
            return
        }

        // If success, we create the FoodItem
        val dateString: String = if (expiryDateMs != null) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(expiryDateMs))
        } else {
            ""
        }

        val newItem = FoodItem(
            id = (0..1000000).random(),
            name = name,
            expiryDate = dateString,
            amount = quantity.toDoubleOrNull() ?: 0.0,
            unit = unit,
            category = category
        )

        viewModelScope.launch {
            try {
                // Saving the item to the repository
                repository.addFoodItem(newItem)

                // Going back to the inventory
                _uiEvent.emit(AddItemUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(AddItemUiEvent.ShowError("ERROR while saving the data"))
            }
        }
    }
}