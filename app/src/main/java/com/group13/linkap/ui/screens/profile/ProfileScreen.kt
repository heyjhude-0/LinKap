package com.group13.linkap.ui.screens.profile

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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.group13.linkap.data.model.UserProfile
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.GreenSecondaryDark
import com.group13.linkap.ui.theme.Inter
import com.group13.linkap.ui.theme.StatusError
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark

private class ProfileColors(
    val surface: Color,
    val primary: Color,
    val onPrimary: Color,
    val textPrimary: Color,
    val textSecondary: Color,
    val border: Color,
    val heroBackground: Color,
    val heroText: Color,
    val success: Color
)

@Composable
private fun profileColors(): ProfileColors {
    val dark = isSystemInDarkTheme()
    return if (dark) {
        ProfileColors(
            surface = SurfaceDark,
            primary = GreenPrimaryDark,
            onPrimary = BackgroundDark,
            textPrimary = TextPrimaryDark,
            textSecondary = TextSecondaryDark,
            border = Color(0xFF2A3A30),
            heroBackground = GreenSecondaryDark,
            heroText = TextPrimaryDark,
            success = GreenPrimaryDark
        )
    } else {
        ProfileColors(
            surface = SurfaceLight,
            primary = GreenPrimary,
            onPrimary = Color.White,
            textPrimary = TextPrimary,
            textSecondary = TextSecondary,
            border = Color(0xFFD5E5DB),
            heroBackground = GreenPrimary,
            heroText = Color.White,
            success = Color(0xFF256B00)
        )
    }
}

/**
 * Adds no outer padding and does not scroll by itself. Show it inside the HomeScreen
 * content column (20dp margin, vertical scroll), like the other tabs.
 */
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel,
    onLogout: () -> Unit
) {
    val profile by viewModel.profile.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    val colors = profileColors()

    var showEditDialog by remember { mutableStateOf(false) }
    var editedName by remember { mutableStateOf("") }
    var isSaving by remember { mutableStateOf(false) }
    var editError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        viewModel.loadProfile()
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                text = "My Profile",
                style = MaterialTheme.typography.headlineMedium,
                color = colors.primary
            )
            Text(
                text = "Your BHW account information.",
                style = MaterialTheme.typography.bodyMedium,
                color = colors.textSecondary
            )
        }

        when {
            isLoading && profile == null -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = colors.primary)
                }
            }

            errorMessage != null && profile == null -> {
                ProfileInfoCard(colors) {
                    Text(
                        text = errorMessage ?: "Unable to load profile.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = StatusError
                    )

                    TextButton(onClick = { viewModel.loadProfile() }) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = null,
                            tint = colors.primary
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Retry",
                            color = colors.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }

            profile != null -> {
                val user = profile!!

                ProfileHeader(user, colors)

                ProfileInfoCard(colors) {
                    ProfileSectionTitle("Personal Information", colors)

                    ProfileInfoRow(
                        icon = Icons.Default.Email,
                        label = "Email Address",
                        value = user.email,
                        colors = colors
                    )

                    ProfileDivider(colors)

                    ProfileInfoRow(
                        icon = Icons.Default.LocationOn,
                        label = "Assigned Barangay",
                        value = user.barangayId
                            .replaceFirstChar { it.uppercase() },
                        colors = colors
                    )
                }

                ProfileInfoCard(colors) {
                    ProfileSectionTitle("Account Information", colors)

                    ProfileInfoRow(
                        icon = Icons.Default.Badge,
                        label = "Account Role",
                        value = user.role,
                        colors = colors
                    )

                    ProfileDivider(colors)

                    ProfileInfoRow(
                        icon = Icons.Default.Security,
                        label = "Account Status",
                        value = if (user.isActive) "Active" else "Inactive",
                        colors = colors,
                        badgeColor = if (user.isActive) colors.success else StatusError
                    )

                    ProfileDivider(colors)

                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Account UID",
                            fontFamily = Inter,
                            fontWeight = FontWeight.Medium,
                            fontSize = 13.sp,
                            color = colors.textSecondary
                        )

                        Text(
                            text = user.uid,
                            fontFamily = Inter,
                            fontSize = 12.sp,
                            color = colors.textPrimary
                        )
                    }
                }

                // Main action of the page, shown only once the profile has loaded
                Button(
                    onClick = {
                        editedName = profile?.fullName.orEmpty()
                        editError = null
                        showEditDialog = true
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = colors.primary,
                        contentColor = colors.onPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Edit Profile",
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }

                OutlinedButton(
                    onClick = { viewModel.loadProfile() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    enabled = !isLoading,
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(
                        1.5.dp,
                        if (isLoading) colors.border else colors.primary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.Refresh,
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (isLoading) "Refreshing..." else "Refresh Profile",
                        color = if (isLoading) colors.textSecondary else colors.primary,
                        style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                    )
                }
            }
        }

        OutlinedButton(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .height(52.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.5.dp, StatusError.copy(alpha = 0.7f))
        ) {
            Text(
                text = "Log Out",
                color = StatusError,
                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        if (showEditDialog) {
            // Same save call as before, shared by the button and the keyboard Done key
            val saveName = {
                isSaving = true
                editError = null

                viewModel.updateFullName(editedName) { success, error ->
                    isSaving = false

                    if (success) {
                        showEditDialog = false
                    } else {
                        editError = error
                    }
                }
            }

            AlertDialog(
                onDismissRequest = {
                    if (!isSaving) showEditDialog = false
                },
                containerColor = colors.surface,
                shape = RoundedCornerShape(20.dp),
                title = {
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Edit Profile",
                            style = MaterialTheme.typography.titleLarge,
                            color = colors.textPrimary
                        )
                        Text(
                            text = "Update the name shown on your account.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = colors.textSecondary
                        )
                    }
                },
                text = {
                    OutlinedTextField(
                        value = editedName,
                        onValueChange = {
                            editedName = it
                            editError = null
                        },
                        label = { Text("Full Name") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        enabled = !isSaving,
                        isError = editError != null,
                        supportingText = editError?.let { { Text(it) } },
                        shape = RoundedCornerShape(12.dp),
                        keyboardOptions = KeyboardOptions(
                            capitalization = KeyboardCapitalization.Words,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(
                            onDone = { if (!isSaving) saveName() }
                        ),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedTextColor = colors.textPrimary,
                            unfocusedTextColor = colors.textPrimary,
                            focusedBorderColor = colors.primary,
                            unfocusedBorderColor = colors.border,
                            focusedLabelColor = colors.primary,
                            unfocusedLabelColor = colors.textSecondary,
                            cursorColor = colors.primary,
                            errorBorderColor = StatusError,
                            errorLabelColor = StatusError,
                            errorSupportingTextColor = StatusError,
                            errorTextColor = colors.textPrimary
                        )
                    )
                },
                confirmButton = {
                    Button(
                        enabled = !isSaving,
                        onClick = { saveName() },
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = colors.primary,
                            contentColor = colors.onPrimary,
                            disabledContainerColor = colors.primary.copy(alpha = 0.6f),
                            disabledContentColor = colors.onPrimary
                        )
                    ) {
                        if (isSaving) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(18.dp),
                                color = colors.onPrimary,
                                strokeWidth = 2.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = if (isSaving) "Saving..." else "Save Changes",
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                },
                dismissButton = {
                    TextButton(
                        enabled = !isSaving,
                        onClick = {
                            showEditDialog = false
                        }
                    ) {
                        Text(
                            text = "Cancel",
                            color = if (isSaving) colors.textSecondary else colors.primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            )
        }
    }
}

@Composable
private fun ProfileHeader(user: UserProfile, colors: ProfileColors) {
    val initials = user.fullName
        .trim()
        .split(Regex("\\s+"))
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "B" }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        color = colors.heroBackground
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(84.dp)
                    .clip(CircleShape)
                    .background(colors.heroText.copy(alpha = 0.18f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = initials,
                    color = colors.heroText,
                    style = MaterialTheme.typography.headlineMedium
                )
            }

            Text(
                text = user.fullName.ifBlank { "BHW User" },
                style = MaterialTheme.typography.titleLarge,
                color = colors.heroText
            )

            Text(
                text = user.role,
                fontFamily = Inter,
                fontWeight = FontWeight.SemiBold,
                fontSize = 13.sp,
                color = colors.heroText,
                modifier = Modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(colors.heroText.copy(alpha = 0.18f))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            )
        }
    }
}

@Composable
private fun ProfileInfoCard(
    colors: ProfileColors,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            content = { content() }
        )
    }
}

@Composable
private fun ProfileSectionTitle(title: String, colors: ProfileColors) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = colors.textPrimary
    )
}

@Composable
private fun ProfileDivider(colors: ProfileColors) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(colors.border)
    )
}

@Composable
private fun ProfileInfoRow(
    icon: ImageVector,
    label: String,
    value: String,
    colors: ProfileColors,
    badgeColor: Color? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(colors.primary.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                fontFamily = Inter,
                fontWeight = FontWeight.Medium,
                fontSize = 13.sp,
                color = colors.textSecondary
            )

            val text = value.ifBlank { "Not available" }

            if (badgeColor != null) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = text,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    color = badgeColor,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(badgeColor.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 3.dp)
                )
            } else {
                Text(
                    text = text,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textPrimary,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}