package com.group13.linkap.ui.screens.home

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.group13.linkap.R
import com.group13.linkap.data.local.entity.Household
import com.group13.linkap.data.local.entity.Resident
import com.group13.linkap.data.local.entity.Visit
import com.group13.linkap.di.AppModule
import com.group13.linkap.ui.components.BottomNavItem
import com.group13.linkap.ui.components.BottomNavigationBar
import com.group13.linkap.ui.components.LinKapTopBar
import com.group13.linkap.ui.components.SecondaryTopBar
import com.group13.linkap.ui.screens.households.EditResidentScreen
import com.group13.linkap.ui.screens.households.EditResidentViewModel
import com.group13.linkap.ui.screens.households.EditResidentViewModelFactory
import com.group13.linkap.ui.screens.households.HouseholdDetailsScreen
import com.group13.linkap.ui.screens.households.HouseholdScreen
import com.group13.linkap.ui.screens.households.HouseholdViewModel
import com.group13.linkap.ui.screens.households.HouseholdViewModelFactory
import com.group13.linkap.ui.screens.households.RegisterHouseholdScreen
import com.group13.linkap.ui.screens.households.RegisterHouseholdViewModel
import com.group13.linkap.ui.screens.households.RegisterHouseholdViewModelFactory
import com.group13.linkap.ui.screens.households.RegisterResidentScreen
import com.group13.linkap.ui.screens.households.RegisterResidentViewModel
import com.group13.linkap.ui.screens.households.RegisterResidentViewModelFactory
import com.group13.linkap.ui.screens.scanner.QrScannerScreen
import com.group13.linkap.ui.screens.visits.FieldVisitScreen
import com.group13.linkap.ui.screens.visits.FieldVisitViewModel
import com.group13.linkap.ui.screens.visits.FieldVisitViewModelFactory
import com.group13.linkap.ui.screens.visits.VisitDetailsScreen
import com.group13.linkap.ui.screens.visits.VisitDetailsViewModel
import com.group13.linkap.ui.screens.visits.VisitDetailsViewModelFactory
import com.group13.linkap.ui.screens.visits.VisitHistoryScreen
import com.group13.linkap.ui.screens.visits.VisitHistoryViewModel
import com.group13.linkap.ui.screens.visits.VisitHistoryViewModelFactory
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.BackgroundLight
import com.group13.linkap.ui.theme.GreenPrimary
import com.group13.linkap.ui.theme.GreenPrimaryDark
import com.group13.linkap.ui.theme.GreenSecondaryDark
import com.group13.linkap.ui.theme.Inter
import com.group13.linkap.ui.theme.StatusError
import com.group13.linkap.ui.theme.StatusInfo
import com.group13.linkap.ui.theme.StatusWarning
import com.group13.linkap.ui.theme.SurfaceDark
import com.group13.linkap.ui.theme.SurfaceLight
import com.group13.linkap.ui.theme.TextPrimary
import com.group13.linkap.ui.theme.TextPrimaryDark
import com.group13.linkap.ui.theme.TextSecondary
import com.group13.linkap.ui.theme.TextSecondaryDark
import java.util.Calendar

// Colors that switch between light and dark, taken from the LinKap theme
private class HomeColors(
    val background: Color,
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
private fun rememberHomeColors(): HomeColors {
    val dark = isSystemInDarkTheme()
    return remember(dark) {
        if (dark) {
            HomeColors(
                background = BackgroundDark,
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
            HomeColors(
                background = BackgroundLight,
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
}

@Composable
fun HomeScreen(
    onLogout: () -> Unit
) {
    var selectedItem by remember {
        mutableStateOf(BottomNavItem.HOME)
    }
    var showScanner by remember {
        mutableStateOf(false)
    }
    var scannedHouseholdId by remember {
        mutableStateOf<String?>(null)
    }
    var visitHouseholdId by remember { mutableStateOf<String?>(null) }
    var isVisitScan by remember { mutableStateOf(false) }
    var selectedHouseholdDetails by remember {
        mutableStateOf<Household?>(null)
    }
    var showRegisterResident by remember {
        mutableStateOf(false)
    }
    var selectedResident by remember { mutableStateOf<Resident?>(null) }
    var showEditResident by remember { mutableStateOf(false) }
    var selectedVisit by remember {
        mutableStateOf<Visit?>(null)
    }
    val context = LocalContext.current

    val householdViewModel: HouseholdViewModel = viewModel(
        factory = HouseholdViewModelFactory(
            AppModule.provideHouseholdRepository(context),
            AppModule.provideResidentRepository(context)
        )
    )
    val registerHouseholdViewModel: RegisterHouseholdViewModel = viewModel(
        factory = RegisterHouseholdViewModelFactory(
            AppModule.provideHouseholdRepository(context)
        )
    )
    val registerResidentViewModel: RegisterResidentViewModel = viewModel(
        factory = RegisterResidentViewModelFactory(
            AppModule.provideResidentRepository(context)
        )
    )
    val editResidentViewModel: EditResidentViewModel = viewModel(
        factory = EditResidentViewModelFactory(
            AppModule.provideResidentRepository(context)
        )
    )
    val fieldVisitViewModel: FieldVisitViewModel = viewModel(
        factory = FieldVisitViewModelFactory(
            AppModule.provideVisitRepository(context)
        )
    )
    val visitDetailsViewModel: VisitDetailsViewModel = viewModel(
        factory = VisitDetailsViewModelFactory(
            AppModule.provideVisitRepository(context)
        )
    )
    val selectedHousehold by householdViewModel.selectedHousehold.collectAsState()
    val selectedResidents by householdViewModel.selectedResidents.collectAsState()
    val households by householdViewModel.households.collectAsState(
        initial = emptyList()
    )
    val isSavingVisit by fieldVisitViewModel.isSaving.collectAsState()
    val visitSaveMessage by fieldVisitViewModel.saveMessage.collectAsState()
    var showRegisterHousehold by remember {
        mutableStateOf(false)
    }
    val visitResidents by visitDetailsViewModel.residents.collectAsState()
    val isLoadingVisitResidents by visitDetailsViewModel.isLoading.collectAsState()

    val visitHistoryViewModel: VisitHistoryViewModel = viewModel(
        factory = VisitHistoryViewModelFactory(
            AppModule.provideVisitRepository(context)
        )
    )

    val visitHistory by visitHistoryViewModel.visits.collectAsState(
        initial = emptyList()
    )

    LaunchedEffect(visitHistory) {
        android.util.Log.d(
            "VISIT_HISTORY",
            "Retrieved visits: ${visitHistory.size}"
        )
    }
    var isRegistering by remember { mutableStateOf(false) }
    var registrationMessage by remember { mutableStateOf<String?>(null) }
    var registrationSuccess by remember { mutableStateOf(false) }
    var showVisitHistory by remember {
        mutableStateOf(false)
    }
    val colors = rememberHomeColors()

    // Closing the visit details page. Opening a visit loads its household into the same
    // selectedHousehold the Record Visit form uses, so reload the household that form had.
    val closeVisitDetails = {
        selectedVisit = null
        visitHouseholdId?.let { householdViewModel.loadHousehold(it) }
    }

    Scaffold(
        containerColor = colors.background,
        topBar = {
            Surface(
                color = colors.surface,
                shadowElevation = 1.dp
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp)
                ) {
                    if (showScanner) {
                        SecondaryTopBar(
                            title = "Scan Household QR",
                            onBackClick = { showScanner = false }
                        )
                    } else if (showRegisterHousehold) {
                        SecondaryTopBar(
                            title = "Register Household",
                            onBackClick = { showRegisterHousehold = false }
                        )
                    } else if (showEditResident) {
                        SecondaryTopBar(
                            title = "Edit Resident",
                            onBackClick = {
                                showEditResident = false
                                selectedResident = null
                            }
                        )
                    } else if (showRegisterResident) {
                        SecondaryTopBar(
                            title = "Register Resident",
                            onBackClick = { showRegisterResident = false }
                        )
                    } else if (selectedHouseholdDetails != null) {
                        SecondaryTopBar(
                            title = "Household Details",
                            onBackClick = { selectedHouseholdDetails = null }
                        )
                    } else if (selectedVisit != null) {
                        SecondaryTopBar(
                            title = "Visit Details",
                            onBackClick = { closeVisitDetails() }
                        )
                    } else {
                        LinKapTopBar()
                    }
                }
            }
        },
        bottomBar = {
            if (!showScanner &&
                !showRegisterHousehold &&
                !showRegisterResident &&
                !showEditResident &&
                selectedHouseholdDetails == null &&
                selectedVisit == null
            ) {
                BottomNavigationBar(
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it }
                )
            }
        }
    ) { innerPadding ->

        if (showScanner) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                QrScannerScreen(
                    onBackClick = { showScanner = false },
                    onQrCodeScanned = { qrValue ->
                        householdViewModel.loadHousehold(qrValue)
                        showScanner = false

                        if (isVisitScan) {
                            visitHouseholdId = qrValue
                            scannedHouseholdId = null
                        } else {
                            scannedHouseholdId = qrValue
                        }
                    }
                )
            }
        } else if (showEditResident && selectedResident != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                EditResidentScreen(
                    resident = selectedResident!!,
                    onSaveClick = { firstName, middleName, lastName, birthDate, sex, relationship ->

                        editResidentViewModel.updateResident(
                            resident = selectedResident!!,
                            firstName = firstName,
                            middleName = middleName,
                            lastName = lastName,
                            birthDate = birthDate,
                            sex = sex,
                            relationship = relationship
                        ) { success, message ->

                            Toast.makeText(
                                context,
                                message,
                                Toast.LENGTH_SHORT
                            ).show()

                            if (success) {
                                showEditResident = false
                                selectedResident = null
                            }
                        }
                    }
                )
            }
        } else if (showRegisterHousehold) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                RegisterHouseholdScreen(
                    onBackClick = {
                        showRegisterHousehold = false
                    },
                    isLoading = isRegistering,
                    resultMessage = registrationMessage,
                    isSuccess = registrationSuccess,
                    onContinueClick = { householdCode, purok, street, barangay ->

                        isRegistering = true
                        registrationMessage = null

                        registerHouseholdViewModel.registerHousehold(
                            householdCode = householdCode,
                            purok = purok,
                            street = street,
                            barangay = barangay
                        ) { success, message ->

                            isRegistering = false
                            registrationSuccess = success
                            registrationMessage = message
                        }
                    },
                    onResetForm = {
                        registrationMessage = null
                        registrationSuccess = false
                    },
                )
            }
        } else if (showRegisterResident) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                RegisterResidentScreen(
                    onSaveClick = { firstName, middleName, lastName, birthDate, sex, relationship ->

                        val householdId = selectedHouseholdDetails?.householdId

                        if (householdId != null) {
                            registerResidentViewModel.registerResident(
                                householdId = householdId,
                                firstName = firstName,
                                middleName = middleName,
                                lastName = lastName,
                                birthDate = birthDate,
                                sex = sex,
                                relationship = relationship
                            ) { success, message ->

                                Toast.makeText(
                                    context,
                                    message,
                                    Toast.LENGTH_SHORT
                                ).show()

                                if (success) {
                                    showRegisterResident = false
                                }
                            }
                        }
                    }
                )
            }
        } else if (selectedHouseholdDetails != null) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                HouseholdDetailsScreen(
                    household = selectedHouseholdDetails!!,
                    residents = selectedResidents,
                    onBackClick = {
                        selectedHouseholdDetails = null
                    },
                    onAddResidentClick = {
                        showRegisterResident = true
                    },
                    onEditResidentClick = { resident ->
                        selectedResident = resident
                        showEditResident = true
                    }
                )
            }
        } else if (selectedVisit != null) {
            // Its own page, outside the scrolling column below. VisitDetailsScreen scrolls
            // by itself, so showing it inside that column caused the infinite-height crash.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                VisitDetailsScreen(
                    visit = selectedVisit!!,
                    household = selectedHousehold?.takeIf {
                        it.householdId == selectedVisit?.householdId
                    },
                    residents = visitResidents,
                    isLoadingResidents = isLoadingVisitResidents,
                    onBackClick = { closeVisitDetails() }
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                // Scanned household details are shown first so they are not hidden below a long list
                scannedHouseholdId?.let {
                    ScannedHouseholdCard(
                        household = selectedHousehold?.let { h ->
                            ScannedHouseholdInfo(
                                code = h.householdCode,
                                purok = h.purok.toString(),
                                street = h.street ?: "Not specified",
                                barangay = h.barangay.toString()
                            )
                        },
                        onClose = { scannedHouseholdId = null },
                        colors = colors
                    )
                    Spacer(modifier = Modifier.height(20.dp))
                }

                when (selectedItem) {

                    BottomNavItem.HOME -> {
                        DashboardContent(
                            onRegisterHouseholdClick = { showRegisterHousehold = true },
                            onScanClick = {
                                isVisitScan = true
                                showScanner = true
                            },
                            colors = colors
                        )
                    }

                    BottomNavItem.HOUSEHOLDS -> HouseholdScreen(
                        households = households,
                        onScanClick = {
                            isVisitScan = false
                            showScanner = true
                        },
                        onHouseholdClick = { household ->
                            householdViewModel.loadHousehold(household.householdId)
                            selectedHouseholdDetails = household
                        }
                    )

                    BottomNavItem.VISITS -> {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            VisitTabSwitch(
                                showHistory = showVisitHistory,
                                onSelect = { showVisitHistory = it },
                                colors = colors
                            )

                            if (showVisitHistory) {
                                VisitHistoryScreen(
                                    visits = visitHistory,
                                    households = households,
                                    onVisitClick = { visit ->
                                        selectedVisit = visit
                                        householdViewModel.loadHousehold(visit.householdId)
                                        visitDetailsViewModel.loadResidents(visit.visitId)
                                    }
                                )
                            } else {
                                FieldVisitScreen(
                                    visitHouseholdId = visitHouseholdId,
                                    household = selectedHousehold,
                                    residents = selectedResidents,
                                    onScanClick = {
                                        isVisitScan = true
                                        showScanner = true
                                    },
                                    onSearchClick = {
                                        selectedItem = BottomNavItem.HOUSEHOLDS
                                    },
                                    onSaveVisit = { householdId, residentIds, category, notes, visitDate, latitude, longitude ->
                                        fieldVisitViewModel.saveVisit(
                                            householdId = householdId,
                                            residentIds = residentIds,
                                            category = category,
                                            notes = notes,
                                            visitDate = visitDate,
                                            latitude = latitude,
                                            longitude = longitude
                                        )
                                    },
                                    isSaving = isSavingVisit,
                                    saveMessage = visitSaveMessage,
                                    onMessageShown = { fieldVisitViewModel.clearSaveMessage() }
                                )
                            }
                        }
                    }

                    BottomNavItem.FOLLOW_UPS -> {
                        PlaceholderScreen(
                            title = "Follow-ups",
                            description = "Monitor scheduled household follow-ups.",
                            colors = colors
                        )
                    }

                    BottomNavItem.PROFILE -> {
                        PlaceholderScreen(
                            title = "My Profile",
                            description = "View your BHW account information.",
                            colors = colors
                        )

                        Spacer(modifier = Modifier.height(32.dp))

                        // Log out lives here now that the header no longer has a button for it
                        OutlinedButton(
                            onClick = onLogout,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(52.dp),
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.5.dp, colors.primary)
                        ) {
                            Text(
                                text = "Log out",
                                color = colors.primary,
                                style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                            )
                        }
                    }
                }
            }
        }
    }
}

private data class ScannedHouseholdInfo(
    val code: String,
    val purok: String,
    val street: String,
    val barangay: String
)

private fun greeting(): String {
    val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    return when {
        hour < 12 -> "GOOD MORNING"
        hour < 18 -> "GOOD AFTERNOON"
        else -> "GOOD EVENING"
    }
}

/** Two-part switch for the Visits tab: Record Visit and Visit History. */
@Composable
private fun VisitTabSwitch(
    showHistory: Boolean,
    onSelect: (Boolean) -> Unit,
    colors: HomeColors
) {
    val shape = RoundedCornerShape(14.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(colors.surface)
            .border(1.dp, colors.border, shape)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        listOf("Record Visit" to false, "Visit History" to true).forEach { (label, isHistory) ->
            val selected = showHistory == isHistory

            Box(
                modifier = Modifier
                    .weight(1f)
                    .height(44.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (selected) colors.primary else Color.Transparent)
                    .selectable(
                        selected = selected,
                        role = Role.Tab,
                        onClick = { onSelect(isHistory) }
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 14.sp,
                    color = if (selected) colors.onPrimary else colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun ScannedHouseholdCard(
    household: ScannedHouseholdInfo?,
    onClose: () -> Unit,
    colors: HomeColors
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.primary.copy(alpha = 0.5f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconBadge(Icons.Default.Check, colors.primary)
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Household Details",
                    style = MaterialTheme.typography.titleLarge,
                    color = colors.textPrimary
                )
            }

            if (household != null) {
                DetailRow("Household Code", household.code, colors)
                DetailRow("Purok", household.purok, colors)
                DetailRow("Street", household.street, colors)
                DetailRow("Barangay", household.barangay, colors)
            } else {
                Text(
                    text = "Household not found in the local database.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }

            TextButton(
                onClick = onClose,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(
                    text = "Close",
                    color = colors.primary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

@Composable
private fun DetailRow(label: String, value: String, colors: HomeColors) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = label,
            fontFamily = Inter,
            fontWeight = FontWeight.Medium,
            fontSize = 13.sp,
            color = colors.textSecondary,
            modifier = Modifier.weight(0.42f)
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textPrimary,
            modifier = Modifier.weight(0.58f)
        )
    }
}

@Composable
private fun DashboardContent(
    onRegisterHouseholdClick: () -> Unit,
    onScanClick: () -> Unit,
    colors: HomeColors
) {
    Column {

        // Welcome card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            color = colors.heroBackground
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = greeting(),
                    fontFamily = Inter,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    letterSpacing = 1.sp,
                    color = colors.heroText.copy(alpha = 0.85f)
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Welcome back!",
                    style = MaterialTheme.typography.headlineLarge,
                    color = colors.heroText
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "Barangay Alangilan, Batangas City",
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.heroText.copy(alpha = 0.9f)
                )
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Overview
        SectionTitle("Your Overview", colors)

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard("Households", "128", colors.primary, colors, Modifier.weight(1f))
            DashboardStatCard("Residents", "356", StatusInfo, colors, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DashboardStatCard("Pending Visits", "8", StatusWarning, colors, Modifier.weight(1f))
            DashboardStatCard("Follow-ups", "5", StatusError, colors, Modifier.weight(1f))
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Quick Actions
        SectionTitle("Quick Actions", colors)

        Spacer(modifier = Modifier.height(12.dp))

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            QuickActionCard(
                title = "Register Household",
                description = "Add a new household and its members.",
                icon = Icons.Default.Add,
                colors = colors,
                onClick = onRegisterHouseholdClick
            )

            QuickActionCard(
                title = "Record Field Visit",
                description = "Document a household health visit.",
                icon = Icons.Default.Edit,
                colors = colors
            )

            QuickActionCard(
                title = "Scan Household QR",
                description = "Quickly access a household profile.",
                icon = Icons.Default.QrCodeScanner,
                colors = colors,
                onClick = onScanClick
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Recent Activity
        SectionTitle("Recent Activity", colors)

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = colors.surface),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, colors.border),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconBadge(Icons.Default.Check, colors.success)

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Household Visit Completed",
                        style = MaterialTheme.typography.titleMedium,
                        color = colors.textPrimary
                    )

                    Text(
                        text = "Household HH-001",
                        style = MaterialTheme.typography.bodyMedium,
                        color = colors.textSecondary
                    )

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = "Today · 9:30 AM",
                        fontFamily = Inter,
                        fontSize = 12.sp,
                        color = colors.textSecondary
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "SYNCED",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.sp,
                    letterSpacing = 0.5.sp,
                    color = colors.success,
                    modifier = Modifier
                        .clip(RoundedCornerShape(999.dp))
                        .background(colors.success.copy(alpha = 0.12f))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

@Composable
private fun SectionTitle(text: String, colors: HomeColors) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleLarge,
        color = colors.textPrimary
    )
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(40.dp)
            .clip(CircleShape)
            .background(tint.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun DashboardStatCard(
    title: String,
    value: String,
    accent: Color,
    colors: HomeColors,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(accent)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = title,
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = colors.textSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineLarge,
                color = colors.textPrimary
            )
        }
    }
}

@Composable
private fun QuickActionCard(
    title: String,
    description: String,
    icon: ImageVector,
    colors: HomeColors,
    onClick: () -> Unit = {}
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = colors.surface),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, colors.border),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconBadge(icon, colors.primary)

            Spacer(modifier = Modifier.width(14.dp))

            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = colors.textPrimary
                )

                Spacer(modifier = Modifier.height(2.dp))

                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyMedium,
                    color = colors.textSecondary
                )
            }
        }
    }
}

@Composable
private fun PlaceholderScreen(
    title: String,
    description: String,
    colors: HomeColors
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Image(
            painter = painterResource(R.drawable.linkap_logo),
            contentDescription = null,
            modifier = Modifier.size(72.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            color = colors.primary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = description,
            style = MaterialTheme.typography.bodyMedium,
            color = colors.textSecondary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "COMING SOON",
            fontFamily = Inter,
            fontWeight = FontWeight.Bold,
            fontSize = 11.sp,
            letterSpacing = 1.sp,
            color = colors.primary,
            modifier = Modifier
                .clip(RoundedCornerShape(999.dp))
                .background(colors.primary.copy(alpha = 0.12f))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}