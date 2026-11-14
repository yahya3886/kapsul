package com.kapsul.app.data

import java.util.UUID

/**
 * Geleceğe mühürlenmiş bir not.
 *
 * [unlockAt] anına kadar mühürlü kalır; [openedAt] yalnızca kullanıcı
 * mührü kırdığında dolar. Veriler yalnızca cihazda (JSON) saklanır.
 */
data class Capsule(
    val id: String = UUID.randomUUID().toString(),
    val title: String = "",
    val message: String = "",
    /** Kartta ve mühürde görünen sembol. */
    val emoji: String = "💌",
    /** Renk paletindeki konum (0..5). */
    val colorIndex: Int = 0,
    val createdAt: Long = System.currentTimeMillis(),
    /** Mührün kırılabileceği an. */
    val unlockAt: Long = 0L,
    /** Mühür kırıldığında dolar; null ise mühür hâlâ kapalı. */
    val openedAt: Long? = null,
) {
    val isSealed: Boolean get() = openedAt == null

    fun isUnlocked(now: Long = System.currentTimeMillis()): Boolean =
        openedAt != null || now >= unlockAt

    /** Mühür kapalı ve vakti gelmişse true — artık erken sayılmaz. */
    fun isOverdue(now: Long = System.currentTimeMillis()): Boolean =
        openedAt == null && now >= unlockAt
}
