package com.example.foodmanager.ui.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.domain.model.HouseholdMember
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HouseholdViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val currentHousehold = settingsRepository.getCurrentHousehold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val members: StateFlow<List<HouseholdMember>> = settingsRepository
        .getCurrentHouseholdMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun removeMember(member: HouseholdMember) {
        val memberId = member.id ?: return

        viewModelScope.launch {
            settingsRepository.deleteHouseholdMember(memberId)
        }
    }
}
