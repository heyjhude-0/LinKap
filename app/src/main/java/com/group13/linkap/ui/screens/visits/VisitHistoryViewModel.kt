package com.group13.linkap.ui.screens.visits

import androidx.lifecycle.ViewModel
import com.group13.linkap.data.repository.VisitRepository

class VisitHistoryViewModel(
    repository: VisitRepository
) : ViewModel() {

    val visits = repository.getAllVisits()
}