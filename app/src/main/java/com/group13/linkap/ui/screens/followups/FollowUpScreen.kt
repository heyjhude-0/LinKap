package com.group13.linkap.ui.screens.followups

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.group13.linkap.data.local.entity.FollowUp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.ui.theme.BackgroundDark
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

/**
 * Adds no outer padding and does not scroll by itself. Show it inside the HomeScreen
 * content column (20dp margin, vertical scroll), like HouseholdScreen.
 */
@Composable
fun FollowUpScreen(
    followUps: List<FollowUp>,
    households: List<Household>,
    onAddClick: () -> Unit,
    onComplete: (FollowUp) -> Unit,
    onDelete: (FollowUp) -> Unit
) {
    var followUpToDelete by remember { mutableStateOf<FollowUp?>(null) }

    val currentTime = System.currentTimeMillis()

    val pending = followUps.count {
        it.status == "PENDING" && it.dueDate >= currentTime
    }

    val overdue = followUps.count {
        it.status == "PENDING" && it.dueDate < currentTime
    }

    val completed = followUps.count {
        it.status == "COMPLETED"
    }

    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val onPrimary = if (dark) BackgroundDark else Color.White
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)
    val pendingColor = if (dark) StatusWarning else Color(0xFF8A5A00)
    val completedColor = if (dark) GreenPrimaryDark else Color(0xFF256B00)

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Header
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "Follow-up Management",
                style = MaterialTheme.typography.headlineMedium,
                color = primary
            )

            Text(
                text = "Monitor and manage scheduled household follow-ups.",
                style = MaterialTheme.typography.bodyMedium,
                color = textSecondary
            )
        }

        Button(
            onClick = onAddClick,
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
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(22.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = "Add Follow-up",
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FollowUpStatCard("Pending", pending, pendingColor, surface, border,
                textPrimary, textSecondary, Modifier.weight(1f))
            FollowUpStatCard("Overdue", overdue, StatusError, surface, border,
                textPrimary, textSecondary, Modifier.weight(1f))
            FollowUpStatCard("Completed", completed, completedColor, surface, border,
                textPrimary, textSecondary, Modifier.weight(1f))
        }

        Text(
            text = "All Follow-ups (${followUps.size})",
            style = MaterialTheme.typography.titleMedium,
            color = textPrimary
        )

        if (followUps.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(64.dp)
                        .clip(CircleShape)
                        .background(primary.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = null,
                        tint = primary,
                        modifier = Modifier.size(32.dp)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "No follow-up tasks yet.",
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimary,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Tap Add Follow-up to schedule one.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary,
                    textAlign = TextAlign.Center
                )
            }
        } else {
            followUps.forEach { followUp ->
                val householdCode = households.find {
                    it.householdId == followUp.householdId
                }?.householdCode ?: "Unknown household"

                FollowUpCard(
                    followUp = followUp,
                    householdCode = householdCode,
                    isOverdue = followUp.status == "PENDING" &&
                            followUp.dueDate < System.currentTimeMillis(),
                    surface = surface,
                    primary = primary,
                    onPrimary = onPrimary,
                    border = border,
                    textPrimary = textPrimary,
                    textSecondary = textSecondary,
                    pendingColor = pendingColor,
                    completedColor = completedColor,
                    onComplete = {
                        onComplete(
                            followUp.copy(
                                status = "COMPLETED",
                                updatedAt = System.currentTimeMillis()
                            )
                        )
                    },
                    onDeleteClick = {
                        followUpToDelete = followUp
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }

    followUpToDelete?.let { followUp ->
        AlertDialog(
            onDismissRequest = { followUpToDelete = null },
            containerColor = surface,
            shape = RoundedCornerShape(20.dp),
            title = {
                Text(
                    text = "Delete Follow-up",
                    style = MaterialTheme.typography.titleLarge,
                    color = textPrimary
                )
            },
            text = {
                Text(
                    text = "Are you sure you want to delete \"${followUp.title}\"? This action cannot be undone.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = textSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onDelete(followUp)
                        followUpToDelete = null
                    }
                ) {
                    Text(
                        text = "Delete",
                        color = StatusError,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            },
            dismissButton = {
                TextButton(
                    onClick = { followUpToDelete = null }
                ) {
                    Text(
                        text = "Cancel",
                        color = primary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        )
    }
}

@Composable
private fun FollowUpStatCard(
    label: String,
    count: Int,
    accent: Color,
    surface: Color,
    border: Color,
    textPrimary: Color,
    textSecondary: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = label,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 12.sp,
                    color = textSecondary,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = count.toString(),
                style = MaterialTheme.typography.headlineLarge,
                color = textPrimary
            )
        }
    }
}

@Composable
private fun FollowUpCard(
    followUp: FollowUp,
    householdCode: String,
    isOverdue: Boolean,
    surface: Color,
    primary: Color,
    onPrimary: Color,
    border: Color,
    textPrimary: Color,
    textSecondary: Color,
    pendingColor: Color,
    completedColor: Color,
    onComplete: () -> Unit,
    onDeleteClick: () -> Unit
) {
    val dateFormat = SimpleDateFormat(
        "MMM dd, yyyy",
        Locale.getDefault()
    )

    val dueDate = dateFormat.format(Date(followUp.dueDate))

    val status = when {
        followUp.status == "COMPLETED" -> "Completed"
        isOverdue -> "Overdue"
        else -> "Pending"
    }

    val statusColor = when (status) {
        "Completed" -> completedColor
        "Overdue" -> StatusError
        else -> pendingColor
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(16.dp),
        // Overdue cards get a red outline so they stand out in the list
        border = BorderStroke(
            width = if (isOverdue) 1.5.dp else 1.dp,
            color = if (isOverdue) StatusError.copy(alpha = 0.6f) else border
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(start = 16.dp, top = 8.dp, end = 8.dp, bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(modifier = Modifier.weight(1f)) {
                    Text(
                        text = householdCode,
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

                IconButton(
                    onClick = onDeleteClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Delete Follow-up",
                        tint = StatusError
                    )
                }
            }

            Column(
                modifier = Modifier.padding(end = 8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                Text(
                    text = followUp.title,
                    style = MaterialTheme.typography.titleMedium,
                    color = textPrimary
                )

                followUp.description?.let {
                    Text(
                        text = it,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Due: $dueDate",
                            fontFamily = Inter,
                            fontSize = 13.sp,
                            color = textSecondary
                        )
                    }

                    Text(
                        text = status.uppercase(),
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

                if (followUp.status == "PENDING") {
                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = onComplete,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Mark as Completed",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}