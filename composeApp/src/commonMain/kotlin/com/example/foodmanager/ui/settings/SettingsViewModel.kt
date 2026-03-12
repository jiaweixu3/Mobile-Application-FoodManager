package com.example.foodmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// This file creates the view model for settings

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    // Stores current available households, no need to declare them privately as stateIn handles this
    val availableHouseholds = settingsRepository.getHouseholdsList().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    // Selected household
    val currentHousehold = settingsRepository.getCurrentHousehold.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // Updates the system based on the chosen household
    fun onHouseholdChanged(newHousehold: String){
        // As we had a suspend function, we will use launch to call the function
        viewModelScope.launch {
            settingsRepository.storeHousehold(newHousehold)
        }
    }


}