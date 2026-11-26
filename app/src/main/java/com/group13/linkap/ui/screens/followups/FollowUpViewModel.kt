
package com.group13.linkap.ui.screens.followups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.local.entity.FollowUp
import com.group13.linkap.data.repository.FollowUpRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class FollowUpViewModel(
    private val repository: FollowUpRepository
) : ViewModel() {

    val followUps = repository.getAllFollowUps()
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5000),
            emptyList()
        )

    fun addFollowUp(followUp: FollowUp) {
        viewModelScope.launch {
            repository.insertFollowUp(followUp)
        }
    }

    fun updateFollowUp(followUp: FollowUp) {
        viewModelScope.launch {
            repository.updateFollowUp(followUp)
        }
    }

    fun deleteFollowUp(followUp: FollowUp) {
        viewModelScope.launch {
            repository.deleteFollowUp(followUp)
        }
    }
}
