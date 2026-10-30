package com.github.meypod.al_azan

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import com.example.data.model.SalahGuardStore

/** Real alarms are strictly separate from the 60-second developer test. */
class SalahGuardRealPrayerActivity : Activity() {
    private lateinit var prayer: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= 27) { setShowWhenLocked(true); setTurnScreenOn(true) }
        prayer = intent.getStringExtra("prayer") ?: "Fajr"
        if (prayer !in SalahGuardStore.names) { finish(); return }
        val layout = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(34, 60, 34, 34)
            setBackgroundColor(android.graphics.Color.rgb(5, 25, 29))
        }
        fun info(value: String, size: Float) {
            layout.addView(TextView(this).apply {
                text = value; textSize = size
                setTextColor(android.graphics.Color.WHITE)
                setPadding(8, 15, 8, 22)
                gravity = android.view.Gravity.CENTER
            })
        }
        fun button(text: String, action: () -> Unit) {
            layout.addView(Button(this).apply {
                this.text = text
                isAllCaps = false
                setOnClickListener { action() }
            }, LinearLayout.LayoutParams(-1, -2).apply { bottomMargin = 12 })
        }
        info("SALAHGUARD • PRAYER TIME", 15f)
        info(prayer, 38f)
        info("It is time to pray. Please answer truthfully.", 18f)
        button("I have prayed • Verify") { verify() }
        button("Snooze • 10 minutes") {
            SalahGuardPrayerScheduler.snooze(this, prayer)
            Toast.makeText(this, "Reminder snoozed for 10 minutes", Toast.LENGTH_LONG).show()
            finish()
        }
        button("I have not prayed • Explain") { explain() }
        button("Close • Leave unconfirmed") { finish() }
        setContentView(android.widget.ScrollView(this).apply { addView(layout) })
    }
    private fun verify() {
        val field = EditText(this).apply { hint = "Type I PRAYED"; setSingleLine(true) }
        AlertDialog.Builder(this).setTitle("Confirm $prayer prayer")
            .setMessage("Confirm only if you actually performed the prayer.")
            .setView(field).setNegativeButton("Cancel", null)
            .setPositiveButton("Confirm") { _, _ ->
                if (field.text.toString().trim() == "I PRAYED") {
                    getSharedPreferences("salahguard_v03", MODE_PRIVATE).edit()
                        .putString("status_${SalahGuardStore.today()}_$prayer", "Completed").apply()
                    finish()
                } else {
                    Toast.makeText(this, "Please type I PRAYED exactly", Toast.LENGTH_LONG).show()
                }
            }.show()
    }
    private fun explain() {
        val reasons = arrayOf("Work / busy", "Laziness", "Phone / social media",
            "Overslept", "Forgot", "Illness / travel", "Other")
        AlertDialog.Builder(this).setTitle("Why have you not prayed $prayer?")
            .setItems(reasons) { _, index ->
                val field = EditText(this).apply { hint = "Your honest explanation (15+ characters)" }
                AlertDialog.Builder(this).setTitle(reasons[index]).setView(field)
                    .setNegativeButton("Cancel", null)
                    .setPositiveButton("Save") { _, _ ->
                        val explanation = field.text.toString().trim()
                        if (explanation.length < 15) {
                            Toast.makeText(this, "Write at least 15 characters", Toast.LENGTH_LONG).show()
                        } else {
                            getSharedPreferences("salahguard_v03", MODE_PRIVATE).edit()
                                .putString("reason_${SalahGuardStore.today()}_$prayer",
                                    "${reasons[index]}: $explanation")
                                .putString("status_${SalahGuardStore.today()}_$prayer", "NotPrayed").apply()
                            // NotPrayed is a current declaration, not proof the prayer window expired.
                            finish()
                        }
                    }.show()
            }.show()
    }
}
