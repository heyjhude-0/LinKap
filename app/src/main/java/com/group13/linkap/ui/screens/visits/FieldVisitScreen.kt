
package com.group13.linkap.ui.screens.visits

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.material3.Icon
import androidx.compose.ui.graphics.Color
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Checkbox
import androidx.compose.runtime.*import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


@Composable
fun FieldVisitScreen(
    visitHouseholdId: String?,
    household: Household?,
    onScanClick: () -> Unit,
    onSearchClick: () -> Unit,
    residents: List<Resident>,
    onSaveVisit: (
        String,
        Set<String>,
        String,
        String,
        Long
    ) -> Unit,
    isSaving: Boolean,
    saveMessage: String?
) {
    var selectedResidentIds by remember {
        mutableStateOf(setOf<String>())
    }
    var selectedCategory by remember {
        mutableStateOf("Routine Checkup")
    }

    var visitNotes by remember {
        mutableStateOf("")
    }
    var categoryExpanded by remember {
        mutableStateOf(false)
    }

    var visitDate by remember {
        mutableStateOf(System.currentTimeMillis())
    }
    LaunchedEffect(visitHouseholdId) {
        selectedResidentIds = emptySet()
    }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Record Field Visit",
            style = MaterialTheme.typography.headlineSmall
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Scan a household QR code or search manually to begin recording a visit.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (visitHouseholdId != null &&
            household?.householdId == visitHouseholdId
        ) {
            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Select Residents Visited",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (residents.isEmpty()) {
                Text(
                    text = "No residents registered in this household.",
                    style = MaterialTheme.typography.bodyMedium
                )
            } else {
                Card(
                    modifier = Modifier.fillMaxWidth()
                ) {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 280.dp)
                            .padding(8.dp)
                    ) {
                        items(
                            items = residents,
                            key = { it.residentId }
                        ) { resident ->

                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = resident.residentId in selectedResidentIds,
                                    onCheckedChange = { checked ->
                                        selectedResidentIds =
                                            if (checked) {
                                                selectedResidentIds + resident.residentId
                                            } else {
                                                selectedResidentIds - resident.residentId
                                            }
                                    }
                                )

                                Text(
                                    text = listOfNotNull(
                                        resident.firstName,
                                        resident.middleName,
                                        resident.lastName
                                    ).joinToString(" ")
                                )
                            }
                        }
                    }
                }
            }
            if (selectedResidentIds.isNotEmpty()) {
                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Visit Information",
                    style = MaterialTheme.typography.titleLarge
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Visit Category",
                    style = MaterialTheme.typography.titleMedium
                )

                Box {
                    OutlinedButton(
                        onClick = { categoryExpanded = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(selectedCategory)
                    }

                    DropdownMenu(
                        expanded = categoryExpanded,
                        onDismissRequest = {
                            categoryExpanded = false
                        }
                    ) {
                        listOf(
                            "Routine Checkup",
                            "Prenatal",
                            "Postnatal",
                            "Child Health",
                            "Immunization",
                            "Family Planning",
                            "Other"
                        ).forEach { category ->
                            DropdownMenuItem(
                                text = { Text(category) },
                                onClick = {
                                    selectedCategory = category
                                    categoryExpanded = false
                                }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Visit Date",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = SimpleDateFormat(
                        "MMMM dd, yyyy",
                        Locale.getDefault()
                    ).format(Date(visitDate)),
                    onValueChange = {},
                    readOnly = true,
                    enabled = false,
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = "Observations / Notes",
                    style = MaterialTheme.typography.titleMedium
                )

                OutlinedTextField(
                    value = visitNotes,
                    onValueChange = { visitNotes = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = {
                        Text("Enter observations or details about the visit...")
                    },
                    minLines = 3,
                    maxLines = 5
                )
                Button(
                    onClick = {
                        onSaveVisit(
                            visitHouseholdId!!,
                            selectedResidentIds,
                            selectedCategory,
                            visitNotes,
                            visitDate
                        )
                    },
                    enabled = !isSaving && visitHouseholdId != null
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            color = MaterialTheme.colorScheme.onPrimary,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Save Field Visit")
                    }
                }
                saveMessage?.let { message ->
                    Text(
                        text = message,
                        color = if (message.contains("successfully", ignoreCase = true)) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.error
                        },
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    tint = GreenPrimary,
                    modifier = Modifier.height(56.dp)
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Scan Household QR",
                    style = MaterialTheme.typography.titleMedium
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Quickly identify a registered household using its unique QR code.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onScanClick,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = GreenPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null
                    )
                    Text(
                        text = " Scan QR Code",
                        modifier = Modifier.padding(start = 8.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedButton(
            onClick = onSearchClick,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Search,
                contentDescription = null
            )
            Text(
                text = " Search Household Manually",
                modifier = Modifier.padding(start = 8.dp)
            )
        }
    }
}
