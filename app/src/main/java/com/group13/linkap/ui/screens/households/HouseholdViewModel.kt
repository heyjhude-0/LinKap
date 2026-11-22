package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.repository.HouseholdRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import com.group13.linkap.data.repository.ResidentRepository
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted

class HouseholdViewModel(
    private val repository: HouseholdRepository,
    private val residentRepository: ResidentRepository
) : ViewModel() {

    private val _selectedHousehold = MutableStateFlow<Household?>(null)
    val selectedHousehold: StateFlow<Household?> =
        _selectedHousehold.asStateFlow()

    val selectedResidents = selectedHousehold
        .flatMapLatest { household ->
            if (household != null) {
                residentRepository.getResidentsByHousehold(
                    household.householdId
                )
            } else {
                flowOf(emptyList())
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
    val households = repository.getAllHouseholds()
    fun loadHousehold(householdId: String) {
        viewModelScope.launch {
            repository.getHouseholdById(householdId).collect { household ->
                _selectedHousehold.value = household
            }
        }
    }
}
