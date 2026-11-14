package com.kapsul.app.util

import java.util.Calendar

private val months =
    arrayOf("Oca", "Şub", "Mar", "Nis", "May", "Haz", "Tem", "Ağu", "Eyl", "Eki", "Kas", "Ara")

/** "14 Ara 2026 · 18:30" biçiminde yerel tarih-saat. */
fun formatDateTime(ts: Long): String {
    val c = Calendar.getInstance().apply { timeInMillis = ts }
    val day = c.get(Calendar.DAY_OF_MONTH)
    val month = months[c.get(Calendar.MONTH)]
    val year = c.get(Calendar.YEAR)
    val hour = c.get(Calendar.HOUR_OF_DAY).toString().padStart(2, '0')
    val minute = c.get(Calendar.MINUTE).toString().padStart(2, '0')
    return "$day $month $year · $hour:$minute"
}

/** Hedefe kalan süreyi Türkçe ve kısa biçimde yazar: "3 gün 5 saat", "12 dk 04 sn". */
fun remainingText(target: Long, now: Long = System.currentTimeMillis()): String {
    val totalSeconds = ((target - now) / 1000).coerceAtLeast(0)
    val days = totalSeconds / 86_400
    val hours = (totalSeconds % 86_400) / 3_600
    val minutes = (totalSeconds % 3_600) / 60
    val seconds = totalSeconds % 60
    return when {
        days > 0 -> "$days gün $hours saat"
        hours > 0 -> "$hours saat $minutes dk"
        minutes > 0 -> "$minutes dk ${seconds.toString().padStart(2, '0')} sn"
        else -> "$seconds sn"
    }
}
