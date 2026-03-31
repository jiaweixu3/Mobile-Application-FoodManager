package com.example.foodmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.HouseholdJoinResult
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This file creates the view model for settings

sealed class SettingsUiEvent {
    data class ShowMessage(val message: String) : SettingsUiEvent()
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiEvent = MutableSharedFlow<SettingsUiEvent>()
    val uiEvent = _uiEvent.asSharedFlow()

    // Stores current available households, no need to declare them privately as stateIn handles this
    val availableHouseholds =
        settingsRepository.getHouseholdsList().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected household
    val currentHousehold = settingsRepository.getCurrentHousehold.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // To know whether we have to initialize the first household when a new user enters the app
    private var isInitialized = false

    init {
        viewModelScope.launch {
            settingsRepository.getHouseholdsList().collect { households ->
                // Only if it has not initialized
                if (!isInitialized) {

                    // If no household, we have to create one
                    if (households.isEmpty()) {
                        val defaultHousehold = Household(id = "", name = "House 1")
                        settingsRepository.addHousehold(defaultHousehold)
                        isInitialized = true
                    } else if (!isInitialized) {
                        // Selects the first household so the app can load an inventory and shopping list
                        settingsRepository.storeHousehold(households.first())
                        isInitialized = true
                    }
                }
            }
        }
    }

    // Updates the system based on the chosen household
    fun onHouseholdChanged(newHousehold: Household) {
        // As we had a suspend function, we will use launch to call the function
        viewModelScope.launch {
            settingsRepository.storeHousehold(newHousehold)
        }
    }

    // Adding a new Household
    fun addNewHousehold(name: String) {
        viewModelScope.launch {
            val newHousehold = Household("", name)
            settingsRepository.addHousehold(newHousehold)
        }
    }


    // Updating the name of a household
    fun updateHouseholdName(newName: String) {
        val currentHousehold = currentHousehold.value ?: return

        viewModelScope.launch {
            // Updating the household
            settingsRepository.updateHouseholdName(currentHousehold.id, newName)
        }
    }

    // Generating a code for joining
    fun generateCodeHousehold() {
        val household = currentHousehold.value ?: return
        viewModelScope.launch {
            settingsRepository.generateCode(household.id)
        }
    }

    // Function for actually joining
    fun joinHousehold(code: String) {
        viewModelScope.launch {
            val normalizedCode = code.trim().uppercase()
            val existingHousehold = availableHouseholds.value.firstOrNull {
                it.joinCode?.equals(normalizedCode, ignoreCase = true) == true
            }

            if (existingHousehold != null) {
                settingsRepository.storeHousehold(existingHousehold)
                _uiEvent.emit(
                    SettingsUiEvent.ShowMessage("You are already in household '${existingHousehold.name}'.")
                )
                return@launch
            }

            when (val result = settingsRepository.joinHousehold(code)) {
                is HouseholdJoinResult.Success -> {
                    _uiEvent.emit(SettingsUiEvent.ShowMessage("Joined '${result.household.name}' successfully."))
                }
                is HouseholdJoinResult.Error -> {
                    _uiEvent.emit(SettingsUiEvent.ShowMessage(result.message))
                }
            }
        }
    }


}
