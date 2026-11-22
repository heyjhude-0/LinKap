package com.group13.linkap.ui.screens.households

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.GreenSecondaryDark
import com.group13.linkap.ui.theme.Inter
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark

private class DetailsColors(
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val heroBackground: Color,
    val heroText: Color
)

@Composable
private fun rememberDetailsColors(): DetailsColors {
    val dark = isSystemInDarkTheme()
    return remember(dark) {
        if (dark) {
            DetailsColors(
                surface = SurfaceDark,
                primary = GreenPrimaryDark,
                onPrimary = BackgroundDark,
                textPrimary = TextPrimaryDark,
                textSecondary = TextSecondaryDark,
                border = Color(0xFF2A3A30),
                heroBackground = GreenSecondaryDark,
                heroText = TextPrimaryDark
            )
        } else {
            DetailsColors(
                surface = SurfaceLight,
                primary = GreenPrimary,
                onPrimary = Color.White,
                textPrimary = TextPrimary,
                textSecondary = TextSecondary,
                border = Color(0xFFD5E5DB),
                heroBackground = GreenPrimary,
                heroText = Color.White
            )
        }
    }
}

/**
 * This screen adds no top spacing of its own. Show it inside the Scaffold content
 * with .padding(innerPadding) so it starts below the app bar.
 */
@Composable
fun HouseholdDetailsScreen(
    household: Household,
    residents: List<Resident>,
    onBackClick: () -> Unit,
    onAddResidentClick: () -> Unit,
    onEditResidentClick: (Resident) -> Unit
) {
    val colors = rememberDetailsColors()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header card with the household code
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.heroBackground
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colors.heroText.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = null,
                            tint = colors.heroText,
                            modifier = Modifier.size(26.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = "HOUSEHOLD",
                            fontFamily = Inter,
                            fontWeight = FontWeight.SemiBold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp,
                            color = colors.heroText.copy(alpha = 0.85f)
                        )
                        Text(
                            text = household.householdCode,
                            style = MaterialTheme.typography.headlineMedium,
                            color = colors.heroText
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = household.purok.toString(),
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.heroText,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.heroText.copy(alpha = 0.15f))
                        .padding(horizontal = 12.dp, vertical = 4.dp)
                )
            }
        }

        // Address
        InfoCard(
            title = "Household Information",
            icon = Icons.Default.Home,
            colors = colors
        ) {
            InfoRow("Purok", household.purok.toString(), colors)
            InfoDivider(colors)
            InfoRow("Street / Sitio", household.street ?: "Not specified", colors)
            InfoDivider(colors)
            InfoRow("Barangay", household.barangay.toString(), colors)
        }

        // Location
        InfoCard(
            title = "Location",
            icon = Icons.Default.LocationOn,
            colors = colors
        ) {
            InfoRow(
                "Latitude",
                household.latitude?.toString() ?: "Not recorded",
                colors
            )
            InfoDivider(colors)
            InfoRow(
                "Longitude",
                household.longitude?.toString() ?: "Not recorded",
                colors
            )
        }

        Text(
            text = "Household Members",
            style = MaterialTheme.typography.titleLarge,
            color = colors.textPrimary
        )

        // Residents
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.border),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(colors.primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = null,
                            tint = colors.primary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Registered Residents (${residents.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )
                }

                Button(
                    onClick = onAddResidentClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Add Resident",
                        style = MaterialTheme.typography.labelLarge
                    )
                }

                if (residents.isEmpty()) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "No residents registered yet.",
                            style = MaterialTheme.typography.titleMedium,
                            color = colors.textPrimary,
                            textAlign = TextAlign.Center
                        )
                        Text(
                            text = "Tap Add Resident to register the first member.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary,
                            textAlign = TextAlign.Center
                        )
                    }
                } else {
                    Column {
                        residents.forEachIndexed { index, resident ->
                            if (index > 0) {
                                InfoDivider(colors)
                            }
                            ResidentRow(
                                resident = resident,
                                colors = colors,
                                onEditClick = { onEditResidentClick(resident) }
                            )
                        }
                    }
                }
            }
        }

        OutlinedButton(
            onClick = onBackClick,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, colors.primary)
        ) {
            Text(
                text = "Back to Households",
                color = colors.primary,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun ResidentRow(
    resident: Resident,
    colors: DetailsColors,
    onEditClick: () -> Unit
) {
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
                .size(44.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                fontFamily = Inter,
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = colors.primary
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${resident.firstName} ${resident.lastName}",
                style = MaterialTheme.typography.titleMedium,
                color = colors.textPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = resident.relationshipToHead,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = colors.primary,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.primary.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = resident.sex,
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    color = colors.textSecondary
                )
            }
        }

        TextButton(onClick = onEditClick) {
            Text(
                text = "Edit",
                color = colors.primary,
                style = MaterialTheme.typography.labelLarge
            )
        }
    }
}

@Composable
private fun InfoCard(
    title: String,
    icon: ImageVector,
    colors: DetailsColors,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(colors.primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = colors.primary,
                        modifier = Modifier.size(18.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            content()
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String, colors: DetailsColors) {
    // Empty values like "Not specified" are shown in the softer text color
    val muted = value == "Not specified" || value == "Not recorded"

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
            color = colors.textSecondary,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = if (muted) colors.textSecondary else colors.textPrimary,
            modifier = Modifier.weight(0.6f)
        )
    }
}

@Composable
private fun InfoDivider(colors: DetailsColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.border)
    )
}