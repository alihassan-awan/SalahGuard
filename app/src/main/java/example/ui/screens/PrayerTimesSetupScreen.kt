package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.PrayerType
import com.example.data.model.SalahGuardStore
import com.example.data.model.SalahGuardPrayerCalculator
import com.github.meypod.al_azan.SalahGuardPrayerScheduler
import androidx.compose.ui.platform.LocalContext
import com.example.ui.components.*
import com.example.ui.theme.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrayerTimesSetupScreen(
    onNavigateBack: () -> Unit,
    onDetectLocationClick: () -> Unit,
    onEditTimesClick: () -> Unit,
    onSaved: () -> Unit,
    onQuickSwitchClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var gpsEnabled by remember { mutableStateOf(SalahGuardStore.gpsEnabled(context)) }
    var selectedCalculationMethod by remember { mutableStateOf(SalahGuardStore.setting(context, "calculation_method", "Muslim World League (MWL)")) }
    var selectedAsrMethod by remember { mutableStateOf(SalahGuardStore.setting(context, "asr_method", SalahGuardPrayerCalculator.suggestedAsrMethod(SalahGuardStore.city(context)))) }
    var is24HourFormat by remember { mutableStateOf(SalahGuardStore.is24(context)) }

    var calcDropdownExpanded by remember { mutableStateOf(false) }
    var autoError by remember { mutableStateOf(false) }
    var useCalculatedTimes by remember { mutableStateOf(
        SalahGuardStore.setting(context, "timetable_mode", "calculated") != "manual" ||
            !context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
                .getBoolean("manual_schedule_enabled", false)
    ) }
    var asrDropdownExpanded by remember { mutableStateOf(false) }

    val calculationMethods = listOf(
        "Muslim World League (MWL)",
        "Islamic Society of North America (ISNA)",
        "Egyptian General Authority of Survey",
        "Umm al-Qura University, Makkah",
        "University of Islamic Sciences, Karachi"
    )

    val asrMethods = listOf(
        "Standard (Shafi'i, Maliki, Hanbali)",
        "Hanafi"
    )

    // Preview is calculated from the *currently selected* method, not stale saved settings.
    val savedCoordinates = context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
        .getString("gps_coords", null)
    val preview = if (useCalculatedTimes) SalahGuardPrayerCalculator.calculateForCoordinates(
        savedCoordinates,
        selectedCalculationMethod,
        selectedAsrMethod,
        java.time.LocalDate.now(),
        java.time.ZoneId.systemDefault()
    ) else null
    val scrollState = rememberScrollState()

    Scaffold(
        containerColor = SalahDarkBg,
        topBar = {
            SalahGuardTopBar(
                title = "Prayer Times",
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
                    text = "Save Settings",
                    onClick = {
                        SalahGuardStore.setGpsEnabled(context, gpsEnabled)
                        SalahGuardStore.setSetting(context, "calculation_method", selectedCalculationMethod)
                        SalahGuardStore.setSetting(context, "asr_method", selectedAsrMethod)
                        SalahGuardStore.set24Format(context, is24HourFormat)
                        if (useCalculatedTimes) {
                            // Explicit opt-in: never silently replace manually entered prayer times.
                            if (SalahGuardPrayerCalculator.calculate(context) != null) {
                                SalahGuardStore.setSetting(context, "timetable_mode", "calculated")
                                if (SalahGuardPrayerCalculator.apply(context)) {
                                    SalahGuardPrayerScheduler.reschedule(context)
                                    onSaved()
                                } else autoError = true
                            } else autoError = true
                        } else {
                            val p = context.getSharedPreferences("salahguard_v03", android.content.Context.MODE_PRIVATE)
                            if (p.getBoolean("manual_schedule_enabled", false)) {
                                SalahGuardStore.setSetting(context, "timetable_mode", "manual")
                                onSaved()
                            } else autoError = true
                        }
                    },
                    testTag = "save_settings_button"
                )
            }
        },
        modifier = modifier.testTag("prayer_times_setup_screen")
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (autoError) Text("GPS coordinates unavailable. Detect location or save all five manual prayer times first.", color = SalahGold)
            Surface(
                color = SalahCardBg,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(Modifier.padding(14.dp)) {
                    Text("Prayer timetable source", color = SalahTextWhite, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(8.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = useCalculatedTimes, onClick = { useCalculatedTimes = true; autoError = false })
                        Text("Automatic from saved GPS coordinates", color = SalahTextWhite,
                            modifier = Modifier.clickable { useCalculatedTimes = true; autoError = false })
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        RadioButton(selected = !useCalculatedTimes, onClick = { useCalculatedTimes = false; autoError = false })
                        Text("My manually entered prayer times", color = SalahTextWhite,
                            modifier = Modifier.clickable { useCalculatedTimes = false; autoError = false })
                    }
                    Text("Automatic times depend on calculation method and device timezone. Confirm against your local masjid timetable.",
                        color = SalahTextSecondary, fontSize = 12.sp)
                }
            }
            Spacer(Modifier.height(16.dp))
            if (useCalculatedTimes) {
                Surface(color = SalahCardBg, shape = RoundedCornerShape(14.dp),
                    modifier = Modifier.fillMaxWidth()) {
                    Column(Modifier.padding(16.dp)) {
                        Text("Calculated preview — today", color = SalahTextWhite,
                            fontWeight = FontWeight.Bold)
                        Text("Asr school: " + selectedAsrMethod,
                            color = SalahGold, fontSize = 13.sp)
                        Text("Location: " + SalahGuardStore.city(context),
                            color = SalahTextSecondary, fontSize = 12.sp)
                        if (preview == null) {
                            Text("No valid GPS coordinates. Detect location before saving.",
                                color = SalahGold)
                        } else {
                            SalahGuardStore.names.forEach { prayer ->
                                Row(Modifier.fillMaxWidth().padding(vertical = 5.dp),
                                    horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(prayer, color = SalahTextWhite)
                                    Text(SalahGuardStore.display(preview.getValue(prayer), is24HourFormat),
                                        color = SalahEmeraldLight)
                                }
                            }
                        }
                        Text("Times are astronomical estimates. Compare with your local mosque timetable.",
                            color = SalahTextSecondary, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(16.dp))
            }
            // Live Location (GPS) Card
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(16.dp)),
                shape = RoundedCornerShape(16.dp),
                color = SalahCardBg
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "Live Location (GPS)",
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.Bold,
                                color = SalahTextWhite
                            )
                        )
                        Switch(
                            checked = gpsEnabled,
                            onCheckedChange = { gpsEnabled = it; SalahGuardStore.setGpsEnabled(context, it) },
                            colors = SwitchDefaults.colors(
                                checkedThumbColor = SalahTextWhite,
                                checkedTrackColor = SalahEmerald,
                                uncheckedThumbColor = SalahTextSecondary,
                                uncheckedTrackColor = SalahCardBorder
                            ),
                            modifier = Modifier.testTag("gps_switch")
                        )
                    }

                    Text(
                        text = "Auto detect your city and calculate times",
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = SalahTextSecondary,
                            fontSize = 13.sp
                        )
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    // Detected City Inner Box
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .clickable(onClick = onDetectLocationClick),
                        color = Color(0xFF0C1D22),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.LocationOn,
                                contentDescription = "Location",
                                tint = Color(0xFF38BDF8),
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = SalahGuardStore.city(context),
                                    style = MaterialTheme.typography.titleSmall.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = SalahTextWhite
                                    )
                                )
                                Text(
                                    text = if (SalahGuardStore.city(context) == "Location not detected") "Tap to detect GPS" else "Saved GPS location",
                                    style = MaterialTheme.typography.labelSmall.copy(
                                        color = SalahTextSecondary,
                                        fontSize = 11.sp
                                    )
                                )
                            }
                            IconButton(
                                onClick = onDetectLocationClick,
                                modifier = Modifier.size(36.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Refresh Location",
                                    tint = SalahTextWhite,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown 1: Calculation Method
            Text(
                text = "Calculation Method",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SalahCardBorder, RoundedCornerShape(14.dp))
                        .clickable { calcDropdownExpanded = true },
                    color = SalahCardBg,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedCalculationMethod,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = SalahTextWhite,
                                fontSize = 15.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = SalahTextSecondary
                        )
                    }
                }
                DropdownMenu(
                    expanded = calcDropdownExpanded,
                    onDismissRequest = { calcDropdownExpanded = false },
                    modifier = Modifier.background(SalahDarkSurface)
                ) {
                    calculationMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method, color = SalahTextWhite) },
                            onClick = {
                                selectedCalculationMethod = method
                                calcDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Dropdown 2: Asr Method
            Text(
                text = "Asr Method",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Box {
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.dp, SalahCardBorder, RoundedCornerShape(14.dp))
                        .clickable { asrDropdownExpanded = true },
                    color = SalahCardBg,
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = selectedAsrMethod,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                color = SalahTextWhite,
                                fontSize = 15.sp
                            )
                        )
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowDown,
                            contentDescription = "Dropdown",
                            tint = SalahTextSecondary
                        )
                    }
                }
                DropdownMenu(
                    expanded = asrDropdownExpanded,
                    onDismissRequest = { asrDropdownExpanded = false },
                    modifier = Modifier.background(SalahDarkSurface)
                ) {
                    asrMethods.forEach { method ->
                        DropdownMenuItem(
                            text = { Text(method, color = SalahTextWhite) },
                            onClick = {
                                selectedAsrMethod = method
                                asrDropdownExpanded = false
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Time Format Segmented Control
            Text(
                text = "Time Format",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(12.dp)),
                shape = RoundedCornerShape(12.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                ) {
                    // 12 Hour Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (!is24HourFormat) Color(0xFF00B37E).copy(alpha = 0.85f) else Color.Transparent)
                            .clickable { is24HourFormat = false },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "12 Hour",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (!is24HourFormat) FontWeight.Bold else FontWeight.Normal,
                                color = if (!is24HourFormat) SalahTextWhite else SalahTextSecondary
                            )
                        )
                    }

                    // 24 Hour Pill
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (is24HourFormat) Color(0xFF00B37E).copy(alpha = 0.85f) else Color.Transparent)
                            .clickable { is24HourFormat = true },
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "24 Hour",
                            style = MaterialTheme.typography.labelLarge.copy(
                                fontWeight = if (is24HourFormat) FontWeight.Bold else FontWeight.Normal,
                                color = if (is24HourFormat) SalahTextWhite else SalahTextSecondary
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Manual Prayer Times (Optional)
            Text(
                text = "Manual Prayer Times (Optional)",
                style = MaterialTheme.typography.labelLarge.copy(
                    color = SalahTextSecondary,
                    fontSize = 13.sp
                )
            )
            Spacer(modifier = Modifier.height(8.dp))

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, SalahCardBorder, RoundedCornerShape(14.dp))
                    .clickable(onClick = onEditTimesClick),
                shape = RoundedCornerShape(14.dp),
                color = SalahCardBg
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    PrayerIcon(type = PrayerType.FAJR, modifier = Modifier.size(24.dp))
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "Fajr",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.Medium,
                            color = SalahTextWhite
                        ),
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = Color(0xFF0C1D22),
                        border = androidx.compose.foundation.BorderStroke(1.dp, SalahCardBorder)
                    ) {
                        Text(
                            text = SalahGuardStore.display(SalahGuardStore.get24(context, "Fajr")),
                            style = MaterialTheme.typography.bodyMedium.copy(
                                color = SalahTextWhite,
                                fontWeight = FontWeight.SemiBold
                            ),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edit All Times",
                        tint = SalahEmerald,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
