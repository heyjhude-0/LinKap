package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group13.linkap.data.repository.ResidentRepository

class EditResidentViewModelFactory(
    private val repository: ResidentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(EditResidentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return EditResidentViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}