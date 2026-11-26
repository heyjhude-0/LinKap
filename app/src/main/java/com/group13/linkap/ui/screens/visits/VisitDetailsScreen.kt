package com.group13.linkap.ui.screens.visits

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.GreenSecondaryDark
import com.group13.linkap.ui.theme.Inter
import com.group13.linkap.ui.theme.StatusError
import com.group13.linkap.ui.theme.StatusWarning
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Adds no top spacing of its own. Show it inside the Scaffold content with
 * .padding(innerPadding) so it starts below the app bar, like HouseholdDetailsScreen.
 */
@Composable
fun VisitDetailsScreen(
    visit: Visit,
    household: Household?,
    residents: List<Resident>,
    isLoadingResidents: Boolean,
    onBackClick: () -> Unit
) {
    val date = SimpleDateFormat(
        "MMMM dd, yyyy",
        Locale.getDefault()
    ).format(Date(visit.visitDate))

    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)
    val heroBackground = if (dark) GreenSecondaryDark else GreenPrimary
    val heroText = if (dark) TextPrimaryDark else Color.White

    // Same groups as the history list: synced, failed, and everything else (pending)
    val status = visit.syncStatus.uppercase()
    val statusColor = when (status) {
        "SYNCED" -> if (dark) GreenPrimaryDark else Color(0xFF256B00)
        "FAILED" -> StatusError
        else -> if (dark) StatusWarning else Color(0xFF8A5A00)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = heroBackground
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "FIELD VISIT",
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = heroText.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = visit.visitCategory,
                    style = MaterialTheme.typography.headlineMedium,
                    color = heroText
                )

                Spacer(modifier = Modifier.height(14.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = heroText,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = date,
                        style = MaterialTheme.typography.bodyLarge,
                        color = heroText,
                        modifier = Modifier.weight(1f)
                    )

                    // Light pill so the status stays readable on the green header
                    Text(
                        text = status,
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = statusColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(surface)
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        }

        // Household
        SectionCard(
            title = "Household",
            icon = Icons.Default.Home,
            surface = surface,
            primary = primary,
            textPrimary = textPrimary,
            border = border
        ) {
            InfoRow(
                label = "Household Code",
                value = household?.householdCode ?: "Loading household...",
                textPrimary = textPrimary,
                textSecondary = textSecondary,
                muted = household == null
            )

            household?.let {
                InfoDivider(border)
                InfoRow("Purok", it.purok, textPrimary, textSecondary)
                InfoDivider(border)
                InfoRow("Street", it.street ?: "Not specified", textPrimary, textSecondary,
                    muted = it.street == null)
                InfoDivider(border)
                InfoRow("Barangay", it.barangay, textPrimary, textSecondary)
            }
        }

        // Residents
        SectionCard(
            title = if (isLoadingResidents || residents.isEmpty()) {
                "Residents Included"
            } else {
                "Residents Included (${residents.size})"
            },
            icon = Icons.Default.Person,
            surface = surface,
            primary = primary,
            textPrimary = textPrimary,
            border = border
        ) {
            when {
                isLoadingResidents -> {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(28.dp),
                            color = primary,
                            strokeWidth = 3.dp
                        )
                    }
                }

                residents.isEmpty() -> {
                    Text(
                        text = "No residents found for this visit.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        modifier = Modifier.padding(vertical = 8.dp)
                    )
                }

                else -> {
                    residents.forEachIndexed { index, resident ->
                        if (index > 0) InfoDivider(border)

                        val fullName = listOfNotNull(
                            resident.firstName,
                            resident.middleName?.takeIf { it.isNotBlank() },
                            resident.lastName
                        ).joinToString(" ")

                        val initials = listOfNotNull(
                            resident.firstName.firstOrNull(),
                            resident.lastName.firstOrNull()
                        ).joinToString("").uppercase()

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(primary.copy(alpha = 0.12f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = initials,
                                    fontFamily = Inter,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp,
                                    color = primary
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column {
                                Text(
                                    text = fullName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textPrimary
                                )
                                Text(
                                    text = "Resident ${index + 1}",
                                    fontFamily = Inter,
                                    fontSize = 12.sp,
                                    color = textSecondary
                                )
                            }
                        }
                    }
                }
            }
        }

        // Notes
        SectionCard(
            title = "Observations / Notes",
            icon = Icons.Default.Edit,
            surface = surface,
            primary = primary,
            textPrimary = textPrimary,
            border = border
        ) {
            val notes = visit.notes?.takeIf { it.isNotBlank() }

            Text(
                text = notes ?: "No observations or notes recorded.",
                style = MaterialTheme.typography.bodyLarge,
                color = if (notes != null) textPrimary else textSecondary,
                modifier = Modifier.padding(vertical = 4.dp)
            )
        }

        // Location
        SectionCard(
            title = "Location",
            icon = Icons.Default.LocationOn,
            surface = surface,
            primary = primary,
            textPrimary = textPrimary,
            border = border
        ) {
            if (visit.latitude != null && visit.longitude != null) {
                InfoRow(
                    "Latitude",
                    String.format(Locale.US, "%.6f°", visit.latitude),
                    textPrimary,
                    textSecondary
                )
                InfoDivider(border)
                InfoRow(
                    "Longitude",
                    String.format(Locale.US, "%.6f°", visit.longitude),
                    textPrimary,
                    textSecondary
                )
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(textSecondary.copy(alpha = 0.10f))
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = textSecondary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Location not captured",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, primary)
        ) {
            Text(
                text = "Back to Visit History",
                color = primary,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionCard(
    title: String,
    icon: ImageVector,
    surface: Color,
    primary: Color,
    textPrimary: Color,
    border: Color,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
private fun InfoRow(
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color,
    muted: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = textSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (muted) textSecondary else textPrimary,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun InfoDivider(border: Color) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(border)
    )
}