package com.group13.linkap.ui.screens.visits

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
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
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

private fun isSynced(status: String) = status.uppercase() == "SYNCED"
private fun isFailed(status: String) = status.uppercase() == "FAILED"

/**
 * Adds no outer padding and does not scroll by itself. Show it inside the HomeScreen
 * content column (20dp margin, vertical scroll), like HouseholdScreen. A lazy list or a
 * weight(1f) child inside that scrolling column is measured with zero height, which
 * is what made the page blank.
 */
@Composable
fun VisitHistoryScreen(
    visits: List<Visit>,
    households: List<Household>,
    onVisitClick: (Visit) -> Unit
){
    val dark = isSystemInDarkTheme()
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary

    val pendingCount = visits.count { !isSynced(it.syncStatus) && !isFailed(it.syncStatus) }
    val failedCount = visits.count { isFailed(it.syncStatus) }

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {
        // Header
        Text(
            text = "Visit History",
            style = MaterialTheme.typography.headlineMedium,
            color = primary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "Field visits saved on this device.",
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary
        )

        if (visits.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                CountChip(
                    text = if (visits.size == 1) "1 visit" else "${visits.size} visits",
                    color = primary
                )
                if (pendingCount > 0) {
                    CountChip(
                        text = "$pendingCount pending",
                        color = if (dark) StatusWarning else Color(0xFF8A5A00)
                    )
                }
                if (failedCount > 0) {
                    CountChip(text = "$failedCount failed", color = StatusError)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (visits.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 48.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(
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
                            imageVector = Icons.Default.History,
                            contentDescription = null,
                            modifier = Modifier.size(36.dp),
                            tint = primary
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Text(
                        text = "No field visits recorded yet.",
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = "Your saved field visits will appear here.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                visits.forEach { visit ->
                    key(visit.visitId) {
                        VisitHistoryCard(
                            visit = visit,
                            householdCode = households.find {
                                it.householdId == visit.householdId
                            }?.householdCode ?: "Unknown household",
                            onClick = { onVisitClick(visit) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
private fun CountChip(text: String, color: Color) {
    Text(
        text = text,
        fontFamily = Inter,
        fontWeight = FontWeight.SemiBold,
        fontSize = 12.sp,
        color = color,
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    )
}

@Composable
private fun VisitHistoryCard(
    visit: Visit,
    householdCode: String,
    onClick: () -> Unit
) {
    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)

    val date = SimpleDateFormat(
        "MMM dd, yyyy",
        Locale.getDefault()
    ).format(Date(visit.visitDate))

    // Same three groups as before: synced, failed, and everything else (pending)
    val statusColor = when {
        isSynced(visit.syncStatus) -> if (dark) GreenPrimaryDark else Color(0xFF256B00)
        isFailed(visit.syncStatus) -> StatusError
        else -> if (dark) StatusWarning else Color(0xFF8A5A00)
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, top = 14.dp, bottom = 14.dp, end = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = visit.visitCategory,
                        style = MaterialTheme.typography.titleMedium,
                        color = textPrimary,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Text(
                        text = visit.syncStatus.uppercase(),
                        fontFamily = Inter,
                        fontWeight = FontWeight.Bold,
                        fontSize = 11.sp,
                        letterSpacing = 0.5.sp,
                        color = statusColor,
                        modifier = Modifier
                            .clip(RoundedCornerShape(999.dp))
                            .background(statusColor.copy(alpha = 0.12f))
                            .padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Date: $date",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Home,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Household: $householdCode",
                        fontFamily = Inter,
                        fontSize = 13.sp,
                        color = textSecondary
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = textSecondary
            )
        }
    }
}