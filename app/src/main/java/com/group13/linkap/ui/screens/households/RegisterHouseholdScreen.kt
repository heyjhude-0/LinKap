package com.group13.linkap.ui.screens.households

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark

@Composable
fun RegisterHouseholdScreen(
    onBackClick: () -> Unit,
    onContinueClick: (String, String, String, String) -> Unit,
    isLoading: Boolean = false,
    resultMessage: String? = null,
    isSuccess: Boolean = false,
    onResetForm: () -> Unit,
) {
    var householdCode by remember { mutableStateOf("") }
    var purok by remember { mutableStateOf("") }
    var street by remember { mutableStateOf("") }
    var barangay by remember { mutableStateOf("Alangilan") }

    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val onPrimary = if (dark) BackgroundDark else Color.White
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)

    val focusManager = LocalFocusManager.current

    val fieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = textPrimary,
        unfocusedTextColor = textPrimary,
        focusedContainerColor = surface,
        unfocusedContainerColor = surface,
        focusedBorderColor = primary,
        unfocusedBorderColor = border,
        focusedLabelColor = primary,
        unfocusedLabelColor = textSecondary,
        cursorColor = primary,
        focusedPlaceholderColor = textSecondary,
        unfocusedPlaceholderColor = textSecondary
    )

    val fieldShape = RoundedCornerShape(12.dp)
    val nextAction = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) }
    )
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Household Information",
                style = MaterialTheme.typography.titleLarge,
                color = textPrimary
            )
            Text(
                text = "Enter the household details to register it.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
        }

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = surface,
            tonalElevation = 1.dp
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = householdCode,
                    onValueChange = { householdCode = it },
                    label = { Text("Household Code") },
                    placeholder = { Text("e.g. HH-001") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Characters,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = nextAction
                )

                OutlinedTextField(
                    value = purok,
                    onValueChange = { purok = it },
                    label = { Text("Purok") },
                    placeholder = { Text("e.g. Purok 1") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = nextAction
                )

                OutlinedTextField(
                    value = street,
                    onValueChange = { street = it },
                    label = { Text("Street / Sitio (Optional)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = nextAction
                )

                OutlinedTextField(
                    value = barangay,
                    onValueChange = { barangay = it },
                    label = { Text("Barangay") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = { focusManager.clearFocus() }
                    )
                )
            }
        }

        Button(
            onClick = {
                errorMessage = when {
                    householdCode.isBlank() ->
                        "Please enter a household code."

                    purok.isBlank() ->
                        "Please enter the purok."

                    barangay.isBlank() ->
                        "Please enter the barangay."

                    else -> {
                        onContinueClick(
                            householdCode.trim(),
                            purok.trim(),
                            street.trim(),
                            barangay.trim()
                        )
                        null
                    }
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = !isLoading
        ) {
            Text(
                if (isLoading) "Registering..." else "Continue"
            )
        }
        if (isLoading) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )
        }

        resultMessage?.let { message ->
            Text(
                text = message,
                color = if (isSuccess) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
                style = MaterialTheme.typography.bodyMedium
            )
        }
        if (isSuccess) {
            OutlinedButton(
                onClick = {
                    householdCode = ""
                    purok = ""
                    street = ""
                    barangay = "Alangilan"
                    errorMessage = null
                    onResetForm()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register Another Household")
            }
        }
        errorMessage?.let { message ->
            Text(
                text = message,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}