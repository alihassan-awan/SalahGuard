package com.github.meypod.al_azan

import android.app.TimePickerDialog
import android.Manifest
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.LocationManager
import android.os.Build
import androidx.core.content.ContextCompat
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Switch
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Surface
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.ButtonDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.Calendar

/**
 * SalahGuard UI Phase 1: Compose dashboard with no overlay or hard-coded prayer completions.
 * Timetable values are user-editable examples until GPS scheduling is wired in a later phase.
 */
class SalahGuardDashboardActivity : ComponentActivity() {
    private val prayers = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha")
    private val examples = listOf("05:00", "13:30", "16:30", "18:00", "19:30")
    private val prefs by lazy { getSharedPreferences("salahguard_v03", MODE_PRIVATE) }
    private val date = SimpleDateFormat("yyyy-MM-dd", Locale.US)
    private val navy = Color(0xFF071B22)
    private val surface = Color(0xFF12343B)
    private val mint = Color(0xFF38D6A0)
    private val gold = Color(0xFFFFC15A)
    private var refresh by mutableStateOf(0)
    private var tab by mutableStateOf(0)
    private var detail by mutableStateOf("")
    private var liveLocation by mutableStateOf(false)
    private var locationStatus by mutableStateOf("Location not detected")
    private var locationBusy by mutableStateOf(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        liveLocation = prefs.getBoolean("live_location", false)
        setContent { SalahGuardScreen() }
    }

    @Composable
    private fun SalahGuardScreen() {
        val unusedRefresh = refresh
        val today = date.format(Date())
        val completed = prayers.count {
            prefs.getString("status_${today}_${it}", "Unconfirmed") == "Completed"
        }
        MaterialTheme {
            Scaffold(
                containerColor = navy,
                bottomBar = {
                    NavigationBar(containerColor = surface) {
                        listOf("Home", "Times", "History", "Settings").forEachIndexed { index, label ->
                            NavigationBarItem(
                                selected = tab == index,
                                onClick = { tab = index },
                                icon = { Text(listOf("⌂", "◷", "▤", "⚙")[index], color = if (tab == index) gold else mint, fontSize = 22.sp) },
                                label = { Text(label, color = Color.White) },
                            )
                        }
                    }
                },
            ) { padding ->
                Column(
                    modifier = Modifier.fillMaxSize().padding(padding).verticalScroll(rememberScrollState())
                        .padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Text("☪  SalahGuard", color = mint, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(today + "  •  " + (prefs.getString("gps_city", null) ?: "Manual timetable"), color = Color.LightGray)
                    if (detail.isNotEmpty()) {
                        DetailScreen()
                    } else when (tab) {
                        0 -> {
                            val now = Calendar.getInstance()
                            val currentMinute = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
                            val next = prayers.indices.map { i ->
                                val clock = (prefs.getString("time_${prayers[i]}", examples[i]) ?: examples[i]).split(":")
                                val minute = clock[0].toInt() * 60 + clock[1].toInt()
                                Triple(i, minute, (minute - currentMinute + 1440) % 1440)
                            }.minByOrNull { it.third }
                            Card(colors = CardDefaults.cardColors(containerColor = Color(0xFF075449)),
                                shape = RoundedCornerShape(22.dp)) {
                                Column(Modifier.fillMaxWidth().padding(22.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                    Text("NEXT PRAYER • MANUAL SCHEDULE", color = mint,
                                        fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    if (next != null) {
                                        Text(prayers[next.first], color = Color.White,
                                            fontSize = 31.sp, fontWeight = FontWeight.Bold)
                                        Text(formatTime(next.second), color = gold, fontSize = 25.sp)
                                        Text("In ${next.third / 60}h ${next.third % 60}m  •  Based on saved times",
                                            color = Color.LightGray, fontSize = 13.sp)
                                    }
                                }
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = surface),
                                shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.fillMaxWidth().padding(18.dp)) {
                                    Text("TODAY'S PRAYERS", color = mint, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(7.dp))
                                    Text("$completed / 5 verified", color = Color.White, fontSize = 24.sp)
                                    LinearProgressIndicator(progress = { completed / 5f }, modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), color = mint)
                                    Text("A saved time does not mean the prayer was performed.",
                                        color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            prayers.forEachIndexed { i, prayer -> PrayerRow(today, prayer, examples[i]) }
                            Button(onClick = { tab = 1 }, modifier = Modifier.fillMaxWidth()) {
                                Text("Manage prayer timings")
                            }
                        }
                        1 -> {
                            Header("Prayer Times", "Location, calculation and editable timings")
                            Card(colors = CardDefaults.cardColors(containerColor = surface),
                                shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(10.dp)) {
                                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                                        Column {
                                            Text("◉  Live Location (GPS)", color = Color.White, fontWeight = FontWeight.SemiBold)
                                            Text("Detect your city", color = Color.LightGray, fontSize = 12.sp)
                                        }
                                        Switch(checked = liveLocation, onCheckedChange = {
                                            liveLocation = it
                                            prefs.edit().putBoolean("live_location", it).apply()
                                            if (it) detectLocation()
                                        })
                                    }
                                    HorizontalDivider(color = Color(0xFF335259))
                                    Text(prefs.getString("gps_city", null) ?: "No detected city yet",
                                        color = mint, fontSize = 17.sp)
                                    Text(locationStatus, color = Color.LightGray, fontSize = 12.sp)
                                    OutlinedButton(onClick = { detail = "Location Detection"; detectLocation() },
                                        modifier = Modifier.fillMaxWidth()) { Text("Refresh GPS location") }
                                }
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = surface),
                                shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.fillMaxWidth().padding(16.dp)) {
                                    Text("Calculation method", color = gold, fontWeight = FontWeight.SemiBold)
                                    Text("Manual timetable active", color = Color.White, fontSize = 16.sp)
                                    OutlinedButton(onClick = { detail = "Calculation Method" },
                                        modifier = Modifier.fillMaxWidth()) { Text("Calculation & Asr settings") }
                                }
                            }
                            Card(colors = CardDefaults.cardColors(containerColor = surface),
                                shape = RoundedCornerShape(16.dp)) {
                                Text("MANUAL MODE • Times are saved locally. Real alarms are not yet linked.",
                                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                                    color = gold, fontSize = 13.sp)
                            }
                            prayers.forEachIndexed { i, prayer -> PrayerRow(today, prayer, examples[i], true) }
                        }
                        2 -> {
                            Header("Prayer History", "Your Salah accountability records")
                            Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(18.dp)) {
                                Column(Modifier.fillMaxWidth().padding(22.dp)) {
                                    Text("TODAY • $today", color = gold, fontWeight = FontWeight.Bold)
                                    Text("$completed of 5 verified", color = Color.White, fontSize = 26.sp)
                                    LinearProgressIndicator(progress = { completed / 5f }, color = mint,
                                        modifier = Modifier.fillMaxWidth().padding(top = 10.dp))
                                    Text("Verification and historical records are not yet integrated.", color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            OutlinedButton(onClick = { detail = "Daily History" }, modifier = Modifier.fillMaxWidth()) { Text("Today’s prayer records") }
                            OutlinedButton(onClick = { detail = "Weekly & Monthly Reports" }, modifier = Modifier.fillMaxWidth()) { Text("Weekly / Monthly Reports") }
                            Text("Real verification and report calculations are not yet connected.", color = Color.LightGray)
                        }
                        else -> {
                            Header("Settings", "Prayer alerts, calculation and developer tools")
                            Text("NOTIFICATIONS & ALARM", color = gold, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { detail = "Alarm Settings" }, modifier = Modifier.fillMaxWidth()) { Text("Notifications • Alarm • Snooze") }
                            Text("LOCATION & CALCULATION", color = gold, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { detail = "Location Detection" }, modifier = Modifier.fillMaxWidth()) { Text("Location & GPS") }
                            OutlinedButton(onClick = { detail = "Calculation Method" }, modifier = Modifier.fillMaxWidth()) { Text("Prayer calculation method") }
                            Text("DEVELOPER TOOLS", color = gold, fontWeight = FontWeight.Bold)
                            OutlinedButton(onClick = { detail = "Developer Test Alarm" }, modifier = Modifier.fillMaxWidth()) { Text("Developer test alarm screen") }
                            Text("No real prayer statuses are altered by developer tests.", color = Color.LightGray, fontSize = 12.sp)
                        }
                    }
                }
            }
        }
    }

    @Composable
    private fun DetailScreen() {
        Header(detail, "SalahGuard")
        OutlinedButton(onClick = { detail = "" }, modifier = Modifier.fillMaxWidth()) { Text("← Back") }
        Card(colors = CardDefaults.cardColors(containerColor = surface), shape = RoundedCornerShape(18.dp)) {
            Column(Modifier.fillMaxWidth().padding(20.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                when (detail) {
                    "Location Detection" -> {
                        Text(locationStatus, color = Color.White)
                        Button(onClick = { detectLocation() }, modifier = Modifier.fillMaxWidth()) { Text("Detect city using GPS") }
                        Text("Detected coordinates are not yet linked to calculation.", color = Color.LightGray)
                    }
                    "Developer Test Alarm" -> {
                        Text("Test alerts are separate from real Salah records.", color = Color.White)
                        Button(onClick = {
                            Toast.makeText(this@SalahGuardDashboardActivity,
                                SalahGuardTestAlarm.schedule(this@SalahGuardDashboardActivity), Toast.LENGTH_LONG).show()
                        }) { Text("Trigger in 60 seconds") }
                    }
                    "Calculation Method" -> {
                        Text("Manual timetable is currently active.", color = gold)
                        Text("Calculation method and Asr selection will be connected to the inherited prayer engine in the next functional phase.", color = Color.White)
                    }
                    "Alarm Settings" -> {
                        Text("Prayer alarms and snooze settings will be connected to manual prayer times next.", color = Color.White)
                        Text("Existing 60-second test alarm is available under Developer Test Alarm.", color = Color.LightGray)
                    }
                    "Daily History" -> {
                        Text("No prayer is marked completed until verified.", color = Color.White)
                        prayers.forEach { Text("$it  •  Not yet verified", color = Color.LightGray) }
                    }
                    else -> {
                        Text("Weekly and monthly reports are awaiting real prayer-status data.", color = Color.White)
                    }
                }
            }
        }
    }

    @Composable
    private fun Header(title: String, subtitle: String) {
        Text(title, color = Color.White, fontSize = 23.sp, fontWeight = FontWeight.Bold)
        Text(subtitle, color = Color.LightGray, fontSize = 14.sp)
    }

    @Composable
    private fun PrayerRow(today: String, prayer: String, fallback: String, editable: Boolean = false) {
        val value = prefs.getString("time_$prayer", fallback) ?: fallback
        val status = prefs.getString("status_${today}_${prayer}", "Unconfirmed") ?: "Unconfirmed"
        Card(
            modifier = if (editable) Modifier.fillMaxWidth() else Modifier.fillMaxWidth().clickable { tab = 1 },
            colors = CardDefaults.cardColors(containerColor = surface),
            shape = RoundedCornerShape(15.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(prayer, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Text(if (editable) "Time saved locally" else if (status == "Unconfirmed") "Prayer not yet verified" else status,
                        color = if (editable) mint else Color.LightGray, fontSize = 12.sp)
                }
                Column {
                    Text(formatTime(value.split(":")[0].toInt() * 60 + value.split(":")[1].toInt()), color = gold, fontSize = 19.sp)
                    if (editable) {
                        Text("Edit time", color = Color.White, modifier = Modifier.clickable {
                            val t = value.split(":")
                            TimePickerDialog(this@SalahGuardDashboardActivity, { _, hour, minute ->
                                prefs.edit().putString("time_$prayer", "%02d:%02d".format(Locale.US, hour, minute)).apply()
                                refresh++
                            }, t[0].toInt(), t[1].toInt(), false).show()
                        })
                    }
                }
            }
        }
    }
    private fun formatTime(minutes: Int): String {
        val hour = (minutes / 60) % 24
        val minute = minutes % 60
        return "%d:%02d %s".format(Locale.US, if (hour % 12 == 0) 12 else hour % 12,
            minute, if (hour < 12) "AM" else "PM")
    }

    private fun detectLocation() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) !=
            PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION), 4201)
            return
        }
        val manager = getSystemService(LocationManager::class.java)
        val provider = when {
            manager.isProviderEnabled(LocationManager.GPS_PROVIDER) -> LocationManager.GPS_PROVIDER
            manager.isProviderEnabled(LocationManager.NETWORK_PROVIDER) -> LocationManager.NETWORK_PROVIDER
            else -> {
                locationStatus = "Location services are disabled. Enable GPS in Android Settings."
                return
            }
        }
        locationBusy = true
        locationStatus = "Retrieving GPS coordinates..."
        if (Build.VERSION.SDK_INT >= 30) {
            manager.getCurrentLocation(provider, null, mainExecutor) { point ->
                locationBusy = false
                if (point == null) {
                    locationStatus = "No GPS fix. Check permissions and retry, or use manual times."
                } else {
                    prefs.edit().putString("gps_coords", "${point.latitude},${point.longitude}").apply()
                    val coords = "%.4f, %.4f".format(Locale.US, point.latitude, point.longitude)
                    locationStatus = "Coordinates: $coords"
                    Thread {
                        try {
                            @Suppress("DEPRECATION")
                            val address = Geocoder(this, Locale.getDefault())
                                .getFromLocation(point.latitude, point.longitude, 1)?.firstOrNull()
                            val city = listOfNotNull(address?.locality ?: address?.subAdminArea,
                                address?.countryName).joinToString(", ")
                            runOnUiThread {
                                if (city.isNotBlank()) {
                                    locationStatus = "$city ($coords)"
                                    prefs.edit().putString("gps_city", city).apply()
                                } else locationStatus = "GPS: $coords (city unavailable)"
                            }
                        } catch (_: Exception) {
                            runOnUiThread { locationStatus = "GPS: $coords (city unavailable)" }
                        }
                    }.start()
                }
            }
        } else {
            @Suppress("DEPRECATION")
            val point = manager.getLastKnownLocation(provider)
            locationBusy = false
            locationStatus = if (point == null) "Location not yet available; retry with GPS enabled."
                else "GPS: %.4f, %.4f".format(Locale.US, point.latitude, point.longitude)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 4201) {
            if (grantResults.any { it == PackageManager.PERMISSION_GRANTED }) detectLocation()
            else locationStatus = "Location permission denied. Manual prayer times remain available."
        }
    }

}
