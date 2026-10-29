package com.example.data.model

import android.content.Context
import android.content.SharedPreferences
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Read-through bridge to the existing SalahGuard manual timetable preferences. */
object SalahGuardStore {
    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
    val names = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha")
    private val defaults = listOf("05:00", "13:30", "16:30", "18:00", "19:30")
    fun get24(context: Context, prayer: String): String {
        val index = names.indexOf(prayer).coerceAtLeast(0)
        return prefs(context).getString("time_$prayer", defaults[index]) ?: defaults[index]
    }
    fun set24(context: Context, prayer: String, time: String) {
        prefs(context).edit().putString("time_$prayer", time).apply()
    }
    fun display(time: String, format24: Boolean = false): String {
        val parts = time.split(":")
        val hour = parts.getOrNull(0)?.toIntOrNull() ?: 0
        val min = parts.getOrNull(1)?.toIntOrNull() ?: 0
        return if (format24) "%02d:%02d".format(Locale.US, hour, min)
        else "%d:%02d %s".format(Locale.US, if (hour % 12 == 0) 12 else hour % 12, min,
            if (hour < 12) "AM" else "PM")
    }
    fun parse(value: String): String? {
        val trimmed = value.trim()
        val twelve = Regex("""^(\d{1,2}):(\d{2})\s*(AM|PM)$""", RegexOption.IGNORE_CASE).matchEntire(trimmed)
        if (twelve != null) {
            val h = twelve.groupValues[1].toInt()
            val m = twelve.groupValues[2].toInt()
            if (h !in 1..12 || m !in 0..59) return null
            val hour = (h % 12) + if (twelve.groupValues[3].equals("PM", true)) 12 else 0
            return "%02d:%02d".format(Locale.US, hour, m)
        }
        val twentyFour = Regex("""^(\d{1,2}):(\d{2})$""").matchEntire(trimmed) ?: return null
        val h = twentyFour.groupValues[1].toInt()
        val m = twentyFour.groupValues[2].toInt()
        return if (h in 0..23 && m in 0..59) "%02d:%02d".format(Locale.US, h, m) else null
    }
    fun city(context: Context): String = prefs(context).getString("gps_city", null) ?: "Location not detected"
    fun setCity(context: Context, name: String, lat: Double, lon: Double) {
        prefs(context).edit().putString("gps_city", name)
            .putString("gps_coords", "$lat,$lon").apply()
    }
    fun gpsEnabled(context: Context) = prefs(context).getBoolean("live_location", false)
    fun setGpsEnabled(context: Context, enabled: Boolean) =
        prefs(context).edit().putBoolean("live_location", enabled).apply()
    fun setting(context: Context, key: String, fallback: String): String =
        prefs(context).getString(key, fallback) ?: fallback
    fun setSetting(context: Context, key: String, value: String) =
        prefs(context).edit().putString(key, value).apply()
    fun is24(context: Context) = prefs(context).getBoolean("time_format_24", false)
    fun set24Format(context: Context, value: Boolean) =
        prefs(context).edit().putBoolean("time_format_24", value).apply()
    fun today(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())
    fun verified(context: Context, prayer: String) =
        prefs(context).getString("status_${today()}_$prayer", "Unconfirmed") == "Completed"
}
