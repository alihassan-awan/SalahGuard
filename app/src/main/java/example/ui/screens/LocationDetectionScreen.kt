package com.example.ui.screens

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.*
import com.example.data.model.SalahGuardStore
import androidx.compose.ui.platform.LocalContext
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import android.Manifest
import android.content.pm.PackageManager
import android.location.LocationManager
import android.location.Geocoder
import android.os.Build
import androidx.core.content.ContextCompat
import java.util.Locale
import com.example.ui.theme.*

@Composable
fun LocationDetectionScreen(
    onNavigateBack: () -> Unit,
    onContinue: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var locationResult by remember { mutableStateOf(SalahGuardStore.city(context)) }
    var busy by remember { mutableStateOf(false) }
    fun requestFix() {
        val manager = context.getSystemService(LocationManager::class.java)
        val provider = when {
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            else -> { locationResult = "Enable location services in Android Settings"; return }
        }
        busy = true
        locationResult = "Getting current location..."
        if (Build.VERSION.SDK_INT >= 30) {
            try {
                manager.getCurrentLocation(provider, null, context.mainExecutor) { location ->
                    busy = false
                    if (location == null) locationResult = "Location unavailable. Please retry."
                    else {
                        val lat = location.latitude
                        val lon = location.longitude
                        Thread {
                            val name = try {
                                @Suppress("DEPRECATION")
                                val address = Geocoder(context, Locale.getDefault())
                                    .getFromLocation(lat, lon, 1)?.firstOrNull()
                                listOfNotNull(address?.locality ?: address?.subAdminArea,
                                    address?.countryName).joinToString(", ").ifBlank { "Coordinates detected" }
                            } catch (_: Exception) { "Coordinates detected" }
                            SalahGuardStore.setCity(context, name, lat, lon)
                            android.os.Handler(android.os.Looper.getMainLooper()).post { locationResult = name }
                        }.start()
                    }
                }
            } catch (_: SecurityException) { busy = false; locationResult = "Location permission required" }
        } else {
            try {
                @Suppress("DEPRECATION")
                val location = manager.getLastKnownLocation(provider)
                busy = false
                if (location == null) locationResult = "No recent location. Retry with GPS enabled."
                else {
                    SalahGuardStore.setCity(context, "Coordinates detected", location.latitude, location.longitude)
                    locationResult = "Coordinates detected"
                }
            } catch (_: SecurityException) { busy = false; locationResult = "Location permission required" }
        }
    }
    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { results ->
        if (results.values.any { it }) requestFix()
        else locationResult = "Permission denied. You can continue with manual times."
    }
    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED) requestFix()
        else permissionLauncher.launch(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
            Manifest.permission.ACCESS_FINE_LOCATION))
    }
    val scrollState = rememberScrollState()

    // Pulse animation for location radar
    val infiniteTransition = rememberInfiniteTransition(label = "radar")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = EaseInOutQuad),
            repeatMode = RepeatMode.Reverse
        ),
        label = "radarScale"
    )

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "",
                onBackClick = onNavigateBack,
                onQuickSwitchClick = onQuickSwitchClick
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SalahDarkBg)
                    .padding(16.dp)
                    .navigationBarsPadding()
            ) {
                SalahPrimaryButton(
                    text = "Continue",
                    onClick = onContinue,
                    testTag = "location_continue_button"
                )
            }
        },
        modifier = modifier.testTag("location_detection_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Heading
            Text(
                text = "Detecting Your Location",
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = SalahTextWhite,
                    fontSize = 24.sp
                ),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Concentric Glowing Radar Circles with Location Pin
            Box(
                modifier = Modifier
                    .size(190.dp)
                    .scale(pulseScale),
                contentAlignment = Alignment.Center
            ) {
                // Outermost subtle ring
                Box(
                    modifier = Modifier
                        .size(180.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF0F3139).copy(alpha = 0.35f))
                        .border(1.dp, Color(0xFF164E5B).copy(alpha = 0.5f), CircleShape)
                )

                // Middle ring
                Box(
                    modifier = Modifier
                        .size(136.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.radialGradient(
                                colors = listOf(
                                    Color(0xFF0D434D),
                                    Color(0xFF0A2B32)
                                )
                            )
                        )
                        .border(1.5.dp, Color(0xFF147A8A), CircleShape)
                )

                // Inner bright circle with location pin
                Box(
                    modifier = Modifier
                        .size(86.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                colors = listOf(
                                    Color(0xFF00B37E),
                                    Color(0xFF0A5866)
                                )
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location Pin",
                        tint = SalahTextWhite,
                        modifier = Modifier.size(46.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Subtitle
            Text(
                text = if (busy) "Getting your current location..." else locationResult,
                style = MaterialTheme.typography.bodyMedium.copy(
                    color = SalahTextSecondary,
                    fontSize = 15.sp
                )
            )

            Spacer(modifier = Modifier.height(30.dp))

            // Step-by-step detection checklist (matching mockup)
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                LocationCheckItem(title = "Checking GPS")
                LocationCheckItem(title = "Retrieving coordinates")
                LocationCheckItem(title = "Finding your city")
                LocationCheckItem(title = "Manual prayer times (calculation not connected)")
            }

            Spacer(modifier = Modifier.height(32.dp))

            // Bottom Success Card (matches mockup)
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .border(1.dp, Color(0xFF1A4550), RoundedCornerShape(16.dp)),
                color = SalahCardBg,
                shape = RoundedCornerShape(16.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(SalahEmerald),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = null,
                            tint = SalahTextWhite,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = locationResult,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahTextWhite,
                                fontSize = 16.sp
                            )
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (SalahGuardStore.city(context) == "Location not detected") "Location not yet detected" else "Location stored locally",
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SalahEmeraldLight,
                                fontWeight = FontWeight.Medium,
                                fontSize = 13.sp
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun LocationCheckItem(
    title: String,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(24.dp)
                .clip(CircleShape)
                .background(Color(0xFF0F3A42))
                .border(1.dp, SalahEmerald, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Check,
                contentDescription = null,
                tint = SalahEmerald,
                modifier = Modifier.size(15.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge.copy(
                fontWeight = FontWeight.Normal,
                color = SalahTextWhite,
                fontSize = 15.sp
            )
        )
    }
}
