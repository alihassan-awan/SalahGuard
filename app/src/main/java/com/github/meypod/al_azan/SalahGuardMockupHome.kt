package com.github.meypod.al_azan

import android.content.SharedPreferences
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import java.util.Calendar
import java.util.Locale

private val dark = Color(0xFF061920)
private val card = Color(0xFF102C33)
private val emerald = Color(0xFF20D48D)
private val amber = Color(0xFFFFC255)
private val muted = Color(0xFFB8C8C9)

@Composable
fun SalahGuardMockupHome(prefs: SharedPreferences, openTimes: () -> Unit) {
    val names = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha")
    val icons = listOf("🌅", "☀", "🌤", "🌇", "☾")
    val defaults = listOf("05:00", "13:30", "16:30", "18:00", "19:30")
    val now = Calendar.getInstance()
    val minuteNow = now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE)
    val day = java.text.SimpleDateFormat("yyyy-MM-dd", Locale.US).format(now.time)
    val times = names.indices.map { index ->
        val raw = prefs.getString("time_${names[index]}", defaults[index]) ?: defaults[index]
        val parts = raw.split(":")
        (parts[0].toIntOrNull() ?: 0) * 60 + (parts.getOrNull(1)?.toIntOrNull() ?: 0)
    }
    val nextIndex = times.indices.minByOrNull { (times[it] - minuteNow + 1440) % 1440 } ?: 0
    val remaining = (times[nextIndex] - minuteNow + 1440) % 1440
    fun clock(value: Int): String {
        val h = (value / 60) % 24
        return "%d:%02d %s".format(Locale.US, if (h % 12 == 0) 12 else h % 12,
            value % 60, if (h < 12) "AM" else "PM")
    }
    Text("Your next prayer", color = muted, fontSize = 13.sp)
    Card(shape = RoundedCornerShape(22.dp), colors = CardDefaults.cardColors(containerColor = card)) {
        Box(
            Modifier.fillMaxWidth().background(
                Brush.linearGradient(listOf(Color(0xFF07504A), Color(0xFF11343B), dark))
            ).padding(20.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                Text(icons[nextIndex], fontSize = 46.sp)
                Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text("NEXT PRAYER", color = muted, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    Text(names[nextIndex], color = Color.White, fontSize = 29.sp, fontWeight = FontWeight.Bold)
                    Text("%02d:%02d:%02d".format(Locale.US, remaining / 60, remaining % 60, 0),
                        color = amber, fontSize = 28.sp, fontWeight = FontWeight.Bold)
                    Text(clock(times[nextIndex]) + " • Manual timetable", color = muted, fontSize = 12.sp)
                }
            }
        }
    }
    Text("Today's prayers", color = Color.White, fontSize = 19.sp, fontWeight = FontWeight.Bold)
    names.forEachIndexed { index, prayer ->
        val verified = prefs.getString("status_${day}_${prayer}", "Unconfirmed") == "Completed"
        val upcoming = index == nextIndex
        val modifier = Modifier.fillMaxWidth().then(
            if (upcoming) Modifier.border(1.dp, amber, RoundedCornerShape(13.dp)) else Modifier
        ).clickable(onClick = openTimes)
        Surface(modifier = modifier, shape = RoundedCornerShape(13.dp), color = card) {
            Row(Modifier.fillMaxWidth().padding(horizontal = 14.dp, vertical = 13.dp),
                verticalAlignment = Alignment.CenterVertically) {
                Text(icons[index], fontSize = 23.sp)
                Spacer(Modifier.width(13.dp))
                Text(prayer, color = Color.White, fontSize = 17.sp, fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f))
                Text(clock(times[index]), color = if (upcoming) amber else emerald, fontSize = 14.sp)
                Spacer(Modifier.width(12.dp))
                Text(if (verified) "✓" else if (upcoming) "◷" else "○",
                    color = if (verified) emerald else if (upcoming) amber else muted, fontSize = 21.sp)
            }
        }
    }
    Text("✓ = verified  •  ○ = not yet verified. Times are not proof of prayer.",
        color = muted, fontSize = 11.sp)
}
