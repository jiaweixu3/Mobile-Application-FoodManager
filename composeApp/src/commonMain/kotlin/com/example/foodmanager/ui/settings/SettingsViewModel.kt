package com.example.foodmanager.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.HouseholdMember
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.domain.model.Household
import com.example.foodmanager.data.supabase
import io.github.jan.supabase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    val availableHouseholds =
        settingsRepository.getHouseholdsList().stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val currentHousehold = settingsRepository.getCurrentHousehold.stateIn(viewModelScope, SharingStarted.Lazily, null)

    // --- Member State ---
    private val _members = MutableStateFlow<List<HouseholdMember>>(emptyList())
    val members: StateFlow<List<HouseholdMember>> = _members.asStateFlow()

    private val _isLoadingMembers = MutableStateFlow(false)
    val isLoadingMembers: StateFlow<Boolean> = _isLoadingMembers.asStateFlow()
    // -------------------------

    private val _joinMessage = MutableStateFlow<String?>(null)
    val joinMessage: StateFlow<String?> = _joinMessage.asStateFlow()

    // Account state
    private val _userEmail = MutableStateFlow<String>("")
    val userEmail: StateFlow<String> = _userEmail.asStateFlow()

    private val _passwordMessage = MutableStateFlow<String?>(null)
    val passwordMessage: StateFlow<String?> = _passwordMessage.asStateFlow()

    private var isInitialized = false

    init {
        // Fetch the user's email when the ViewModel starts
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

        // --- Automatically listen for household changes and fetch members! ---
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

    // --- Fetch Members Function (Internal) ---
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

    // --- NEW PUBLIC FUNCTION: Called by the UI when the screen opens ---
    fun getHouseholdMembers() {
        val currentId = currentHousehold.value?.id
        if (currentId != null) {
            fetchMembers(currentId)
        }
    }

    // Change password function
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
            try {
                _joinMessage.value = null
                settingsRepository.joinHousehold(code.trim())

                // Refresh members immediately after successfully joining!
                currentHousehold.value?.id?.let { fetchMembers(it) }

                _joinMessage.value = "Successfully joined the household!"
            } catch (e: Exception) {
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

    fun clearJoinMessage() {
        _joinMessage.value = null
    }
}