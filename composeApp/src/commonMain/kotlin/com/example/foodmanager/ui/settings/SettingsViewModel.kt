package com.example.foodmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This file creates the view model for settings

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Stores current available households, no need to declare them privately as stateIn handles this
    val availableHouseholds =
        settingsRepository.getHouseholdsList().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected household
    val currentHousehold = settingsRepository.getCurrentHousehold.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // UI State: Feedback message for joining a household
    private val _joinMessage = MutableStateFlow<String?>(null)
    val joinMessage: StateFlow<String?> = _joinMessage.asStateFlow()

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

    // Function for actually joining (Updated to handle success and errors)
    fun joinHousehold(code: String) {
        if (code.isBlank()) {
            _joinMessage.value = "Please enter a valid join code."
            return
        }

        viewModelScope.launch {
            try {
                _joinMessage.value = null // Clear previous messages

                settingsRepository.joinHousehold(code.trim())

                // If it succeeds without throwing an error:
                _joinMessage.value = "Successfully joined the household!"

            } catch (e: Exception) {
                // Determine the exact cause based on the error message
                val errorString = e.message?.lowercase() ?: ""

                when {
                    errorString.contains("already") || errorString.contains("duplicate") -> {
                        _joinMessage.value = "You have already joined this household."
                    }
                    errorString.contains("not found") || errorString.contains("invalid") -> {
                        _joinMessage.value = "Invalid Code. Please check and try again."
                    }
                    else -> {
                        _joinMessage.value = "Failed to join: Invalid Code or Network Error."
                    }
                }
            }
        }
    }

    // Function to clear the message when the user starts typing again
    fun clearJoinMessage() {
        _joinMessage.value = null
    }
}