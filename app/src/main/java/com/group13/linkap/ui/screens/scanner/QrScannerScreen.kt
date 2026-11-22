package com.group13.linkap.ui.screens.scanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.group13.linkap.ui.theme.BackgroundDark
import com.group13.linkap.ui.theme.BackgroundLight
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
 * Uses the existing CameraPreview(modifier, onQrCodeDetected) composable from this package.
 * The back button and title come from the host app bar (SecondaryTopBar in HomeScreen),
 * so this screen has no header of its own. onBackClick is kept so existing calls compile.
 */
@Composable
fun QrScannerScreen(
    onBackClick: () -> Unit,
    onQrCodeScanned: (String) -> Unit = {}
) {
    val context = LocalContext.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            permissionLauncher.launch(Manifest.permission.CAMERA)
        }
    }

    // Ignore repeated detections of the same code
    var lastScanned by remember { mutableStateOf<String?>(null) }

    val dark = isSystemInDarkTheme()
    val background = if (dark) BackgroundDark else BackgroundLight
    val surface = if (dark) SurfaceDark else SurfaceLight
    val primary = if (dark) GreenPrimaryDark else GreenPrimary
    val onPrimary = if (dark) BackgroundDark else Color.White
    val textPrimary = if (dark) TextPrimaryDark else TextPrimary
    val textSecondary = if (dark) TextSecondaryDark else TextSecondary

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(
            text = "Position the household QR code inside the scanner frame.",
            style = MaterialTheme.typography.bodyMedium,
            color = textSecondary
        )

        if (hasCameraPermission) {
            // Camera with scan frame
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(3f / 4f)
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black)
            ) {
                CameraPreview(
                    modifier = Modifier.fillMaxSize(),
                    onQrCodeDetected = { qrValue ->
                        if (qrValue != lastScanned) {
                            lastScanned = qrValue
                            // We'll handle the detected QR code next.
                            onQrCodeScanned(qrValue)
                        }
                    }
                )

                ScannerFrameOverlay(modifier = Modifier.fillMaxSize())

                Text(
                    text = "Align the QR code inside the frame",
                    fontFamily = Inter,
                    fontWeight = FontWeight.Medium,
                    fontSize = 13.sp,
                    color = Color.White,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 16.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(Color.Black.copy(alpha = 0.55f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                )
            }

            // Scan status
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = surface
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (lastScanned == null) textSecondary else primary)
                    )
                    Text(
                        text = if (lastScanned == null) "Scanning for a QR code..." else "QR code detected",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textPrimary,
                        modifier = Modifier.padding(start = 12.dp)
                    )
                }
            }
        } else {
            // Permission needed
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                color = surface
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(primary.copy(alpha = 0.12f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCodeScanner,
                            contentDescription = null,
                            tint = primary,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    Text(
                        text = "Camera access needed",
                        style = MaterialTheme.typography.titleLarge,
                        color = textPrimary,
                        textAlign = TextAlign.Center
                    )

                    Text(
                        text = "Camera permission is required to scan QR codes.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = textSecondary,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    Button(
                        onClick = { permissionLauncher.launch(Manifest.permission.CAMERA) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = primary,
                            contentColor = onPrimary
                        )
                    ) {
                        Text(
                            text = "Allow camera",
                            style = MaterialTheme.typography.labelLarge.copy(fontSize = 16.sp)
                        )
                    }

                    // If the permission was denied permanently, the system dialog will not
                    // appear again, so offer a shortcut to the app settings.
                    TextButton(
                        onClick = {
                            val intent = Intent(
                                Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                                Uri.fromParts("package", context.packageName, null)
                            )
                            context.startActivity(intent)
                        }
                    ) {
                        Text(
                            text = "Open app settings",
                            color = primary,
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
    }
}

/** Four corner brackets that mark where to hold the QR code. */
@Composable
private fun ScannerFrameOverlay(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        val side = size.minDimension * 0.65f
        val left = (size.width - side) / 2f
        val top = (size.height - side) / 2f
        val right = left + side
        val bottom = top + side
        val length = side * 0.2f
        val stroke = 5.dp.toPx()

        fun corner(x: Float, y: Float, dx: Float, dy: Float) {
            drawLine(Color.White, Offset(x, y), Offset(x + dx, y), stroke, StrokeCap.Round)
            drawLine(Color.White, Offset(x, y), Offset(x, y + dy), stroke, StrokeCap.Round)
        }

        corner(left, top, length, length)
        corner(right, top, -length, length)
        corner(left, bottom, length, -length)
        corner(right, bottom, -length, -length)
    }
}