package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group13.linkap.data.repository.HouseholdRepository

class RegisterHouseholdViewModelFactory(
    private val repository: HouseholdRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterHouseholdViewModel::class.java)) {
            return RegisterHouseholdViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}