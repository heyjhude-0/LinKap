
package com.group13.linkap.ui.screens.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.data.local.entity.VisitResident
import com.group13.linkap.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class FieldVisitViewModel(
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving = _isSaving.asStateFlow()

    private val _saveMessage = MutableStateFlow<String?>(null)
    val saveMessage = _saveMessage.asStateFlow()

    fun saveVisit(
        householdId: String,
        residentIds: Set<String>,
        category: String,
        notes: String,
        visitDate: Long,
        latitude: Double? = null,
        longitude: Double? = null
    ) {
        if (residentIds.isEmpty()) {
            _saveMessage.value = "Please select at least one resident."
            return
        }

        viewModelScope.launch {
            _isSaving.value = true
            _saveMessage.value = null

            try {
                val currentTime = System.currentTimeMillis()
                val visitId = UUID.randomUUID().toString()

                val visit = Visit(
                    visitId = visitId,
                    householdId = householdId,
                    visitDate = visitDate,
                    visitCategory = category,
                    notes = notes.trim().ifBlank { null },
                    latitude = latitude,
                    longitude = longitude,
                    syncStatus = "PENDING",
                    createdAt = currentTime,
                    updatedAt = currentTime
                )

                val visitResidents = residentIds.map { residentId ->
                    VisitResident(
                        visitId = visitId,
                        residentId = residentId
                    )
                }
                visitRepository.insertVisitWithResidents(
                    visit,
                    visitResidents
                )

                _saveMessage.value = "Field visit saved successfully!"
            } catch (e: Exception) {
                _saveMessage.value =
                    "Failed to save field visit: ${e.message}"
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun clearSaveMessage() {
        _saveMessage.value = null
    }
}
