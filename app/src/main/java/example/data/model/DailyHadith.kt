package com.example.data.model

import android.content.Context
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL
import java.time.LocalDate

/** Free community edition; fetch once per local calendar day, cache and fail safely. */
object DailyHadith {
    private val numbers = listOf(1, 13, 52, 54, 73, 6464, 6018, 10, 15, 16, 33, 40)
    fun current(context: Context): Pair<String,String> {
        val p = context.getSharedPreferences("salahguard_hadith", Context.MODE_PRIVATE)
        val fallback = "The most beloved deeds to Allah are those done regularly, even if small." to "Sahih al-Bukhari 6464"
        return (p.getString("text", null) ?: fallback.first) to
            (p.getString("source", null) ?: fallback.second)
    }
    fun refresh(context: Context, onResult: (Pair<String,String>) -> Unit) {
        val today = LocalDate.now().toString()
        val p = context.getSharedPreferences("salahguard_hadith", Context.MODE_PRIVATE)
        if (p.getString("date", "") == today && !p.getString("text", "").isNullOrBlank()) {
            onResult(current(context)); return
        }
        Thread {
            val number = numbers[(LocalDate.now().toEpochDay() % numbers.size).toInt().let {
                if (it < 0) it + numbers.size else it
            }]
            var result: Pair<String,String>? = null
            for (extension in listOf("min.json", "json")) {
                if (result != null) break
                val url = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/eng-bukhari/$number.$extension"
                result = try {
                val connection = URL(url).openConnection() as HttpURLConnection
                connection.connectTimeout = 6500
                connection.readTimeout = 6500
                try {
                    if (connection.responseCode !in 200..299) null
                    else {
                        val raw = connection.inputStream.bufferedReader().use { it.readText() }
                        val json = JSONObject(raw)
                        val hadith = json.optJSONArray("hadiths")?.optJSONObject(0)
                            ?: json.optJSONObject("hadith") ?: json
                        val value = hadith?.optString("text", "")?.trim().orEmpty()
                        if (value.length in 30..1400) {
                            val citation = "Sahih al-Bukhari $number • community translation"
                            p.edit().putString("text", value).putString("source", citation)
                                .putString("date", today).apply()
                            value to citation
                        } else null
                    }
                } finally { connection.disconnect() }
                } catch (_: Exception) { null }
            }
            android.os.Handler(android.os.Looper.getMainLooper()).post {
                onResult(result ?: current(context))
            }
        }.start()
    }
}
