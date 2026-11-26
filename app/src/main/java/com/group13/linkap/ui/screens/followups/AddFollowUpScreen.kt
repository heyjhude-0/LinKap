package com.group13.linkap.ui.screens.followups

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.group13.linkap.data.local.entity.Household
import android.app.DatePickerDialog
import androidx.compose.foundation.clickable
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import java.util.Calendar
import androidx.compose.ui.platform.LocalContext

@Composable
fun AddFollowUpScreen(
    households: List<Household>,
    onSave: (String, String, String, Long) -> Unit,
    onBack: () -> Unit
) {
    var selectedHouseholdId by remember { mutableStateOf("") }
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var dueDate by remember { mutableLongStateOf(0L) }

    val dateFormatter = remember {
        java.text.SimpleDateFormat("MMM dd, yyyy", java.util.Locale.getDefault())
    }
    val context = LocalContext.current

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Add Follow-up",
            style = MaterialTheme.typography.headlineSmall
        )

        Text(
            text = "Household",
            style = MaterialTheme.typography.titleMedium
        )

        var expanded by remember { mutableStateOf(false) }

        if (households.isEmpty()) {
            Text(
                text = "No registered households available.",
                color = MaterialTheme.colorScheme.error)
        }

        Box {
            OutlinedTextField(
                value = households.firstOrNull {
                    it.householdId == selectedHouseholdId
                }?.householdCode ?: "",
                onValueChange = {},
                readOnly = true,
                enabled = households.isNotEmpty(),
                label = { Text("Select Household") },
                placeholder = { Text("Choose a household") },
                trailingIcon = {
                    IconButton(onClick = { expanded = !expanded }) {
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select household"
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = households.isNotEmpty()) {
                        expanded = !expanded
                    }
            )

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false }
            ) {
                households.forEach { household ->
                    DropdownMenuItem(
                        text = {
                            Text(household.householdCode)
                        },
                        onClick = {
                            selectedHouseholdId = household.householdId
                            expanded = false
                        }
                    )
                }
            }
        }
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Follow-up Title") },
            placeholder = { Text("e.g. Monitor patient's condition") },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            placeholder = { Text("Enter additional details...") },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            maxLines = 5
        )
        OutlinedButton(
            onClick = {
                val calendar = Calendar.getInstance()

                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        calendar.set(year, month, day, 0, 0, 0)
                        calendar.set(Calendar.MILLISECOND, 0)
                        dueDate = calendar.timeInMillis
                    },
                    calendar.get(Calendar.YEAR),
                    calendar.get(Calendar.MONTH),
                    calendar.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = System.currentTimeMillis()
                }.show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                if (dueDate == 0L) "Select Due Date"
                else "Due: ${dateFormatter.format(java.util.Date(dueDate))}"
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = onBack,
                modifier = Modifier.weight(1f)
            ) {
                Text("Cancel")
            }

            Button(
                onClick = {
                    onSave(
                        selectedHouseholdId,
                        title.trim(),
                        description.trim(),
                        dueDate
                    )
                },
                enabled = selectedHouseholdId.isNotBlank() &&
                        title.isNotBlank() &&
                        dueDate > 0L,
                modifier = Modifier.weight(1f)
            ) {
                Text("Save Follow-up")
            }
        }
    }

}