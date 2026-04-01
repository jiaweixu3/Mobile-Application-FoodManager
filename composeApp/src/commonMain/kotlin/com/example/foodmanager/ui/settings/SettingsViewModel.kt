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

    val currentHousehold = settingsRepository.getCurrentHousehold.stateIn(viewModelScope, SharingStarted.Lazily, null)

    private val _members = MutableStateFlow<List<HouseholdMember>>(emptyList())
    val members: StateFlow<List<HouseholdMember>> = _members.asStateFlow()

    private val _isLoadingMembers = MutableStateFlow(false)
    val isLoadingMembers: StateFlow<Boolean> = _isLoadingMembers.asStateFlow()

    private val _joinMessage = MutableStateFlow<String?>(null)
    val joinMessage: StateFlow<String?> = _joinMessage.asStateFlow()

    private val _userEmail = MutableStateFlow<String>("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _passwordMessage = MutableStateFlow<String?>(null)
    val passwordMessage: StateFlow<String?> = _passwordMessage.asStateFlow()

    private var isInitialized = false

    init {
        _userEmail.value = supabase.auth.currentUserOrNull()?.email ?: "Unknown Email"

        viewModelScope.launch {
            settingsRepository.getHouseholdsList().collect { households ->
                if (!isInitialized) {
                    if (households.isEmpty()) {
                        val defaultHousehold = Household(id = "", name = "House 1")
                        settingsRepository.addHousehold(defaultHousehold)
                        isInitialized = true
                    } else if (!isInitialized) {
                        settingsRepository.storeHousehold(households.first())
                        isInitialized = true
                    }
                }
            }
        }

        viewModelScope.launch {
            currentHousehold.collectLatest { household ->
                if (household != null) {
                    fetchMembers(household.id)
                } else {
                    _members.value = emptyList()
                }
            }
        }
    }

    private fun fetchMembers(householdId: String) {
        viewModelScope.launch {
            _isLoadingMembers.value = true
            try {
                val fetchedMembers = settingsRepository.getHouseholdMembers(householdId)
                _members.value = fetchedMembers
            } catch (e: Exception) {
                println("ViewModel Error fetching members: ${e.message}")
                _members.value = emptyList()
            } finally {
                _isLoadingMembers.value = false
            }
        }
    }

    fun getHouseholdMembers() {
        val currentId = currentHousehold.value?.id
        if (currentId != null) {
            fetchMembers(currentId)
        }
    }

    // Removes a member from the household and refreshes the local member list
    fun removeMember(memberId: String) {
        viewModelScope.launch {
            try {
                settingsRepository.removeMember(memberId)
                currentHousehold.value?.id?.let { fetchMembers(it) }
            } catch (e: Exception) {
                println("Error removing member: ${e.message}")
            }
        }
    }

    fun changePassword(newPassword: String) {
        if (newPassword.length < 6) {
            _passwordMessage.value = "Password must be at least 6 characters."
            return
        }

        viewModelScope.launch {
            try {
                _passwordMessage.value = null
                supabase.auth.updateUser {
                    password = newPassword
                }
                _passwordMessage.value = "Successfully updated password!"
            } catch (e: Exception) {
                _passwordMessage.value = "Failed to update: ${e.message}"
            }
        }
    }

    fun clearPasswordMessage() {
        _passwordMessage.value = null
    }

    fun onHouseholdChanged(newHousehold: Household) {
        viewModelScope.launch { settingsRepository.storeHousehold(newHousehold) }
    }

    fun addNewHousehold(name: String) {
        viewModelScope.launch {
            val newHousehold = Household("", name)
            settingsRepository.addHousehold(newHousehold)
        }
    }

    fun updateHouseholdName(newName: String) {
        val currentHousehold = currentHousehold.value ?: return
        viewModelScope.launch {
            settingsRepository.updateHouseholdName(currentHousehold.id, newName)
        }
    }

    fun generateCodeHousehold() {
        val household = currentHousehold.value ?: return
        viewModelScope.launch { settingsRepository.generateCode(household.id) }
    }

    fun joinHousehold(code: String) {
        if (code.isBlank()) {
            _joinMessage.value = "Please enter a valid join code."
            return
        }

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