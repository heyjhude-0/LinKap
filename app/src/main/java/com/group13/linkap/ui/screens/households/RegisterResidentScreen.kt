package com.group13.linkap.ui.screens.households

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.Inter
import com.group13.linkap.ui.theme.StatusError
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// Date picker values are UTC midnight, so format and parse in UTC to avoid off-by-one days
private fun isoFormat() = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun friendlyFormat() = SimpleDateFormat("MMMM d, yyyy", Locale.getDefault()).apply {
    timeZone = TimeZone.getTimeZone("UTC")
}

private fun parseIso(text: String): Long? = try {
    isoFormat().parse(text)?.time
} catch (e: Exception) {
    null
}

/**
 * Adds no top spacing of its own. Show it inside the Scaffold content with
 * .padding(innerPadding), like RegisterHouseholdScreen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterResidentScreen(
    onSaveClick: (
        String,
        String,
        String,
        String,
        String,
        String
    ) -> Unit
) {
    var firstName by remember { mutableStateOf("") }
    var middleName by remember { mutableStateOf("") }
    var lastName by remember { mutableStateOf("") }
    var birthDate by remember { mutableStateOf("") } // saved as yyyy-MM-dd
    var sex by remember { mutableStateOf("") }
    var relationship by remember { mutableStateOf("") }

    var errorMessage by remember { mutableStateOf<String?>(null) }
    var errorField by remember { mutableStateOf<String?>(null) }
    var showDatePicker by remember { mutableStateOf(false) }

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
        unfocusedPlaceholderColor = textSecondary,
        errorBorderColor = StatusError,
        errorLabelColor = StatusError,
        errorContainerColor = surface,
        errorTextColor = textPrimary
    )
    val fieldShape = RoundedCornerShape(12.dp)
    val nextAction = KeyboardActions(
        onNext = { focusManager.moveFocus(FocusDirection.Down) }
    )

    if (showDatePicker) {
        val currentYear = Calendar.getInstance().get(Calendar.YEAR)
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = parseIso(birthDate),
            yearRange = 1900..currentYear,
            selectableDates = object : SelectableDates {
                // Birth dates cannot be in the future
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    utcTimeMillis <= System.currentTimeMillis()

                override fun isSelectableYear(year: Int): Boolean = year <= currentYear
            }
        )

        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(
                    enabled = pickerState.selectedDateMillis != null,
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            birthDate = isoFormat().format(Date(millis))
                        }
                        showDatePicker = false
                    }
                ) {
                    Text("OK", color = primary)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) {
                    Text("Cancel", color = primary)
                }
            }
        ) {
            DatePicker(
                state = pickerState,
                colors = DatePickerDefaults.colors(
                    selectedDayContainerColor = primary,
                    selectedDayContentColor = onPrimary,
                    todayContentColor = primary,
                    todayDateBorderColor = primary,
                    selectedYearContainerColor = primary,
                    selectedYearContentColor = onPrimary
                )
            )
        }
    }

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
                text = "Resident Information",
                style = MaterialTheme.typography.titleLarge,
                color = textPrimary
            )
            Text(
                text = "Add a member of this household.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
        }

        // Personal details
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
                    value = firstName,
                    onValueChange = {
                        firstName = it
                        if (errorField == "first") { errorField = null; errorMessage = null }
                    },
                    label = { Text("First Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorField == "first",
                    shape = fieldShape,
                    colors = fieldColors,
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Words,
                        imeAction = ImeAction.Next
                    ),
                    keyboardActions = nextAction
                )

                OutlinedTextField(
                    value = middleName,
                    onValueChange = { middleName = it },
                    label = { Text("Middle Name (Optional)") },
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
                    value = lastName,
                    onValueChange = {
                        lastName = it
                        if (errorField == "last") { errorField = null; errorMessage = null }
                    },
                    label = { Text("Last Name") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorField == "last",
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

                // Birth date: read-only field that opens the date picker
                Box(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = parseIso(birthDate)?.let { friendlyFormat().format(Date(it)) } ?: "",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Birth Date") },
                        placeholder = { Text("Select date") },
                        trailingIcon = {
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Choose birth date",
                                tint = primary
                            )
                        },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = fieldShape,
                        colors = fieldColors
                    )
                    // Transparent layer so a tap anywhere on the field opens the picker
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clickable {
                                focusManager.clearFocus()
                                showDatePicker = true
                            }
                    )
                }

                // Sex
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "Sex",
                        fontFamily = Inter,
                        fontWeight = FontWeight.Medium,
                        fontSize = 13.sp,
                        color = if (errorField == "sex") StatusError else textSecondary
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf("Male", "Female").forEach { option ->
                            SelectChip(
                                text = option,
                                selected = sex == option,
                                primary = primary,
                                onPrimary = onPrimary,
                                border = if (errorField == "sex") StatusError else border,
                                textPrimary = textPrimary,
                                onClick = {
                                    sex = option
                                    if (errorField == "sex") { errorField = null; errorMessage = null }
                                }
                            )
                        }
                    }
                }
            }
        }

        // Household role
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
                    value = relationship,
                    onValueChange = {
                        relationship = it
                        if (errorField == "relationship") { errorField = null; errorMessage = null }
                    },
                    label = { Text("Relationship to Household Head") },
                    placeholder = { Text("e.g. Head, Spouse, Child") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = errorField == "relationship",
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

                // Quick picks fill the field above
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    listOf("Head", "Spouse", "Child", "Parent", "Sibling", "Other").forEach { option ->
                        SelectChip(
                            text = option,
                            selected = relationship.trim().equals(option, ignoreCase = true),
                            primary = primary,
                            onPrimary = onPrimary,
                            border = border,
                            textPrimary = textPrimary,
                            onClick = {
                                relationship = option
                                if (errorField == "relationship") { errorField = null; errorMessage = null }
                            }
                        )
                    }
                }
            }
        }

        errorMessage?.let {
            Text(
                text = it,
                style = MaterialTheme.typography.bodyMedium,
                color = StatusError
            )
        }

        Button(
            onClick = {
                errorField = when {
                    firstName.isBlank() -> "first"
                    lastName.isBlank() -> "last"
                    sex.isBlank() -> "sex"
                    relationship.isBlank() -> "relationship"
                    else -> null
                }

                errorMessage = when (errorField) {
                    "first" -> "Please enter the first name."
                    "last" -> "Please enter the last name."
                    "sex" -> "Please enter the sex."
                    "relationship" -> "Please enter the relationship."
                    else -> {
                        onSaveClick(
                            firstName.trim(),
                            middleName.trim(),
                            lastName.trim(),
                            birthDate.trim(),
                            sex.trim(),
                            relationship.trim()
                        )
                        null
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = primary,
                contentColor = onPrimary
            )
        ) {
            Text(
                text = "Save Resident",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectChip(
    text: String,
    selected: Boolean,
    primary: Color,
    onPrimary: Color,
    border: Color,
    textPrimary: Color,
    onClick: () -> Unit
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(999.dp),
        color = if (selected) primary else Color.Transparent,
        border = BorderStroke(1.dp, if (selected) primary else border)
    ) {
        Text(
            text = text,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
            color = if (selected) onPrimary else textPrimary,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 10.dp)
        )
    }
}