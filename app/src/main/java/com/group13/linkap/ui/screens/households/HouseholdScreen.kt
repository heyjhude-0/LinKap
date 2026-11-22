package com.group13.linkap.ui.screens.households

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.Inter
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark

/**
 * This screen adds no outer padding of its own. The host (for example HomeScreen)
 * already provides the 20dp screen margin, so adding more here would double it.
 */
@Composable
fun HouseholdScreen(
    modifier: Modifier = Modifier,
    households: List<Household>,
    onScanClick: () -> Unit,
    onHouseholdClick: (Household) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }

    val filteredHouseholds = households.filter {
        it.householdCode.contains(searchQuery, ignoreCase = true) ||
                it.purok.contains(searchQuery, ignoreCase = true) ||
                it.barangay.contains(searchQuery, ignoreCase = true)
    }

    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val onPrimary = if (dark) BackgroundDark else Color.White
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)

    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Households",
                style = MaterialTheme.typography.headlineMedium,
                color = primary
            )
            Text(
                text = "Manage and view registered households in your barangay.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
        }

        // Search + QR scan
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Code, purok or barangay") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.Search,
                        contentDescription = null,
                        tint = textSecondary
                    )
                },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = textPrimary,
                    unfocusedTextColor = textPrimary,
                    focusedContainerColor = surface,
                    unfocusedContainerColor = surface,
                    focusedBorderColor = primary,
                    unfocusedBorderColor = border,
                    cursorColor = primary,
                    focusedPlaceholderColor = textSecondary,
                    unfocusedPlaceholderColor = textSecondary
                )
            )

            // Same 56dp height as the search field, with matching rounded corners
            FilledIconButton(
                onClick = onScanClick,
                modifier = Modifier.size(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = primary,
                    contentColor = onPrimary
                )
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = "Scan household QR",
                    modifier = Modifier.size(28.dp)
                )
            }
        }

        // Count
        Text(
            text = "Registered Households (${households.size})",
            style = MaterialTheme.typography.titleMedium,
            color = textPrimary
        )

        if (filteredHouseholds.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = if (households.isEmpty()) {
                        "No households registered yet."
                    } else {
                        "No households found."
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )
                Text(
                    text = if (households.isEmpty()) {
                        "Use Register Household on the Home tab to add one."
                    } else {
                        "Try a different code, purok or barangay."
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                filteredHouseholds.forEach { household ->
                    Card(
                        onClick = {
                            onHouseholdClick(household)
                        },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = surface),
                        border = BorderStroke(1.dp, border),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 14.dp),
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
                                    text = household.householdCode,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = textPrimary
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = household.purok,
                                        fontFamily = Inter,
                                        fontWeight = FontWeight.SemiBold,
                                        fontSize = 12.sp,
                                        color = primary,
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(999.dp))
                                            .background(primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 10.dp, vertical = 3.dp)
                                    )

                                    Spacer(modifier = Modifier.width(8.dp))

                                    Text(
                                        text = household.barangay,
                                        fontFamily = Inter,
                                        fontSize = 12.sp,
                                        color = textSecondary
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = household.street ?: "No street specified",
                                    fontFamily = Inter,
                                    fontSize = 13.sp,
                                    color = textSecondary
                                )
                            }

                            Icon(
                                imageVector = Icons.Default.KeyboardArrowRight,
                                contentDescription = null,
                                tint = textSecondary
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}