package com.example.foodmanager.ui.additem

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.FavoriteRepository
import com.example.foodmanager.domain.useCase.ValidateFoodItemUseCase
import com.example.foodmanager.domain.useCase.ValidationResult
import com.example.foodmanager.domain.model.FavoriteFoodItem
import com.example.foodmanager.domain.model.FoodItem
import com.example.foodmanager.data.repository.InventoryRepository
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.data.repository.ShoppingRepository
import com.example.foodmanager.domain.normalizeCategory
import com.example.foodmanager.domain.model.ShoppingItem
import com.example.foodmanager.ui.navigation.AddItemDestination
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

// Events observed by the UI
sealed class AddItemUiEvent {
    data class ShowError(val message: String) : AddItemUiEvent()
    object NavigateBack : AddItemUiEvent()
}

class AddItemViewModel(
    private val inventoryRepository: InventoryRepository,
    private val shoppingRepository: ShoppingRepository,
    private val favoriteRepository: FavoriteRepository,
    private val settingsRepository: SettingsRepository,
    private val validateFoodItem: ValidateFoodItemUseCase = ValidateFoodItemUseCase()
) : ViewModel() {

    // Stream used to send one-time events to the UI (errors or navigation)
    private val _uiEvent = MutableSharedFlow<AddItemUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    val favoriteItems: StateFlow<List<FavoriteFoodItem>> = favoriteRepository
        .getFavoriteItems()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun saveItem(
        name: String,
        quantity: String,
        category: String,
        unit: String,
        expiryDateMs: Long?,
        destination: AddItemDestination,
        saveAsFavorite: Boolean
    ) {
        // Validating the input
        val validation = validateFoodItem.execute(
            name = name,
            quantityStr = quantity,
            expiryDateMs = expiryDateMs,
            requireExpiryDate = destination == AddItemDestination.Inventory
        )

        // If error, we stop the process
        if (validation is ValidationResult.Error) {
            viewModelScope.launch {
                _uiEvent.emit(AddItemUiEvent.ShowError(validation.message))
            }
            return
        }

        val normalizedName = name.trim()
        val normalizedAmount = quantity.toDoubleOrNull() ?: 0.0
        val normalizedCategory = normalizeCategory(category)

        val dateString: String = if (expiryDateMs != null) {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                .format(Date(expiryDateMs))
        } else {
            ""
        }

        viewModelScope.launch {
            try {
                if (saveAsFavorite) {
                    favoriteRepository.addFavoriteItem(
                        FavoriteFoodItem(
                            id = null,
                            householdId = settingsRepository.getCurrentHouseholdValue()?.id,
                            name = normalizedName,
                            amount = normalizedAmount,
                            unit = unit,
                            category = normalizedCategory
                        )
                    )
                }

                when (destination) {
                    AddItemDestination.Inventory -> {
                        inventoryRepository.addFoodItem(
                            FoodItem(
                                id = Random.nextInt(0, 1_000_000),
                                inventoryId = null,
                                name = normalizedName,
                                expiryDate = dateString,
                                amount = normalizedAmount,
                                unit = unit,
                                category = normalizedCategory
                            )
                        )
                    }

                    AddItemDestination.ShoppingList -> {
                        shoppingRepository.addShoppingItem(
                            ShoppingItem(
                                shopping_list_id = "",
                                name = normalizedName,
                                amount = normalizedAmount,
                                unit = unit,
                                category = normalizedCategory
                            )
                        )
                    }
                }

                _uiEvent.emit(AddItemUiEvent.NavigateBack)
            } catch (e: Exception) {
                _uiEvent.emit(AddItemUiEvent.ShowError("ERROR while saving the data"))
            }
        }
    }
}
