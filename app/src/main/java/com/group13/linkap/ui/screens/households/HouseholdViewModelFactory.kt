package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group13.linkap.data.repository.HouseholdRepository
import com.group13.linkap.data.repository.ResidentRepository

class HouseholdViewModelFactory(
    private val repository: HouseholdRepository,
    private val residentRepository: ResidentRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(HouseholdViewModel::class.java)) {
            return HouseholdViewModel(
                repository,
                residentRepository
            ) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}