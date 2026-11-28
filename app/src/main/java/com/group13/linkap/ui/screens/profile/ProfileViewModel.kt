
package com.group13.linkap.ui.screens.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.group13.linkap.data.model.UserProfile
import com.group13.linkap.data.repository.AuthRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class ProfileViewModel(
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _profile = MutableStateFlow<UserProfile?>(null)
    val profile: StateFlow<UserProfile?> = _profile.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun loadProfile() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null

            authRepository.getCurrentUserProfile { result ->
                result.onSuccess { userProfile ->
                    _profile.value = userProfile
                }.onFailure { exception ->
                    _errorMessage.value =
                        exception.localizedMessage ?: "Unable to load profile."
                }

                _isLoading.value = false
            }
        }
    }

    fun updateFullName(newFullName: String, onResult: (Boolean, String?) -> Unit) {
        val trimmedName = newFullName.trim()

        if (trimmedName.isBlank()) {
            onResult(false, "Full name cannot be empty.")
            return
        }

        if (trimmedName.length < 2) {
            onResult(false, "Full name must contain at least 2 characters.")
            return
        }

        authRepository.updateFullName(trimmedName) { result ->
            result.onSuccess {
                loadProfile()
                onResult(true, null)
            }.onFailure { exception ->
                onResult(false, exception.localizedMessage ?: "Unable to update profile.")
            }
        }
    }


}
