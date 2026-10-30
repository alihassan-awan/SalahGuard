package com.example.data.model

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.time.LocalDate
import java.util.TimeZone

@RunWith(RobolectricTestRunner::class)
class SalahGuardPrayerCalculatorTest {
    private lateinit var context: Context

    @Before fun reset() {
        context = ApplicationProvider.getApplicationContext()
        context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE).edit().clear().commit()
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Karachi"))
    }

    @Test fun missingLocationCannotGenerateTimetable() {
        assertNull(SalahGuardPrayerCalculator.calculate(context))
        assertFalse(SalahGuardPrayerCalculator.apply(context))
        assertFalse(context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
            .getBoolean("manual_schedule_enabled", false))
    }

    @Test fun invalidCoordinatesAreRejected() {
        context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE).edit()
            .putString("gps_coords", "999.0,73.0").commit()
        assertNull(SalahGuardPrayerCalculator.calculate(context))
    }

    @Test fun islamabadProducesFiveOrderedTimes() {
        val prefs = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
        prefs.edit().putString("gps_coords", "33.6844,73.0479")
            .putString("calculation_method", "University of Islamic Sciences, Karachi")
            .putString("asr_method", "Hanafi").commit()
        val times = SalahGuardPrayerCalculator.calculate(context, LocalDate.of(2026, 10, 30))
        assertNotNull(times)
        assertEquals(SalahGuardStore.names.toSet(), times!!.keys)
        val minutes = SalahGuardStore.names.map { prayer ->
            val t = times.getValue(prayer).split(":")
            t[0].toInt() * 60 + t[1].toInt()
        }
        assertTrue("Prayer times should be ordered within the day: $minutes",
            minutes.zipWithNext().all { (a, b) -> a < b })
    }

    @Test fun manualTimesCannotBeOverwrittenByAutoRefresh() {
        val prefs = context.getSharedPreferences("salahguard_v03", Context.MODE_PRIVATE)
        prefs.edit().putString("gps_coords", "33.6844,73.0479")
            .putBoolean("manual_schedule_enabled", true)
            .putString("timetable_mode", "manual")
            .putString("time_Fajr", "04:44").commit()
        assertFalse(SalahGuardPrayerCalculator.apply(context))
        assertEquals("04:44", prefs.getString("time_Fajr", null))
    }
}
