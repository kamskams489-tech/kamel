package com.example.data

import androidx.annotation.Keep
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

data class DhikrItem(
    val id: String,
    val title: String,
    val defaultTarget: Int = 33,
    val colorHex: String = "#D4AF37",
    val countsAsIstighfar: Boolean = false,
    val custom: Boolean = false
)

val BUILT_IN_ADHKAR: List<DhikrItem> = listOf(
    DhikrItem(
        id = "istighfar",
        title = "أستغفر الله",
        defaultTarget = 33,
        colorHex = "#D4AF37",
        countsAsIstighfar = true
    ),
    DhikrItem(
        id = "subhanallah",
        title = "سبحان الله",
        defaultTarget = 33,
        colorHex = "#34D399",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "alhamdulillah",
        title = "الحمد لله",
        defaultTarget = 33,
        colorHex = "#60A5FA",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "allahuakbar",
        title = "الله أكبر",
        defaultTarget = 34,
        colorHex = "#F59E0B",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "tahlil",
        title = "لا إله إلا الله",
        defaultTarget = 100,
        colorHex = "#A78BFA",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "salawat",
        title = "اللهم صلِّ وسلم على نبينا محمد",
        defaultTarget = 100,
        colorHex = "#F472B6",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "subhanallah_wabihamdihi",
        title = "سبحان الله وبحمده",
        defaultTarget = 100,
        colorHex = "#2DD4BF",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "hawqala",
        title = "لا حول ولا قوة إلا بالله",
        defaultTarget = 33,
        colorHex = "#FB923C",
        countsAsIstighfar = false
    ),
    DhikrItem(
        id = "istighfar_kabir",
        title = "أستغفر الله العظيم وأتوب إليه",
        defaultTarget = 100,
        colorHex = "#F3D26A",
        countsAsIstighfar = true
    )
)

val TARGET_CYCLE_OPTIONS = listOf(33, 99, 100, 1000, 0)

val CUSTOM_DHIKR_COLORS = listOf(
    "#D4AF37",
    "#34D399",
    "#60A5FA",
    "#F59E0B",
    "#A78BFA",
    "#F472B6",
    "#2DD4BF",
    "#FB923C"
)

@Keep
@IgnoreExtraProperties
data class UserSettingsDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val activeDhikrId: String = "istighfar",
    val targetCount: Long = 33L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class CustomDhikrDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val target: Long = 33L,
    val colorHex: String = "#D4AF37",
    val countsAsIstighfar: Boolean = false,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@Keep
@IgnoreExtraProperties
data class LeaderboardEntryDoc(
    @DocumentId val id: String = "",
    val userId: String = "",
    val displayName: String = "",
    val visibility: String = "public",
    val todayDate: String = "",
    val todayTotal: Long = 0L,
    val todayIstighfar: Long = 0L,
    val allTimeTotal: Long = 0L,
    val allTimeIstighfar: Long = 0L,
    val dhikrTotals: Map<String, Long> = emptyMap(),
    val dhikrToday: Map<String, Long> = emptyMap(),
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)
