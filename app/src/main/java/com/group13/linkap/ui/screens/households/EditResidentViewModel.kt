package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.repository.ResidentRepository
import kotlinx.coroutines.launch

class EditResidentViewModel(
    private val repository: ResidentRepository
) : ViewModel() {

    fun updateResident(
        resident: Resident,
        firstName: String,
        middleName: String,
        lastName: String,
        birthDate: Long?,
        sex: String,
        relationship: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val updatedResident = resident.copy(
                    firstName = firstName,
                    middleName = middleName.ifBlank { null },
                    lastName = lastName,
                    birthDate = birthDate,
                    sex = sex,
                    relationshipToHead = relationship
                )

                repository.updateResident(updatedResident)

                onResult(true, "Resident updated successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Failed to update resident."
                )
            }
        }
    }
}