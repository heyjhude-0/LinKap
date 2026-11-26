
package com.group13.linkap.ui.screens.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class VisitDetailsViewModel(
    private val visitRepository: VisitRepository
) : ViewModel() {

    private val _residents = MutableStateFlow<List<Resident>>(emptyList())
    val residents: StateFlow<List<Resident>> = _residents.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    fun loadResidents(visitId: String) {
        viewModelScope.launch {
            _isLoading.value = true

            try {
                _residents.value =
                    visitRepository.getResidentsForVisit(visitId)
            } catch (e: Exception) {
                _residents.value = emptyList()
            } finally {
                _isLoading.value = false
            }
        }
    }
}
