package com.github.meypod.al_azan

import android.app.TimePickerDialog
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
    private var refresh by mutableStateOf(0)
    private var tab by mutableStateOf(0)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
                                icon = { Text(listOf("⌂", "◷", "▤", "⚙")[index], color = mint, fontSize = 22.sp) },
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
                    Text("SalahGuard", color = mint, fontSize = 27.sp, fontWeight = FontWeight.Bold)
                    Text(today, color = Color.LightGray)
                    when (tab) {
                        0 -> {
                            Card(colors = CardDefaults.cardColors(containerColor = surface),
                                shape = RoundedCornerShape(20.dp)) {
                                Column(Modifier.fillMaxWidth().padding(20.dp)) {
                                    Text("TODAY'S PRAYERS", color = mint, fontWeight = FontWeight.SemiBold)
                                    Spacer(Modifier.height(8.dp))
                                    Text("$completed / 5 confirmed", color = Color.White, fontSize = 26.sp)
                                    Text("Unconfirmed does not mean missed or completed.",
                                        color = Color.LightGray, fontSize = 12.sp)
                                }
                            }
                            prayers.forEachIndexed { i, prayer -> PrayerRow(today, prayer, examples[i]) }
                            Button(onClick = { tab = 1 }, modifier = Modifier.fillMaxWidth()) {
                                Text("Edit prayer times")
                            }
                        }
                        1 -> {
                            Header("Prayer timetable", "These are local manual times, not yet connected to alarms.")
                            prayers.forEachIndexed { i, prayer -> PrayerRow(today, prayer, examples[i], true) }
                        }
                        2 -> {
                            Header("Prayer history", "Daily and weekly reporting will be added after real verification is connected.")
                            Text("No completed prayers are inferred from alarm dismissal.",
                                color = Color.White)
                        }
                        else -> {
                            Header("Settings & testing", "Developer tools are kept outside the main dashboard.")
                            Button(onClick = {
                                Toast.makeText(this@SalahGuardDashboardActivity,
                                    SalahGuardTestAlarm.schedule(this@SalahGuardDashboardActivity),
                                    Toast.LENGTH_LONG).show()
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Test alarm in 60 seconds")
                            }
                            Button(onClick = {
                                startActivity(Intent(this@SalahGuardDashboardActivity, MainActivity::class.java))
                            }, modifier = Modifier.fillMaxWidth()) {
                                Text("Original prayer calculation settings")
                            }
                            Text("GPS city detection and real prayer scheduling: upcoming phases.",
                                color = Color.LightGray)
                        }
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
            modifier = Modifier.fillMaxWidth().clickable { tab = 1 },
            colors = CardDefaults.cardColors(containerColor = surface),
            shape = RoundedCornerShape(15.dp),
        ) {
            Row(Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween) {
                Column {
                    Text(prayer, color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.SemiBold)
                    Text(status, color = Color.LightGray, fontSize = 12.sp)
                }
                Column {
                    Text(value, color = mint, fontSize = 19.sp)
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
}
