package com.github.meypod.al_azan

import android.app.Activity
import android.app.AlarmManager
import android.app.AlertDialog
import android.app.PendingIntent
import android.app.NotificationManager
import android.content.Intent
import android.graphics.Color
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import android.provider.Settings
import android.view.Gravity
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * A test-only prototype. No real prayer is marked completed by this screen.
 * Later phases will associate each event with an actual prayer and a persisted prayer window.
 */
class SalahGuardAccountabilityActivity : Activity() {
    private var player: MediaPlayer? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.i("SalahGuardTestAlarm", "test accountability activity opened")
        if (Build.VERSION.SDK_INT >= 27) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(0x00080000 or 0x00200000)
        }
        window.addFlags(android.view.WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        startRinging()
        showActions()
    }

    private fun showActions() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(36, 48, 36, 36)
            setBackgroundColor(Color.rgb(12, 25, 31))
        }
        fun title(value: String, size: Float) {
            root.addView(TextView(this).apply {
                text = value
                textSize = size
                setTextColor(Color.WHITE)
                gravity = Gravity.CENTER
                setPadding(0, 12, 0, 24)
            })
        }
        fun action(label: String, click: () -> Unit) {
            root.addView(Button(this).apply {
                text = label
                isAllCaps = false
                setOnClickListener { click() }
            }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 20 })
        }
        title("SALAHGUARD — ALARM TEST", 14f)
        title("It is time to pray", 29f)
        title("This is a test alarm. Your actual prayer records will NOT be changed.", 16f)
        action("Snooze — 10 minutes") { snooze() }
        action("I have prayed — verify") { verify() }
        action("I have not prayed — explain") { explain() }
        action("End test (no prayer status recorded)") {
            record("test_ended", "")
            stopRinging()
            finish()
        }
        val scroll = ScrollView(this)
        scroll.addView(root)
        setContentView(scroll)
    }

    private fun snooze() {
        val manager = getSystemService(AlarmManager::class.java)
        val pending = PendingIntent.getBroadcast(
            this, 90501,
            Intent(this, SalahGuardTestAlarmReceiver::class.java)
                .setAction("com.github.meypod.al_azan.TEST_ALARM"),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )
        val time = SystemClock.elapsedRealtime() + 10 * 60_000L
        if (Build.VERSION.SDK_INT >= 31 && !manager.canScheduleExactAlarms()) {
            manager.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pending)
        } else {
            manager.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME_WAKEUP, time, pending)
        }
        record("snoozed_10m", "")
        stopRinging()
        Toast.makeText(this, "Test alarm snoozed for 10 minutes", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun verify() {
        val field = EditText(this).apply {
            hint = "Type: I PRAYED"
            setSingleLine()
        }
        AlertDialog.Builder(this)
            .setTitle("An honest answer matters")
            .setMessage("Did you actually perform your prayer? Allah knows our actions. Do not confirm merely to silence an alarm.\n\nThis is ONLY a test; no real Salah will be recorded.")
            .setView(field)
            .setNegativeButton("Go back", null)
            .setPositiveButton("Confirm") { _, _ ->
                if (field.text.toString().trim() == "I PRAYED") {
                    record("test_verified", "typed-confirmation")
                    stopRinging()
                    finish()
                } else {
                    Toast.makeText(this, "Type I PRAYED exactly to confirm", Toast.LENGTH_LONG).show()
                    verify()
                }
            }.show()
    }

    private fun explain() {
        val reasons = arrayOf("Work / busy", "Laziness", "Phone / social media", "Overslept", "Forgot", "Illness / travel", "Other")
        AlertDialog.Builder(this)
            .setTitle("Why was the prayer missed?")
            .setItems(reasons) { _, index ->
                val explanation = EditText(this).apply {
                    hint = "Explain honestly (at least 15 characters)"
                    minLines = 2
                }
                AlertDialog.Builder(this)
                    .setTitle(reasons[index])
                    .setMessage("What would you change before the next prayer? This is test data only.")
                    .setView(explanation)
                    .setNegativeButton("Back", null)
                    .setPositiveButton("Save") { _, _ ->
                        if (explanation.text.toString().trim().length >= 15) {
                            record("test_not_prayed", reasons[index] + ": " + explanation.text.toString().trim())
                            stopRinging()
                            finish()
                        } else {
                            Toast.makeText(this, "Please write a more specific reason", Toast.LENGTH_LONG).show()
                            explain()
                        }
                    }.show()
            }.show()
    }

    private fun record(status: String, detail: String) {
        val prefs = getSharedPreferences("salahguard_test_records", MODE_PRIVATE)
        val timestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Date())
        prefs.edit().putString("latest", "$timestamp | $status | $detail").apply()
    }

    private fun startRinging() {
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            player = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                setDataSource(this@SalahGuardAccountabilityActivity, uri)
                isLooping = true
                prepare()
                start()
            }
        } catch (_: Exception) {
            Toast.makeText(this, "Alarm audio unavailable; check emulator alarm sound", Toast.LENGTH_LONG).show()
        }
    }

    private fun stopRinging() {
        getSystemService(NotificationManager::class.java).cancel(90501)
        runCatching { player?.stop() }
        player?.release()
        player = null
    }

    override fun onDestroy() {
        stopRinging()
        super.onDestroy()
    }
}
