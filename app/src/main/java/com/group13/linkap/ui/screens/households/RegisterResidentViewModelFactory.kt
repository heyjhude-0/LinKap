
package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group13.linkap.data.repository.ResidentRepository

class RegisterResidentViewModelFactory(
    private val repository: ResidentRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(RegisterResidentViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return RegisterResidentViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
