
package com.group13.linkap.ui.screens.followups

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.group13.linkap.data.repository.FollowUpRepository

class FollowUpViewModelFactory(
    private val repository: FollowUpRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(FollowUpViewModel::class.java)) {
            return FollowUpViewModel(repository) as T
        }

        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
