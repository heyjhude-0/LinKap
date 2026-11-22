
package com.group13.linkap.ui.screens.households

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.repository.ResidentRepository
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone
import java.util.UUID

class RegisterResidentViewModel(
    private val repository: ResidentRepository
) : ViewModel() {

    fun registerResident(
        householdId: String,
        firstName: String,
        middleName: String,
        lastName: String,
        birthDate: String,
        sex: String,
        relationship: String,
        onResult: (Boolean, String) -> Unit
    ) {
        viewModelScope.launch {
            try {
                val birthDateMillis = if (birthDate.isBlank()) {
                    null
                } else {
                    val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
                    formatter.isLenient = false
                    formatter.timeZone = TimeZone.getTimeZone("UTC")

                    formatter.parse(birthDate)?.time
                        ?: throw IllegalArgumentException("Invalid birth date.")
                }

                val resident = Resident(
                    residentId = UUID.randomUUID().toString(),
                    householdId = householdId,
                    firstName = firstName,
                    middleName = middleName.ifBlank { null },
                    lastName = lastName,
                    birthDate = birthDateMillis,
                    sex = sex,
                    relationshipToHead = relationship,
                    contactNumber = null,
                    isActive = true
                )

                repository.insertResident(resident)
                onResult(true, "Resident registered successfully.")

            } catch (exception: Exception) {
                onResult(
                    false,
                    exception.message ?: "Failed to register resident."
                )
            }
        }
    }
}
