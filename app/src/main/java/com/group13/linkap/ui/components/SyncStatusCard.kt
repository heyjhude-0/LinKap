package com.group13.linkap.ui.components

import android.widget.Toast
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
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.local.entity.FollowUp
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.sync.ConnectivityObserver
import com.group13.linkap.sync.FirestoreSyncEngine
import com.group13.linkap.sync.SyncManager
import com.group13.linkap.sync.SyncPreferences
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
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun SyncStatusCard(
    households: List<Household>,
    residents: List<Resident>,
    visits: List<Visit>,
    followUps: List<FollowUp>,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val connectivityObserver = remember { ConnectivityObserver(context) }
    val isConnected by connectivityObserver.isConnected.collectAsState(initial = false)

    val syncPreferences = remember { SyncPreferences(context) }
    var lastSyncTime by remember { mutableStateOf(syncPreferences.getLastSyncTime()) }
    var isManualSyncing by remember { mutableStateOf(false) }

    // Auto-trigger sync when connectivity turns online
    LaunchedEffect(isConnected) {
        if (isConnected) {
            SyncManager(context).scheduleSync()
        }
    }

    val pendingHouseholds = households.count { it.syncStatus == "PENDING" }
    val pendingResidents = residents.count { it.syncStatus == "PENDING" }
    val pendingVisits = visits.count { it.syncStatus == "PENDING" }
    val pendingFollowUps = followUps.count { it.syncStatus == "PENDING" }

    val totalPending = pendingHouseholds + pendingResidents + pendingVisits + pendingFollowUps
    val failedCount = households.count { it.syncStatus == "FAILED" } +
            residents.count { it.syncStatus == "FAILED" } +
            visits.count { it.syncStatus == "FAILED" } +
            followUps.count { it.syncStatus == "FAILED" }

    val dark = isSystemInDarkTheme()
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val onPrimary = if (dark) BackgroundDark else Color.White
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary
    val border = if (dark) Color(0xFF2A3A30) else Color(0xFFD5E5DB)
    val success = if (dark) GreenPrimaryDark else Color(0xFF256B00)

    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isConnected) Icons.Default.CloudDone else Icons.Default.CloudOff,
                            contentDescription = null,
                            tint = if (isConnected) primary else StatusWarning,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "Cloud Sync",
                            style = MaterialTheme.typography.titleMedium,
                            color = textPrimary
                        )

                        Text(
                            text = if (isConnected) "Online · Connected" else "Offline · No Internet",
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = if (isConnected) success else StatusWarning
                        )
                    }
                }

                // Badge showing Sync Status
                val statusText: String
                val statusBg: Color
                val statusFg: Color

                when {
                    isManualSyncing -> {
                        statusText = "Syncing..."
                        statusBg = primary.copy(alpha = 0.12f)
                        statusFg = primary
                    }
                    totalPending > 0 -> {
                        statusText = "$totalPending Pending"
                        statusBg = StatusWarning.copy(alpha = 0.12f)
                        statusFg = StatusWarning
                    }
                    failedCount > 0 -> {
                        statusText = "$failedCount Failed"
                        statusBg = StatusError.copy(alpha = 0.12f)
                        statusFg = StatusError
                    }
                    else -> {
                        statusText = "All Synced"
                        statusBg = success.copy(alpha = 0.12f)
                        statusFg = success
                    }
                }

                Text(
                    text = statusText,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = statusFg,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(statusBg)
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            val lastSyncFormatted = if (lastSyncTime > 0) {
                SimpleDateFormat("MMM dd, yyyy · hh:mm a", Locale.getDefault())
                    .format(Date(lastSyncTime))
            } else {
                "Never"
            }

            Text(
                text = "Last synced: $lastSyncFormatted",
                fontFamily = Inter,
                fontSize = 12.sp,
                color = textSecondary
            )

            Button(
                onClick = {
                    if (!isConnected) {
                        Toast.makeText(context, "No internet connection. Sync requires internet.", Toast.LENGTH_SHORT).show()
                    } else {
                        isManualSyncing = true
                        scope.launch {
                            val engine = FirestoreSyncEngine(context)
                            val res = engine.performSync()
                            isManualSyncing = false
                            lastSyncTime = syncPreferences.getLastSyncTime()

                            if (res.isSuccess) {
                                Toast.makeText(context, "Synchronization completed successfully!", Toast.LENGTH_SHORT).show()
                            } else {
                                Toast.makeText(context, "Sync failed: ${res.exceptionOrNull()?.message ?: "Unknown error"}", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                },
                enabled = !isManualSyncing,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(44.dp),
                shape = RoundedCornerShape(10.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = primary,
                    contentColor = onPrimary
                )
            ) {
                if (isManualSyncing) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(18.dp),
                        color = onPrimary,
                        strokeWidth = 2.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Syncing with cloud...",
                        style = MaterialTheme.typography.labelMedium
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Sync,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Sync Now",
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}
