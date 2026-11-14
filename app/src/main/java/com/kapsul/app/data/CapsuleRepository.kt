package com.kapsul.app.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kapsul.app.R
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

/**
 * Kapsülleri cihazda (JSON dosyası) saklayan tek veri kaynağı.
 * Hiçbir veri ağ üzerinden gönderilmez.
 */
class CapsuleRepository private constructor(context: Context) {

    companion object {
        @Volatile
        private var instance: CapsuleRepository? = null

        /** Uygulama ömrünce tek örnek — VM'ler aynı durumu paylaşır. */
        fun shared(context: Context): CapsuleRepository =
            instance ?: synchronized(this) {
                instance ?: CapsuleRepository(context.applicationContext).also { instance = it }
            }
    }

    private val file = File(context.filesDir, "capsules.json")
    private val gson = Gson()
    private val _capsules = MutableStateFlow<List<Capsule>>(emptyList())
    val capsules: StateFlow<List<Capsule>> = _capsules

    init {
        val loaded = if (file.exists()) {
            runCatching {
                val type = object : TypeToken<List<Capsule>>() {}.type
                gson.fromJson<List<Capsule>>(file.readText(), type) ?: emptyList()
            }.getOrDefault(emptyList())
        } else {
            emptyList()
        }

        _capsules.value = if (loaded.isEmpty() && !file.exists()) {
            // İlk açılış: deneyim için 3 dakika içinde açılacak örnek kapsül.
            listOf(
                Capsule(
                    title = context.getString(R.string.demo_title),
                    message = context.getString(R.string.demo_message),
                    emoji = "⏳",
                    colorIndex = 2,
                    unlockAt = System.currentTimeMillis() + 3 * 60_000L,
                )
            ).also { persist(it) }
        } else {
            loaded
        }
    }

    fun upsert(capsule: Capsule) {
        val current = _capsules.value
        val next = if (current.any { it.id == capsule.id }) {
            current.map { if (it.id == capsule.id) capsule else it }
        } else {
            current + capsule
        }
        _capsules.value = next
        persist(next)
    }

    fun open(id: String) {
        val now = System.currentTimeMillis()
        val next = _capsules.value.map {
            if (it.id == id && it.openedAt == null) it.copy(openedAt = now) else it
        }
        _capsules.value = next
        persist(next)
    }

    fun remove(id: String) {
        val next = _capsules.value.filterNot { it.id == id }
        _capsules.value = next
        persist(next)
    }

    private fun persist(list: List<Capsule>) {
        runCatching { file.writeText(gson.toJson(list)) }
    }
}
