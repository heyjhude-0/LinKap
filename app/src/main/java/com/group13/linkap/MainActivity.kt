
package com.group13.linkap

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.group13.linkap.data.repository.AuthRepository
import com.group13.linkap.ui.screens.home.HomeScreen
import com.group13.linkap.ui.screens.login.LoginScreen
import com.group13.linkap.ui.theme.LinKapTheme

enum class AppStartState {
    CHECKING,
    LOGIN,
    HOME,
    ERROR
}

class MainActivity : ComponentActivity() {

    private lateinit var authRepository: AuthRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        authRepository = AuthRepository(applicationContext)

        setContent {
            LinKapTheme {

                var startState by remember {
                    mutableStateOf(AppStartState.CHECKING)
                }

                var sessionError by remember {
                    mutableStateOf<String?>(null)
                }

                fun validateSession() {
                    startState = AppStartState.CHECKING
                    sessionError = null

                    authRepository.validateCurrentSession { result ->
                        result.onSuccess { user ->
                            startState = if (user != null) {
                                AppStartState.HOME
                            } else {
                                AppStartState.LOGIN
                            }
                        }

                        result.onFailure { exception ->
                            sessionError = exception.localizedMessage
                                ?: "Unable to verify your session. Check your internet connection."

                            startState = AppStartState.ERROR
                        }
                    }
                }

                LaunchedEffect(Unit) {
                    validateSession()
                }

                when (startState) {

                    AppStartState.CHECKING -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            CircularProgressIndicator()
                            Text("Verifying your account...")
                        }
                    }

                    AppStartState.LOGIN -> {
                        LoginScreen(
                            onLoginSuccess = {
                                startState = AppStartState.HOME
                            }
                        )
                    }

                    AppStartState.HOME -> {
                        HomeScreen(
                            onLogout = {
                                authRepository.signOut()
                                startState = AppStartState.LOGIN
                            }
                        )
                    }

                    AppStartState.ERROR -> {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = sessionError
                                    ?: "Session verification failed.",
                                style = MaterialTheme.typography.bodyLarge
                            )

                            Button(
                                onClick = { validateSession() }
                            ) {
                                Text("Retry")
                            }
                        }
                    }
                }
            }
        }
    }
}
