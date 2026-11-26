package com.group13.linkap.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource

sealed class LocationResult {
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float
    ) : LocationResult()

    data class Error(val message: String) : LocationResult()
}

sealed class LocationState {
    object Idle : LocationState()
    object Loading : LocationState()
    data class Success(
        val latitude: Double,
        val longitude: Double,
        val accuracy: Float
    ) : LocationState()
    data class Error(val message: String) : LocationState()
}

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        return fineLocation || coarseLocation
    }

    fun isLocationServicesEnabled(context: Context): Boolean {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            ?: return false
        val gpsEnabled = try {
            locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        } catch (e: Exception) {
            false
        }
        val networkEnabled = try {
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
        } catch (e: Exception) {
            false
        }
        return gpsEnabled || networkEnabled
    }

    @SuppressLint("MissingPermission")
    fun fetchCurrentLocation(
        context: Context,
        onResult: (LocationResult) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onResult(LocationResult.Error("Location permission is denied."))
            return
        }

        if (!isLocationServicesEnabled(context)) {
            onResult(LocationResult.Error("Device location services are disabled. Please enable GPS in your device settings."))
            return
        }

        try {
            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
            val cancellationTokenSource = CancellationTokenSource()

            val currentLocationRequest = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_HIGH_ACCURACY)
                .setDurationMillis(10000)
                .build()

            fusedLocationClient.getCurrentLocation(currentLocationRequest, cancellationTokenSource.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        processLocation(location, onResult)
                    } else {
                        // Fallback to lastLocation
                        fusedLocationClient.lastLocation
                            .addOnSuccessListener { lastLoc: Location? ->
                                if (lastLoc != null) {
                                    processLocation(lastLoc, onResult)
                                } else {
                                    onResult(LocationResult.Error("Location cannot be retrieved. Please try again in an open area."))
                                }
                            }
                            .addOnFailureListener { e ->
                                onResult(LocationResult.Error("Location cannot be retrieved: ${e.localizedMessage ?: "Unknown error"}"))
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Fallback to lastLocation
                    fusedLocationClient.lastLocation
                        .addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                processLocation(lastLoc, onResult)
                            } else {
                                onResult(LocationResult.Error("Location cannot be retrieved: ${e.localizedMessage ?: "Unknown error"}"))
                            }
                        }
                        .addOnFailureListener {
                            onResult(LocationResult.Error("Location cannot be retrieved: ${e.localizedMessage ?: "Unknown error"}"))
                        }
                }
        } catch (e: Exception) {
            onResult(LocationResult.Error("Failed to access location: ${e.localizedMessage ?: "Unknown error"}"))
        }
    }

    private fun processLocation(
        location: Location,
        onResult: (LocationResult) -> Unit
    ) {
        val accuracy = if (location.hasAccuracy()) location.accuracy else 999f
        if (location.hasAccuracy() && accuracy > 100f) {
            onResult(
                LocationResult.Error(
                    "Location accuracy is insufficient (${accuracy.toInt()}m). Please move to an open area and try again."
                )
            )
        } else {
            onResult(
                LocationResult.Success(
                    latitude = location.latitude,
                    longitude = location.longitude,
                    accuracy = accuracy
                )
            )
        }
    }
}
