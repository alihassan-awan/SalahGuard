package com.example.data.model

import org.junit.Assert.*
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

/** Pure JVM tests: do not initialize the Android Application or native MMKV. */
class SalahGuardPrayerCalculatorTest {
    private val date = LocalDate.of(2026, 10, 30)
    private val zone = ZoneId.of("Asia/Karachi")
    private val method = "University of Islamic Sciences, Karachi"

    @Test fun missingLocationCannotGenerateTimetable() {
        assertNull(SalahGuardPrayerCalculator.calculateForCoordinates(null, method, "Hanafi", date, zone))
    }

    @Test fun invalidCoordinatesAreRejected() {
        for (coords in listOf("999.0,73.0", "33.0,NaN", "broken", "91,50", "33.0,181")) {
            assertNull(coords, SalahGuardPrayerCalculator.calculateForCoordinates(coords, method, "Hanafi", date, zone))
        }
    }

    @Test fun islamabadProducesFiveOrderedTimes() {
        val times = SalahGuardPrayerCalculator.calculateForCoordinates(
            "33.6844,73.0479", method, "Hanafi", date, zone)
        assertNotNull(times)
        assertEquals(listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha").toSet(), times!!.keys)
        val minutes = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha").map { name ->
            val time = times.getValue(name).split(":")
            time[0].toInt() * 60 + time[1].toInt()
        }
        assertTrue("Prayer times should be ordered within the day: $minutes",
            minutes.zipWithNext().all { (a, b) -> a < b })
    }

    @Test fun hanafiAsrIsLaterThanStandardAsr() {
        val coordinates = "33.6844,73.0479"
        val standard = SalahGuardPrayerCalculator.calculateForCoordinates(
            coordinates, method, "Standard (Shafi'i, Maliki, Hanbali)", date, zone)!!
        val hanafi = SalahGuardPrayerCalculator.calculateForCoordinates(
            coordinates, method, "Hanafi", date, zone)!!
        fun minutes(value: String): Int {
            val parts = value.split(":")
            return parts[0].toInt() * 60 + parts[1].toInt()
        }
        assertTrue("Hanafi Asr should occur after standard Asr",
            minutes(hanafi.getValue("Asr")) > minutes(standard.getValue("Asr")))
    }

    @Test fun nextDayCalculationUsesItsOwnDate() {
        val today = SalahGuardPrayerCalculator.calculateForCoordinates(
            "33.6844,73.0479", method, "Hanafi", date, zone)!!
        val laterDate = SalahGuardPrayerCalculator.calculateForCoordinates(
            "33.6844,73.0479", method, "Hanafi", date.plusDays(30), zone)!!
        assertNotEquals("Date-dependent timetable must change over a month",
            today, laterDate)
    }

    @Test fun manualTimetableRemainsProtected() {
        assertFalse(SalahGuardPrayerCalculator.canAutoApply("manual", true))
        assertTrue(SalahGuardPrayerCalculator.canAutoApply("calculated", true))
        assertTrue(SalahGuardPrayerCalculator.canAutoApply("manual", false))
    }
}
