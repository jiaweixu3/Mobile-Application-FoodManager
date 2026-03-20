package com.example.foodmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.domain.model.Household
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
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

    // To know whether we have to initialize the first household when a new user enters the app
    private var isInitialized = false

    init{
        viewModelScope.launch {
            availableHouseholds.collect { households ->
                // If no household, we have to create one
                if (households.isEmpty()){
                    val defaultHousehold = Household(id = "", name = "House 1")
                    settingsRepository.addHousehold(defaultHousehold)

                } else if (!isInitialized){
                    // Selects the first household so the app can load an inventory and shopping list
                    settingsRepository.storeHousehold(households.first())
                    isInitialized = true
                }
            }
        }
    }

    // Updates the system based on the chosen household
    fun onHouseholdChanged(newHousehold: Household){
        // As we had a suspend function, we will use launch to call the function
        viewModelScope.launch {
            settingsRepository.storeHousehold(newHousehold)
        }
    }

    // Adding a new Household
    fun addNewHousehold(name: String){
        viewModelScope.launch {
            // Adding a new randomized id based on the current time
            val new_id =""
            val newHousehold = Household(new_id, name)

            settingsRepository.addHousehold(newHousehold)
        }
    }



    // Updating the name of a household
    fun updateHouseholdName(newName: String){
        val currentHousehold = currentHousehold.value ?: return

        viewModelScope.launch{


            // Updating the household
            settingsRepository.updateHouseholdName(currentHousehold.id, newName)

            // Updating the value
            val updatedHousehold = currentHousehold.copy(name = newName)

            // Store it as active house
            settingsRepository.storeHousehold(updatedHousehold)
        }
    }

    // Generating a code for joining
    fun generateCodeHousehold(){
        val household = currentHousehold.value ?: return
        viewModelScope.launch{
            val newCode = settingsRepository.generateCode(household.id)

            val updatedHousehold = household.copy(joinCode = newCode)

            settingsRepository.storeHousehold(updatedHousehold)
        }
    }

    // Function for actually joining
    fun joinHousehold(code: String){
        viewModelScope.launch {
            settingsRepository.joinHousehold(code)
        }
    }



}