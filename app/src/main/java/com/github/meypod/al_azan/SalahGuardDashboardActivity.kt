package com.github.meypod.al_azan

import android.app.Activity
import android.app.TimePickerDialog
import android.os.Bundle
import android.content.Intent
import android.widget.Toast
import android.graphics.Color
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * SalahGuard V0.3 foundation: local timetable and honest daily status overview.
 * This screen DOES NOT claim a prayer was performed or automatically schedule real alarms.
 */
class SalahGuardDashboardActivity : Activity() {
    private val names = listOf("Fajr", "Zuhr", "Asr", "Maghrib", "Isha")
    private val initialTimes = listOf("05:00", "13:30", "16:30", "18:00", "19:30")
    private val prefs by lazy { getSharedPreferences("salahguard_v03", MODE_PRIVATE) }
    private val dayFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render()
    }

    private fun render() {
        val today = dayFormat.format(Date())
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(30, 36, 30, 44)
            setBackgroundColor(Color.rgb(12, 25, 31))
        }
        fun text(value: String, size: Float, faded: Boolean = false) {
            root.addView(TextView(this).apply {
                this.text = value
                textSize = size
                setTextColor(if (faded) Color.LTGRAY else Color.WHITE)
                setPadding(0, 8, 0, 14)
            })
        }
        text("SALAHGUARD  •  V0.3", 15f)
        text("Today's five prayers", 28f)
        text(today + "  |  Offline timetable", 15f, true)
        text("Set your local prayer times below. Defaults are examples, NOT verified prayer times.", 14f, true)
        val completed = names.count { prefs.getString("status_${today}_${it}", "Unconfirmed") == "Completed" }
        text("Confirmed: $completed / 5", 22f)
        names.forEachIndexed { i, name ->
            val time = prefs.getString("time_$name", initialTimes[i]) ?: initialTimes[i]
            val status = prefs.getString("status_${today}_${name}", "Unconfirmed") ?: "Unconfirmed"
            text("$name  •  $time", 23f)
            text("Today: $status", 15f, true)
            val change = Button(this).apply {
                this.text = "Edit $name time"
                isAllCaps = false
                setOnClickListener {
                    val parts = time.split(":")
                    TimePickerDialog(
                        this@SalahGuardDashboardActivity,
                        { _, hour, minute ->
                            prefs.edit().putString("time_$name", "%02d:%02d".format(Locale.US, hour, minute)).apply()
                            render()
                        },
                        parts[0].toInt(), parts[1].toInt(), false,
                    ).show()
                }
            }
            root.addView(change)
            root.addView(TextView(this).apply {
                this.text = "Prayer status is not self-ticked here. Real verification will be wired to prayer alarms next."
                textSize = 12f
                setTextColor(Color.LTGRAY)
                setPadding(0, 4, 0, 22)
            })
        }
        root.addView(Button(this).apply {
            text = "Test alarm in 60 seconds"
            isAllCaps = false
            setOnClickListener {
                Toast.makeText(this@SalahGuardDashboardActivity,
                    SalahGuardTestAlarm.schedule(this@SalahGuardDashboardActivity),
                    Toast.LENGTH_LONG).show()
            }
        })
        root.addView(Button(this).apply {
            text = "Open original prayer settings"
            isAllCaps = false
            setOnClickListener {
                startActivity(Intent(this@SalahGuardDashboardActivity, MainActivity::class.java))
            }
        })
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }
}
