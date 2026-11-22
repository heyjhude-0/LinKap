package com.group13.linkap.ui.screens.login
import com.group13.linkap.data.repository.AuthRepository

import android.util.Patterns
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: String? = null,
    val passwordError: String? = null,
    val isPasswordVisible: Boolean = false,
    val isLoading: Boolean = false,
    val loginError: String? = null,
    val hasSubmitted: Boolean = false,
    val isLoggedIn: Boolean = false,
    val isResetLoading: Boolean = false,
    val resetMessage: String? = null,
    val resetError: String? = null,
    val showForgotPasswordDialog: Boolean = false
)

/** Returns an error message, or null if the email is valid. */
fun validateEmail(email: String): String? = when {
    email.isBlank() -> "Enter your email address"
    !Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches() -> "Enter a valid email address"
    else -> null
}

/** Returns an error message, or null if the password is valid. */
fun validatePassword(password: String): String? = when {
    password.isEmpty() -> "Enter your password"
    password.length < 6 -> "Password must be at least 6 characters"
    else -> null
}


class LoginViewModel(
    private val authRepository: AuthRepository = AuthRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update {
            it.copy(
                email = value,
                emailError = if (it.hasSubmitted) validateEmail(value) else null,
                loginError = null
            )
        }
    }

    fun onPasswordChange(value: String) {
        _uiState.update {
            it.copy(
                password = value,
                passwordError = if (it.hasSubmitted) validatePassword(value) else null,
                loginError = null
            )
        }
    }

    fun onTogglePasswordVisibility() {
        _uiState.update {
            it.copy(isPasswordVisible = !it.isPasswordVisible)
        }
    }

    fun onLoginClick() {
        val current = _uiState.value

        if (current.isLoading) return

        val emailError = validateEmail(current.email)
        val passwordError = validatePassword(current.password)

        _uiState.update {
            it.copy(
                emailError = emailError,
                passwordError = passwordError,
                hasSubmitted = true,
                loginError = null
            )
        }

        if (emailError != null || passwordError != null) return

        _uiState.update { it.copy(isLoading = true) }

        authRepository.signIn(
            email = current.email,
            password = current.password
        ) { result ->

            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        isLoggedIn = true,
                        loginError = null
                    )
                }
            }

            result.onFailure { exception ->
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        loginError = when {
                            exception.message?.contains(
                                "network", ignoreCase = true
                            ) == true ->
                                "Please check your internet connection."

                            exception.message?.contains(
                                "not authorized", ignoreCase = true
                            ) == true ->
                                "Your account is not authorized for LinKap."

                            exception.message?.contains(
                                "does not have BHW access", ignoreCase = true
                            ) == true ->
                                "This account does not have BHW access."

                            exception.message?.contains(
                                "deactivated", ignoreCase = true
                            ) == true ->
                                "Your account has been deactivated. Contact your administrator."

                            else ->
                                "Invalid email or password."
                        }
                    )
                }
            }
        }
    }

    fun onLoginHandled() {
        _uiState.update { it.copy(isLoggedIn = false) }
    }

    fun onForgotPasswordClick() {
        _uiState.update {
            it.copy(
                showForgotPasswordDialog = true,
                resetMessage = null,
                resetError = null
            )
        }
    }

    fun onDismissForgotPasswordDialog() {
        _uiState.update {
            it.copy(showForgotPasswordDialog = false)
        }
    }

    fun onSendResetLink() {
        val email = _uiState.value.email.trim()
        val emailError = validateEmail(email)

        if (emailError != null) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    hasSubmitted = true
                )
            }
            return
        }

        if (_uiState.value.isResetLoading) return

        _uiState.update {
            it.copy(
                isResetLoading = true,
                resetMessage = null,
                resetError = null
            )
        }

        authRepository.sendPasswordResetEmail(email) { result ->
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isResetLoading = false,
                        showForgotPasswordDialog = false,
                        resetMessage = "Password reset link sent. Check your email.",
                        resetError = null
                    )
                }
            }

            result.onFailure {
                _uiState.update {
                    it.copy(
                        isResetLoading = false,
                        resetError = "Unable to send reset email. Please try again."
                    )
                }
            }
        }
    }
}
