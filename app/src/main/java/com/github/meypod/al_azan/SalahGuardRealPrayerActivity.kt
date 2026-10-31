package com.github.meypod.al_azan

import androidx.activity.ComponentActivity
import android.app.NotificationManager
import android.media.AudioAttributes
import android.media.MediaPlayer
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import android.util.Log
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import com.example.data.model.SalahGuardStore
import com.example.ui.screens.AlarmScreen
import com.example.ui.screens.PrayedVerificationScreen
import com.example.ui.screens.NotPrayedReasonScreen
import com.example.ui.theme.SalahGuardTheme

/** Real prayer flow using the AI Studio UI; never used for developer test alarms. */
class SalahGuardRealPrayerActivity : ComponentActivity() {
    private var alarmPlayer: MediaPlayer? = null
    private val mainHandler = Handler(Looper.getMainLooper())
    private val autoStop = Runnable { stopAlarmSound() }

    private fun startAlarmSound() {
        if (SalahGuardStore.setting(this, "alarm_sound", "System alarm") == "Silent") return
        try {
            val uri = RingtoneManager.getActualDefaultRingtoneUri(this, RingtoneManager.TYPE_ALARM)
                ?: Settings.System.DEFAULT_ALARM_ALERT_URI
            alarmPlayer = MediaPlayer().apply {
                setAudioAttributes(AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_ALARM)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build())
                setDataSource(this@SalahGuardRealPrayerActivity, uri)
                isLooping = true
                prepare()
                start()
            }
            // Stop after 60 seconds to avoid unbounded ringing.
            mainHandler.postDelayed(autoStop, 60_000L)
            Log.i("SalahGuardPrayer", "real alarm ringing started")
        } catch (e: Exception) {
            Log.e("SalahGuardPrayer", "unable to play real alarm audio", e)
            stopAlarmSound()
        }
    }

    private fun stopAlarmSound() {
        mainHandler.removeCallbacks(autoStop)
        val playing = alarmPlayer
        alarmPlayer = null
        runCatching { playing?.stop() }
        playing?.release()
    }

    override fun onDestroy() {
        stopAlarmSound()
        super.onDestroy()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        val prayer = intent.getStringExtra("prayer") ?: run { finish(); return }
        if (prayer !in SalahGuardStore.names) { finish(); return }
        val index = SalahGuardStore.names.indexOf(prayer)
        val date = intent.getStringExtra("prayer_date")
            ?.let { runCatching { java.time.LocalDate.parse(it) }.getOrNull()?.toString() }
            ?: SalahGuardStore.today()
        Log.i("SalahGuardPrayer", "real alarm activity opened for $prayer on $date")
        startAlarmSound()
        setContent {
            var page by remember { mutableStateOf("alarm") }
            SalahGuardTheme {
                when (page) {
                    "verify" -> PrayedVerificationScreen(
                        prayerName = prayer,
                        onNavigateBack = { page = "alarm" },
                        onQuickSwitchClick = {},
                        onConfirmPrayed = { detail ->
                            stopAlarmSound()
                            getSharedPreferences("salahguard_v03", MODE_PRIVATE).edit()
                                .putString("status_${date}_${prayer}", "Completed")
                                .putString("prayed_detail_${date}_${prayer}", detail)
                                .putLong("verified_at_${date}_${prayer}", System.currentTimeMillis())
                                .remove("reason_${date}_${prayer}")
                                .commit()
                            Log.i("SalahGuardPrayer", "prayed confirmation saved for $prayer on $date")
                            Toast.makeText(this, "نماز کی تصدیق محفوظ ہو گئی", Toast.LENGTH_LONG).show()
                            getSystemService(NotificationManager::class.java).cancel(97000 + index)
                            finish()
                        }
                    )
                    "reason" -> NotPrayedReasonScreen(
                        onNavigateBack = { page = "alarm" },
                        onQuickSwitchClick = {},
                        onSubmitMissedReason = { reason, explanation ->
                            stopAlarmSound()
                            getSharedPreferences("salahguard_v03", MODE_PRIVATE).edit()
                                .putString("status_${date}_${prayer}", "NotPrayed")
                                .putString("reason_${date}_${prayer}", "$reason: $explanation")
                                .remove("prayed_detail_${date}_${prayer}")
                                .remove("verified_at_${date}_${prayer}")
                                .commit()
                            Log.i("SalahGuardPrayer", "missed reason saved for $prayer on $date")
                            Toast.makeText(this, "آپ کی وجہ محفوظ ہو گئی", Toast.LENGTH_LONG).show()
                            getSystemService(NotificationManager::class.java).cancel(97000 + index)
                            finish()
                        }
                    )
                    else -> AlarmScreen(
                        prayerName = prayer,
                        snoozeMinutes = SalahGuardStore.setting(this, "alarm_snooze", "10 minutes")
                            .substringBefore(" ").toIntOrNull() ?: 10,
                        onCloseClick = { stopAlarmSound(); finish() },
                        onPrayedClick = { stopAlarmSound(); page = "verify" },
                        onNotPrayedClick = { stopAlarmSound(); page = "reason" },
                        onSnoozeClick = {
                            stopAlarmSound()
                            SalahGuardStore.recordSnooze(this, prayer, date)
                            SalahGuardPrayerScheduler.snooze(this, prayer, date)
                            finish()
                        },
                        onViewPrayerTimeClick = { stopAlarmSound(); finish() }
                    )
                }
            }
        }
    }
}
