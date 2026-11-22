package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.repository.HouseholdRepository
import kotlinx.coroutines.launch
import java.util.UUID

class RegisterHouseholdViewModel(
    private val repository: HouseholdRepository
) : ViewModel() {

    fun registerHousehold(
        householdCode: String,
        purok: String,
        street: String,
        barangay: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val currentTime = System.currentTimeMillis()
                val isDuplicate = repository.isHouseholdCodeTaken(householdCode)

                if (isDuplicate) {
                    onResult(false, "This household code is already registered.")
                    return@launch
                }
                val household = Household(
                    householdId = UUID.randomUUID().toString(),
                    householdCode = householdCode,
                    purok = purok,
                    street = street.ifBlank { null },
                    barangay = barangay,
                    latitude = null,
                    longitude = null,
                    createdAt = currentTime,
                    updatedAt = currentTime
                )

                repository.insertHousehold(household)

                onResult(true, "Household registered successfully.")
            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.localizedMessage ?: "Unable to register household."
                )
            }
        }
    }
}