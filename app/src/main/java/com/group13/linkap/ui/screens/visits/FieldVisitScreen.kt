package com.group13.linkap.ui.screens.visits

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.selection.toggleable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
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
import com.group13.linkap.util.LocationHelper
import com.group13.linkap.util.LocationResult
import com.group13.linkap.util.LocationState
import kotlinx.coroutines.delay
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adds no outer padding or scrolling of its own. Show it inside the HomeScreen content
 * column (20dp margin, vertical scroll), like HouseholdScreen.
 */
@Composable
fun FieldVisitScreen(
    visitHouseholdId: String?,
    household: Household?,
    onScanClick: () -> Unit,
    onSearchClick: () -> Unit,
    residents: List<Resident>,
    onSaveVisit: (
        householdId: String,
        residentIds: Set<String>,
        category: String,
        notes: String,
        visitDate: Long,
        latitude: Double?,
        longitude: Double?
    ) -> Unit,
    isSaving: Boolean,
    saveMessage: String?,
    onMessageShown: () -> Unit = {}
) {
    val context = LocalContext.current

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

    var locationState by remember { mutableStateOf<LocationState>(LocationState.Idle) }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val fineGranted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        val coarseGranted = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (fineGranted || coarseGranted) {
            locationState = LocationState.Loading
            LocationHelper.fetchCurrentLocation(context) { result ->
                locationState = when (result) {
                    is LocationResult.Success -> LocationState.Success(
                        result.latitude,
                        result.longitude,
                        result.accuracy
                    )
                    is LocationResult.Error -> LocationState.Error(result.message)
                }
            }
        } else {
            locationState = LocationState.Error("Location permission is denied.")
        }
    }

    val onRequestLocation = {
        if (LocationHelper.hasLocationPermission(context)) {
            if (!LocationHelper.isLocationServicesEnabled(context)) {
                locationState = LocationState.Error("Device location services are disabled. Please enable GPS in your device settings.")
            } else {
                locationState = LocationState.Loading
                LocationHelper.fetchCurrentLocation(context) { result ->
                    locationState = when (result) {
                        is LocationResult.Success -> LocationState.Success(
                            result.latitude,
                            result.longitude,
                            result.accuracy
                        )
                        is LocationResult.Error -> LocationState.Error(result.message)
                    }
                }
            }
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    LaunchedEffect(visitHouseholdId) {
        selectedResidentIds = emptySet()
        locationState = LocationState.Idle
    }

    // After a successful save, clear the form so the same visit cannot be saved twice
    LaunchedEffect(saveMessage) {
        if (saveMessage?.contains("successfully", ignoreCase = true) == true) {
            selectedResidentIds = emptySet()
            visitNotes = ""
            selectedCategory = "Routine Checkup"
            visitDate = System.currentTimeMillis()
            locationState = LocationState.Idle
            delay(4000)
            onMessageShown()
        }
    }

    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val onPrimary = if (dark) BackgroundDark else Color.White
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)
    val success = if (dark) GreenPrimaryDark else Color(0xFF256B00)

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

    val householdSelected = visitHouseholdId != null &&
            household?.householdId == visitHouseholdId

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Record Field Visit",
                style = MaterialTheme.typography.headlineMedium,
                color = primary
            )
            Text(
                text = "Scan a household QR code or search manually to begin recording a visit.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
        }

        // Save result, shown here so it stays visible after the form is cleared
        saveMessage?.let { message ->
            val isSuccess = message.contains("successfully", ignoreCase = true)
            val messageColor = if (isSuccess) success else StatusError

            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                color = messageColor,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(messageColor.copy(alpha = 0.12f))
                    .padding(horizontal = 14.dp, vertical = 10.dp)
            )
        }

        if (!householdSelected) {
            // Scan card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface),
                shape = RoundedCornerShape(20.dp),
                border = BorderStroke(1.dp, border),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(38.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "Scan Household QR",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = "Quickly identify a registered household using its unique QR code.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Button(
                        onClick = onScanClick,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Scan QR Code",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                        )
                    }
                }
            }

            OutlinedButton(
                onClick = onSearchClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.5.dp, primary)
            ) {
                Icon(
                    imageVector = Icons.Default.Search,
                    contentDescription = null,
                    tint = primary,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Search Household Manually",
                    color = primary,
                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                )
            }
        } else {
            // Selected household
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.5.dp, primary),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "SELECTED HOUSEHOLD",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 11.sp,
                            letterSpacing = 1.sp,
                            color = textSecondary
                        )
                        Text(
                            text = household?.householdCode ?: "",
                            style = MaterialTheme.typography.titleLarge,
                            color = textPrimary
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = listOfNotNull(household?.purok, household?.barangay)
                                .joinToString(" · "),
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }
                }
            }

            // Change household
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = onScanClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.QrCodeScanner,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Scan another",
                        color = primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                OutlinedButton(
                    onClick = onSearchClick,
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, primary)
                ) {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Search",
                        color = primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }

            // Residents
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = surface),
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, border),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Select Residents Visited",
                            style = MaterialTheme.typography.titleMedium,
                            color = textPrimary,
                            modifier = Modifier.weight(1f)
                        )

                        if (selectedResidentIds.isNotEmpty()) {
                            Text(
                                text = "${selectedResidentIds.size} selected",
                                fontFamily = Inter,
                                fontWeight = FontWeight.SemiBold,
                                fontSize = 12.sp,
                                color = primary,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(999.dp))
                                    .background(primary.copy(alpha = 0.12f))
                                    .padding(horizontal = 10.dp, vertical = 3.dp)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    if (residents.isEmpty()) {
                        Text(
                            text = "No residents registered in this household.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = textSecondary,
                            modifier = Modifier.padding(vertical = 8.dp)
                        )
                    } else {
                        residents.forEachIndexed { index, resident ->
                            if (index > 0) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(border)
                                )
                            }

                            val checked = resident.residentId in selectedResidentIds
                            val toggle = { isChecked: Boolean ->
                                selectedResidentIds =
                                    if (isChecked) {
                                        selectedResidentIds + resident.residentId
                                    } else {
                                        selectedResidentIds - resident.residentId
                                    }
                            }

                            // The whole row is tappable, not only the small checkbox
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .toggleable(
                                        value = checked,
                                        role = Role.Checkbox,
                                        onValueChange = toggle
                                    )
                                    .padding(vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Checkbox(
                                    checked = checked,
                                    onCheckedChange = null,
                                    colors = CheckboxDefaults.colors(
                                        checkedColor = primary,
                                        checkmarkColor = onPrimary,
                                        uncheckedColor = textSecondary
                                    )
                                )

                                Spacer(modifier = Modifier.width(12.dp))

                                Text(
                                    text = listOf(
                                        resident.firstName,
                                        resident.middleName.orEmpty(),
                                        resident.lastName
                                    ).filter { it.isNotBlank() }.joinToString(" "),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textPrimary
                                )
                            }
                        }
                    }
                }
            }

            // Visit information
            if (selectedResidentIds.isNotEmpty()) {
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(containerColor = surface),
                    shape = RoundedCornerShape(16.dp),
                    border = BorderStroke(1.dp, border),
                    elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "Visit Information",
                            style = MaterialTheme.typography.titleLarge,
                            color = textPrimary
                        )

                        // Category dropdown
                        Box(modifier = Modifier.fillMaxWidth()) {
                            OutlinedTextField(
                                value = selectedCategory,
                                onValueChange = {},
                                readOnly = true,
                                label = { Text("Visit Category") },
                                trailingIcon = {
                                    Icon(
                                        imageVector = Icons.Default.KeyboardArrowDown,
                                        contentDescription = null,
                                        tint = primary
                                    )
                                },
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                shape = fieldShape,
                                colors = fieldColors
                            )

                            // Transparent layer so a tap anywhere on the field opens the menu
                            Box(
                                modifier = Modifier
                                    .matchParentSize()
                                    .clickable { categoryExpanded = true }
                            )

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

                        // Visit date (today, not editable)
                        OutlinedTextField(
                            value = SimpleDateFormat(
                                "MMMM dd, yyyy",
                                Locale.getDefault()
                            ).format(Date(visitDate)),
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("Visit Date") },
                            trailingIcon = {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    tint = textSecondary
                                )
                            },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            shape = fieldShape,
                            colors = fieldColors
                        )

                        // GPS Location Capture Section
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(containerColor = surface),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, border)
                        ) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.LocationOn,
                                        contentDescription = null,
                                        tint = primary,
                                        modifier = Modifier.size(20.dp)
                                    )
                                    Text(
                                        text = "GPS Location",
                                        style = MaterialTheme.typography.titleMedium,
                                        color = textPrimary
                                    )
                                }

                                when (val state = locationState) {
                                    is LocationState.Idle -> {
                                        Text(
                                            text = "Optionally capture current GPS coordinates for this visit.",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = textSecondary
                                        )

                                        OutlinedButton(
                                            onClick = onRequestLocation,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(48.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.5.dp, primary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.MyLocation,
                                                contentDescription = null,
                                                tint = primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(8.dp))
                                            Text(
                                                text = "Capture Current Location",
                                                color = primary,
                                                style = MaterialTheme.typography.labelLarge
                                            )
                                        }
                                    }

                                    is LocationState.Loading -> {
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.Center
                                        ) {
                                            CircularProgressIndicator(
                                                modifier = Modifier.size(20.dp),
                                                color = primary,
                                                strokeWidth = 2.dp
                                            )
                                            Spacer(modifier = Modifier.width(12.dp))
                                            Text(
                                                text = "Retrieving current location...",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = textSecondary
                                            )
                                        }
                                    }

                                    is LocationState.Success -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(success.copy(alpha = 0.12f))
                                                .padding(12.dp)
                                        ) {
                                            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Icon(
                                                        imageVector = Icons.Default.CheckCircle,
                                                        contentDescription = null,
                                                        tint = success,
                                                        modifier = Modifier.size(18.dp)
                                                    )
                                                    Spacer(modifier = Modifier.width(6.dp))
                                                    Text(
                                                        text = "Location Captured Successfully",
                                                        fontFamily = Inter,
                                                        fontWeight = FontWeight.SemiBold,
                                                        fontSize = 14.sp,
                                                        color = success
                                                    )
                                                }
                                                Spacer(modifier = Modifier.height(2.dp))
                                                Text(
                                                    text = "Latitude: ${String.format(Locale.US, "%.6f", state.latitude)}°",
                                                    fontFamily = Inter,
                                                    fontSize = 13.sp,
                                                    color = textPrimary
                                                )
                                                Text(
                                                    text = "Longitude: ${String.format(Locale.US, "%.6f", state.longitude)}°",
                                                    fontFamily = Inter,
                                                    fontSize = 13.sp,
                                                    color = textPrimary
                                                )
                                                if (state.accuracy < 999f) {
                                                    Text(
                                                        text = "Accuracy: ±${state.accuracy.toInt()} m",
                                                        fontFamily = Inter,
                                                        fontSize = 12.sp,
                                                        color = textSecondary
                                                    )
                                                }
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = onRequestLocation,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, primary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Recapture Location",
                                                color = primary,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }

                                    is LocationState.Error -> {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .clip(RoundedCornerShape(10.dp))
                                                .background(StatusError.copy(alpha = 0.12f))
                                                .padding(12.dp)
                                        ) {
                                            Row(verticalAlignment = Alignment.Top) {
                                                Icon(
                                                    imageVector = Icons.Default.Warning,
                                                    contentDescription = null,
                                                    tint = StatusError,
                                                    modifier = Modifier
                                                        .size(18.dp)
                                                        .padding(top = 2.dp)
                                                )
                                                Spacer(modifier = Modifier.width(8.dp))
                                                Text(
                                                    text = state.message,
                                                    fontFamily = Inter,
                                                    fontSize = 13.sp,
                                                    color = StatusError
                                                )
                                            }
                                        }

                                        OutlinedButton(
                                            onClick = onRequestLocation,
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(44.dp),
                                            shape = RoundedCornerShape(10.dp),
                                            border = BorderStroke(1.dp, primary)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Refresh,
                                                contentDescription = null,
                                                tint = primary,
                                                modifier = Modifier.size(18.dp)
                                            )
                                            Spacer(modifier = Modifier.width(6.dp))
                                            Text(
                                                text = "Try Again",
                                                color = primary,
                                                style = MaterialTheme.typography.labelMedium
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        OutlinedTextField(
                            value = visitNotes,
                            onValueChange = { visitNotes = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text("Observations / Notes") },
                            placeholder = {
                                Text("Enter observations or details about the visit...")
                            },
                            minLines = 3,
                            maxLines = 5,
                            shape = fieldShape,
                            colors = fieldColors
                        )

                        Button(
                            onClick = {
                                val (lat, lng) = when (val state = locationState) {
                                    is LocationState.Success -> Pair(state.latitude, state.longitude)
                                    else -> Pair(null, null)
                                }
                                onSaveVisit(
                                    visitHouseholdId!!,
                                    selectedResidentIds,
                                    selectedCategory,
                                    visitNotes,
                                    visitDate,
                                    lat,
                                    lng
                                )
                            },
                            enabled = !isSaving && visitHouseholdId != null,
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
                            if (isSaving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(22.dp),
                                    color = onPrimary,
                                    strokeWidth = 2.dp
                                )
                            } else {
                                Text(
                                    text = "Save Field Visit",
                                    style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}
