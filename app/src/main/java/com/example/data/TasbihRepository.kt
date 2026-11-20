package com.example.data

import android.content.Context
import com.example.R
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import com.google.firebase.firestore.snapshots
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class TasbihRepository(
    private val db: FirebaseFirestore,
    private val auth: FirebaseAuth = Firebase.auth
) {
    constructor(context: Context) : this(
        FirebaseFirestore.getInstance(
            context.applicationContext.getString(R.string.firestore_database_id)
        )
    )

    private fun requireUserId(): String {
        return auth.currentUser?.uid?.takeIf { it.isNotBlank() }
            ?: throw IllegalStateException("يجب تسجيل الدخول بحساب Google أولاً.")
    }

    fun currentDateKey(): String {
        val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return formatter.format(Date())
    }

    private fun sanitizeDisplayName(raw: String?): String {
        val cleaned = raw?.trim().orEmpty().take(40)
        return cleaned.ifBlank { "مسبّح بالله" }
    }

    private fun parseLongMap(raw: Any?): Map<String, Long> {
        val map = raw as? Map<*, *> ?: return emptyMap()
        val result = LinkedHashMap<String, Long>()
        for ((k, v) in map) {
            val key = k as? String ?: continue
            val value = (v as? Number)?.toLong() ?: continue
            if (value >= 0L) {
                result[key] = value
            }
        }
        return result
    }

    private fun parseLeaderboardSnapshot(doc: DocumentSnapshot): LeaderboardEntryDoc? {
        if (!doc.exists()) return null
        val data = doc.data ?: return null
        return LeaderboardEntryDoc(
            id = doc.id,
            userId = data["userId"] as? String ?: doc.id,
            displayName = data["displayName"] as? String ?: "مسبّح بالله",
            visibility = data["visibility"] as? String ?: "public",
            todayDate = data["todayDate"] as? String ?: "",
            todayTotal = (data["todayTotal"] as? Number)?.toLong() ?: 0L,
            todayIstighfar = (data["todayIstighfar"] as? Number)?.toLong() ?: 0L,
            allTimeTotal = (data["allTimeTotal"] as? Number)?.toLong() ?: 0L,
            allTimeIstighfar = (data["allTimeIstighfar"] as? Number)?.toLong() ?: 0L,
            dhikrTotals = parseLongMap(data["dhikrTotals"]),
            dhikrToday = parseLongMap(data["dhikrToday"]),
            createdAt = doc.getTimestamp("createdAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE),
            updatedAt = doc.getTimestamp("updatedAt", DocumentSnapshot.ServerTimestampBehavior.ESTIMATE)
        )
    }

    fun observeUserSettings(userId: String): Flow<UserSettingsDoc?> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user settings observation." }
        val path = "users/$uid"
        emitAll(
            db.collection("users").document(uid)
                .snapshots()
                .map { snapshot ->
                    if (!snapshot.exists()) null
                    else snapshot.toObject(
                        UserSettingsDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.GET, path)
                    }
                    throw error
                }
        )
    }

    fun observeCustomAdhkar(userId: String): Flow<List<CustomDhikrDoc>> = flow {
        val uid = requireUserId()
        check(uid == userId) { "Refusing cross-user custom adhkar observation." }
        val path = "users/$uid/custom_adhkar"
        emitAll(
            db.collection("users").document(uid).collection("custom_adhkar")
                .snapshots()
                .map { snapshot ->
                    snapshot.toObjects(
                        CustomDhikrDoc::class.java,
                        DocumentSnapshot.ServerTimestampBehavior.ESTIMATE
                    )
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    fun observePublicLeaderboard(): Flow<List<LeaderboardEntryDoc>> = flow {
        val uid = requireUserId()
        val path = "leaderboard"
        emitAll(
            db.collection("leaderboard")
                .whereEqualTo("visibility", "public")
                .limit(100)
                .snapshots()
                .map { querySnapshot ->
                    querySnapshot.documents.mapNotNull { parseLeaderboardSnapshot(it) }
                }
                .catch { error ->
                    val firestoreError = generateSequence(error) { it.cause }
                        .filterIsInstance<FirebaseFirestoreException>()
                        .firstOrNull()
                    if (firestoreError == null && error is CancellationException) throw error
                    val exceptionToLog = firestoreError ?: (error as? Exception)
                    if (exceptionToLog != null && auth.currentUser?.uid == uid) {
                        handleFirestoreError(exceptionToLog, OperationType.LIST, path)
                    }
                    throw error
                }
        )
    }

    suspend fun ensureUserInitialized(defaultDisplayName: String?): Result<Unit> {
        val uid = try {
            requireUserId()
        } catch (e: Exception) {
            return Result.failure(e)
        }
        val safeName = sanitizeDisplayName(defaultDisplayName)
        val today = currentDateKey()

        val userRef = db.collection("users").document(uid)
        val leaderboardRef = db.collection("leaderboard").document(uid)

        var currentPath = "users/$uid"
        var currentOp = OperationType.GET
        return try {
            val userSnap = userRef.get().await()
            if (!userSnap.exists()) {
                currentOp = OperationType.CREATE
                val userPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "soundEnabled" to true,
                    "vibrateEnabled" to true,
                    "activeDhikrId" to "istighfar",
                    "targetCount" to 33L,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                userRef.set(userPayload).await()
            }

            currentPath = "leaderboard/$uid"
            currentOp = OperationType.GET
            val lbSnap = leaderboardRef.get().await()
            if (!lbSnap.exists()) {
                currentOp = OperationType.CREATE
                val lbPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "visibility" to "public",
                    "todayDate" to today,
                    "todayTotal" to 0L,
                    "todayIstighfar" to 0L,
                    "allTimeTotal" to 0L,
                    "allTimeIstighfar" to 0L,
                    "dhikrTotals" to emptyMap<String, Long>(),
                    "dhikrToday" to emptyMap<String, Long>(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                leaderboardRef.set(lbPayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, currentOp, currentPath)
            Result.failure(e)
        }
    }

    suspend fun syncLeaderboardCounts(
        displayName: String,
        todayDate: String,
        todayTotal: Long,
        todayIstighfar: Long,
        allTimeTotal: Long,
        allTimeIstighfar: Long,
        dhikrTotals: Map<String, Long>,
        dhikrToday: Map<String, Long>
    ): Result<Unit> {
        var docPath = "leaderboard/unauthenticated"
        var currentOp = OperationType.GET
        return try {
            val uid = requireUserId()
            docPath = "leaderboard/$uid"
            val docRef = db.collection("leaderboard").document(uid)

            val clampedTotals = dhikrTotals.entries
                .sortedByDescending { it.value }
                .take(40)
                .associate { it.key to it.value.coerceAtLeast(0L) }
            val clampedToday = dhikrToday.entries
                .sortedByDescending { it.value }
                .take(40)
                .associate { it.key to it.value.coerceAtLeast(0L) }

            val safeName = sanitizeDisplayName(displayName)
            val safeDate = if (todayDate.matches(Regex("^[0-9]{4}-[0-9]{2}-[0-9]{2}$"))) {
                todayDate
            } else {
                currentDateKey()
            }

            val existingSnap = docRef.get().await()
            if (!existingSnap.exists()) {
                currentOp = OperationType.CREATE
                val createPayload = mapOf(
                    "userId" to uid,
                    "displayName" to safeName,
                    "visibility" to "public",
                    "todayDate" to safeDate,
                    "todayTotal" to todayTotal.coerceIn(0L, 10_000_000L),
                    "todayIstighfar" to todayIstighfar.coerceIn(0L, 10_000_000L),
                    "allTimeTotal" to allTimeTotal.coerceIn(0L, 100_000_000L),
                    "allTimeIstighfar" to allTimeIstighfar.coerceIn(0L, 100_000_000L),
                    "dhikrTotals" to clampedTotals,
                    "dhikrToday" to clampedToday,
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(createPayload).await()
            } else {
                currentOp = OperationType.UPDATE
                val updatePayload = mapOf(
                    "displayName" to safeName,
                    "visibility" to "public",
                    "todayDate" to safeDate,
                    "todayTotal" to todayTotal.coerceIn(0L, 10_000_000L),
                    "todayIstighfar" to todayIstighfar.coerceIn(0L, 10_000_000L),
                    "allTimeTotal" to allTimeTotal.coerceIn(0L, 100_000_000L),
                    "allTimeIstighfar" to allTimeIstighfar.coerceIn(0L, 100_000_000L),
                    "dhikrTotals" to clampedTotals,
                    "dhikrToday" to clampedToday,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.update(updatePayload).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, currentOp, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateUserSettings(
        soundEnabled: Boolean? = null,
        vibrateEnabled: Boolean? = null,
        activeDhikrId: String? = null,
        targetCount: Int? = null,
        displayName: String? = null
    ): Result<Unit> {
        var docPath = "users/unauthenticated"
        var currentOp = OperationType.UPDATE
        return try {
            val uid = requireUserId()
            docPath = "users/$uid"
            val docRef = db.collection("users").document(uid)
            val snap = docRef.get().await()
            if (!snap.exists()) {
                currentOp = OperationType.CREATE
                val payload = mapOf(
                    "userId" to uid,
                    "displayName" to sanitizeDisplayName(displayName ?: auth.currentUser?.displayName),
                    "soundEnabled" to (soundEnabled ?: true),
                    "vibrateEnabled" to (vibrateEnabled ?: true),
                    "activeDhikrId" to (activeDhikrId?.takeIf { it.matches(Regex("^[a-zA-Z0-9_\\-]{1,64}$")) } ?: "istighfar"),
                    "targetCount" to (targetCount ?: 33).coerceIn(0, 100_000).toLong(),
                    "createdAt" to FieldValue.serverTimestamp(),
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                docRef.set(payload).await()
            } else {
                val updates = mutableMapOf<String, Any>(
                    "updatedAt" to FieldValue.serverTimestamp()
                )
                if (soundEnabled != null) updates["soundEnabled"] = soundEnabled
                if (vibrateEnabled != null) updates["vibrateEnabled"] = vibrateEnabled
                if (activeDhikrId != null && activeDhikrId.matches(Regex("^[a-zA-Z0-9_\\-]{1,64}$"))) {
                    updates["activeDhikrId"] = activeDhikrId
                }
                if (targetCount != null) {
                    updates["targetCount"] = targetCount.coerceIn(0, 100_000).toLong()
                }
                if (displayName != null) {
                    updates["displayName"] = sanitizeDisplayName(displayName)
                }
                docRef.update(updates).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, currentOp, docPath)
            Result.failure(e)
        }
    }

    suspend fun updateDisplayName(newDisplayName: String): Result<Unit> {
        val safeName = sanitizeDisplayName(newDisplayName)
        var docPath = "users/unauthenticated"
        return try {
            val uid = requireUserId()
            docPath = "users/$uid"
            db.collection("users").document(uid).update(
                mapOf(
                    "displayName" to safeName,
                    "updatedAt" to FieldValue.serverTimestamp()
                )
            ).await()

            val lbRef = db.collection("leaderboard").document(uid)
            val lbSnap = lbRef.get().await()
            if (lbSnap.exists()) {
                docPath = "leaderboard/$uid"
                lbRef.update(
                    mapOf(
                        "displayName" to safeName,
                        "updatedAt" to FieldValue.serverTimestamp()
                    )
                ).await()
            }
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.UPDATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun addCustomDhikr(
        title: String,
        target: Int,
        colorHex: String,
        countsAsIstighfar: Boolean
    ): Result<String> {
        var docPath = "users/unauthenticated/custom_adhkar"
        return try {
            val uid = requireUserId()
            val cleanedTitle = title.trim().take(120)
            require(cleanedTitle.isNotEmpty()) { "يرجى كتابة نص الذكر." }
            val safeColor = if (colorHex.matches(Regex("^#[0-9a-fA-F]{6}$"))) colorHex else "#D4AF37"
            val safeTarget = target.coerceIn(0, 100_000).toLong()

            val docRef = db.collection("users").document(uid)
                .collection("custom_adhkar").document()
            docPath = docRef.path

            val payload = mapOf(
                "userId" to uid,
                "title" to cleanedTitle,
                "target" to safeTarget,
                "colorHex" to safeColor,
                "countsAsIstighfar" to countsAsIstighfar,
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp()
            )
            docRef.set(payload).await()
            Result.success(docRef.id)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.CREATE, docPath)
            Result.failure(e)
        }
    }

    suspend fun deleteCustomDhikr(dhikrId: String): Result<Unit> {
        var docPath = "users/unauthenticated/custom_adhkar/$dhikrId"
        return try {
            val uid = requireUserId()
            require(dhikrId.isNotBlank()) { "معرّف الذكر غير صالح." }
            val docRef = db.collection("users").document(uid)
                .collection("custom_adhkar").document(dhikrId)
            docPath = docRef.path
            docRef.delete().await()
            Result.success(Unit)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            handleFirestoreError(e, OperationType.DELETE, docPath)
            Result.failure(e)
        }
    }
}
