package com.group13.linkap.ui.screens.login

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group13.linkap.R
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.BackgroundLight
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenSecondary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.Nunito
import com.group13.linkap.ui.theme.StatusError
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark
import com.group13.linkap.ui.theme.Typography
import androidx.compose.material3.AlertDialog
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Row

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val state by viewModel.uiState.collectAsState()

    LaunchedEffect(state.isLoggedIn) {
        if (state.isLoggedIn) {
            viewModel.onLoginHandled()
            onLoginSuccess()
        }
    }

    LoginContent(
        state = state,
        onEmailChange = viewModel::onEmailChange,
        onPasswordChange = viewModel::onPasswordChange,
        onTogglePasswordVisibility = viewModel::onTogglePasswordVisibility,
        onLoginClick = viewModel::onLoginClick,
        onForgotPasswordClick = viewModel::onForgotPasswordClick,
        onDismissForgotPasswordDialog = viewModel::onDismissForgotPasswordDialog,
        onSendResetLink = viewModel::onSendResetLink
    )
}

@Composable
fun LoginContent(
    state: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onTogglePasswordVisibility: () -> Unit,
    onLoginClick: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onDismissForgotPasswordDialog: () -> Unit,
    onSendResetLink: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val background = if (dark) BackgroundDark else BackgroundLight
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    // Text on the primary button: dark text on the light dark-mode green, white on the teal
    val onPrimary = if (dark) BackgroundDark else Color.White
    val logoTextStyle = TextStyle(
        fontFamily = Nunito,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 34.sp
    )
    val focusManager = LocalFocusManager.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        focusedBorderColor = primary,
        unfocusedBorderColor = textSecondary,
        focusedLabelColor = primary,
        unfocusedLabelColor = textSecondary,
        cursorColor = primary,
        errorBorderColor = StatusError,
        errorLabelColor = StatusError,
        errorSupportingTextColor = StatusError,
        errorCursorColor = StatusError
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .systemBarsPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Image(
            painter = painterResource(R.drawable.linkap_logo),
            contentDescription = "LinKap logo",
            modifier = Modifier.size(96.dp)
        )

        Spacer(Modifier.height(12.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Lin",
                style = logoTextStyle,
                color = GreenPrimary
            )

            Text(
                text = "Kap",
                style = logoTextStyle,
                color = GreenSecondary
            )
        }

        Spacer(Modifier.height(4.dp))

        Text(
            text = "Offline-first health visit records",
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "Log in",
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimary
                )

                Spacer(Modifier.height(8.dp))

                OutlinedTextField(
                    value = state.email,
                    onValueChange = onEmailChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Email") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = state.emailError != null,
                    supportingText = state.emailError?.let { { Text(it) } },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Email,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = KeyboardActions(
                        onNext = { focusManager.moveFocus(FocusDirection.Down) }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                OutlinedTextField(
                    value = state.password,
                    onValueChange = onPasswordChange,
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Password") },
                    singleLine = true,
                    enabled = !state.isLoading,
                    isError = state.passwordError != null,
                    supportingText = state.passwordError?.let { { Text(it) } },
                    visualTransformation = if (state.isPasswordVisible) {
                        VisualTransformation.None
                    } else {
                        PasswordVisualTransformation()
                    },
                    trailingIcon = {
                        TextButton(onClick = onTogglePasswordVisibility) {
                            Text(
                                text = if (state.isPasswordVisible) "Hide" else "Show",
                                color = primary,
                                style = MaterialTheme.typography.labelLarge
                            )
                        }
                    },
                    keyboardOptions = KeyboardOptions(
                        keyboardType = KeyboardType.Password,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            focusManager.clearFocus()
                            onLoginClick()
                        }
                    ),
                    shape = RoundedCornerShape(12.dp),
                    colors = fieldColors
                )

                TextButton(
                    onClick = onForgotPasswordClick,
                    enabled = !state.isLoading && !state.isResetLoading,
                    modifier = Modifier.align(Alignment.End)
                ) {
                    if (state.isResetLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = primary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Forgot Password?",
                            color = primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }

                if (state.loginError != null) {
                    Text(
                        text = state.loginError,
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusError
                    )
                }

                state.resetMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodySmall,
                        color = primary
                    )
                }

                state.resetError?.let { error ->
                    Text(
                        text = error,
                        style = MaterialTheme.typography.bodySmall,
                        color = StatusError
                    )
                }

                Spacer(Modifier.height(8.dp))

                Button(
                    onClick = {
                        focusManager.clearFocus()
                        onLoginClick()
                    },
                    enabled = !state.isLoading,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = primary,
                        contentColor = onPrimary,
                        disabledContainerColor = primary.copy(alpha = 0.6f),
                        disabledContentColor = onPrimary
                    )
                ) {
                    if (state.isLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(22.dp),
                            color = onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text(
                            text = "Log in",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                        )
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
    if (state.showForgotPasswordDialog) {
        AlertDialog(
            onDismissRequest = {
                if (!state.isResetLoading) {
                    onDismissForgotPasswordDialog()
                }
            },
            containerColor = background,
            title = {
                Text(
                    text = "Reset Password",
                    style = TextStyle(
                        fontFamily = Nunito,
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp
                    ),
                    color = primary
                )

            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        "Enter your registered email address. " +
                                "We'll send you a link to reset your password."
                    )

                    OutlinedTextField(
                        value = state.email,
                        onValueChange = onEmailChange,
                        label = { Text("Email Address") },
                        singleLine = true,
                        isError = state.emailError != null,
                        modifier = Modifier.fillMaxWidth(),
                        colors = fieldColors
                    )

                    state.emailError?.let {
                        Text(
                            text = it,
                            color = StatusError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }

                    state.resetError?.let {
                        Text(
                            text = it,
                            color = StatusError,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = onSendResetLink,
                    enabled = !state.isResetLoading
                ) {
                    if (state.isResetLoading) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Send Reset Link")
                    }
                }
            },
            dismissButton = {
                TextButton(
                    onClick = onDismissForgotPasswordDialog,
                    enabled = !state.isResetLoading
                ) {
                    Text("Cancel")
                }
            }
        )
    }

}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginPreviewEmpty() {
    MaterialTheme(typography = Typography) {
        LoginContent(
            state = LoginUiState(),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onDismissForgotPasswordDialog = {},
            onSendResetLink = {}

        )
    }
}

@Preview(showBackground = true, heightDp = 800)
@Composable
private fun LoginPreviewErrors() {
    MaterialTheme(typography = Typography) {
        LoginContent(
            state = LoginUiState(
                email = "ana@",
                password = "123",
                emailError = "Enter a valid email address",
                passwordError = "Password must be at least 6 characters",
                hasSubmitted = true
            ),
            onEmailChange = {},
            onPasswordChange = {},
            onTogglePasswordVisibility = {},
            onLoginClick = {},
            onForgotPasswordClick = {},
            onDismissForgotPasswordDialog = {},
            onSendResetLink = {}
        )
    }
}