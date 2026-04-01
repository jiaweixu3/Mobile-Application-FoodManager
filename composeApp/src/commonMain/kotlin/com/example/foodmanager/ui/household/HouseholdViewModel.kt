package com.example.foodmanager.ui.household

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.foodmanager.data.repository.SettingsRepository
import com.example.foodmanager.domain.model.HouseholdMember
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HouseholdViewModel(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    private val currentUserIdFlow = MutableStateFlow<String?>(null)

    val currentHousehold = settingsRepository.getCurrentHousehold
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val members: StateFlow<List<HouseholdMember>> = settingsRepository
        .getCurrentHouseholdMembers()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    val currentUserId: StateFlow<String?> = currentUserIdFlow
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), null)

    val currentUserIsOwner: StateFlow<Boolean> = combine(members, currentUserIdFlow) { members, currentUserId ->
        members.any { member -> member.userId == currentUserId && member.role == "Owner" }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), false)

    init {
        viewModelScope.launch {
            currentUserIdFlow.value = settingsRepository.getCurrentUserId()
        }
    }

    fun removeMember(member: HouseholdMember) {
        val memberId = member.id ?: return

        viewModelScope.launch {
            settingsRepository.deleteHouseholdMember(memberId)
        }
    }
}
