package com.group13.linkap.ui.screens.visits

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group13.linkap.data.repository.VisitRepository

class VisitHistoryViewModelFactory(
    private val repository: VisitRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {
        if (modelClass.isAssignableFrom(VisitHistoryViewModel::class.java)) {
            return VisitHistoryViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}