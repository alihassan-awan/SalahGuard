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
        val fallback = "اللہ کو سب سے زیادہ پسندیدہ عمل وہ ہے جو پابندی سے کیا جائے، چاہے تھوڑا ہی ہو۔" to "Sahih al-Bukhari 6464"
        val savedText = p.getString("text_ur", null)
        val savedSource = p.getString("source_ur", null)
        val savedDate = p.getString("date_ur", null)
        val citation = (savedSource ?: fallback.second) +
            if (savedText != null && savedDate != LocalDate.now().toString()) " • سابقہ محفوظ شدہ حدیث" else ""
        return (savedText ?: fallback.first) to citation
    }
    fun refresh(context: Context, onResult: (Pair<String,String>) -> Unit) {
        val today = LocalDate.now().toString()
        val p = context.getSharedPreferences("salahguard_hadith", Context.MODE_PRIVATE)
        if (p.getString("date_ur", "") == today && !p.getString("text_ur", "").isNullOrBlank()) {
            onResult(current(context)); return
        }
        Thread {
            val number = numbers[(LocalDate.now().toEpochDay() % numbers.size).toInt().let {
                if (it < 0) it + numbers.size else it
            }]
            var result: Pair<String,String>? = null
            for (extension in listOf("min.json", "json")) {
                if (result != null) break
                val url = "https://cdn.jsdelivr.net/gh/fawazahmed0/hadith-api@1/editions/urd-bukhari/$number.$extension"
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
                        if (value.length in 30..2200 && value.any { it in '\u0600'..'\u06FF' }) {
                            val citation = "صحیح بخاری، حدیث $number • اردو ترجمہ"
                            p.edit().putString("text_ur", value).putString("source_ur", citation)
                                .putString("date_ur", today).apply()
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
