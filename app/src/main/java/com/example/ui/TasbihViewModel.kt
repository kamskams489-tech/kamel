package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.data.BUILT_IN_ADHKAR
import com.example.data.CustomDhikrDoc
import com.example.data.DhikrItem
import com.example.data.LeaderboardEntryDoc
import com.example.data.TARGET_CYCLE_OPTIONS
import com.example.data.TasbihRepository
import com.example.data.UserSettingsDoc
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class StatsScope { MINE, ALL }
enum class StatsPeriod { TOTAL, TODAY }
enum class CompetitionFilter { TODAY, ISTIGHFAR, ALL_TIME }

data class LocalTasbihState(
    val activeDhikrId: String = "istighfar",
    val targetCount: Int = 33,
    val sessionCount: Int = 0,
    val soundEnabled: Boolean = true,
    val vibrateEnabled: Boolean = true,
    val displayName: String = "مسبّح بالله",
    val todayDate: String = "",
    val todayTotal: Long = 0L,
    val todayIstighfar: Long = 0L,
    val allTimeTotal: Long = 0L,
    val allTimeIstighfar: Long = 0L,
    val dhikrTotals: Map<String, Long> = emptyMap(),
    val dhikrToday: Map<String, Long> = emptyMap(),
    val syncing: Boolean = false,
    val hydratedFromCloud: Boolean = false,
    val targetJustCompleted: Boolean = false,
    val statusMessage: String? = null,
    val errorMessage: String? = null
)

data class TasbihUiState(
    val allAdhkar: List<DhikrItem> = BUILT_IN_ADHKAR,
    val activeDhikr: DhikrItem = BUILT_IN_ADHKAR.first(),
    val local: LocalTasbihState = LocalTasbihState(),
    val leaderboard: List<LeaderboardEntryDoc> = emptyList(),
    val statsScope: StatsScope = StatsScope.MINE,
    val statsPeriod: StatsPeriod = StatsPeriod.TOTAL,
    val competitionFilter: CompetitionFilter = CompetitionFilter.TODAY,
    val cloudError: String? = null,
    val loadingCloud: Boolean = true
)

private sealed interface CloudDataState {
    data object Loading : CloudDataState
    data class Ready(
        val settings: UserSettingsDoc?,
        val customAdhkar: List<CustomDhikrDoc>,
        val leaderboard: List<LeaderboardEntryDoc>
    ) : CloudDataState
    data class Error(val message: String) : CloudDataState
}

class TasbihViewModel(
    private val repository: TasbihRepository,
    private val currentUserId: String,
    private val initialDisplayName: String?
) : ViewModel() {

    private val _local = MutableStateFlow(
        LocalTasbihState(
            displayName = initialDisplayName?.trim()?.take(40).orEmpty().ifBlank { "مسبّح بالله" },
            todayDate = repository.currentDateKey()
        )
    )
    private val _statsScope = MutableStateFlow(StatsScope.MINE)
    private val _statsPeriod = MutableStateFlow(StatsPeriod.TOTAL)
    private val _competitionFilter = MutableStateFlow(CompetitionFilter.TODAY)

    private var syncJob: Job? = null
    private var hasPendingLocalTaps = false

    private val cloudState: StateFlow<CloudDataState> = combine(
        repository.observeUserSettings(currentUserId),
        repository.observeCustomAdhkar(currentUserId),
        repository.observePublicLeaderboard()
    ) { settings, customAdhkar, leaderboard ->
        CloudDataState.Ready(
            settings = settings,
            customAdhkar = customAdhkar,
            leaderboard = leaderboard
        ) as CloudDataState
    }.catch { error ->
        val firestoreError = generateSequence(error) { it.cause }
            .filterIsInstance<FirebaseFirestoreException>()
            .firstOrNull()
        if (firestoreError == null && error is CancellationException) throw error
        val rootError = firestoreError ?: error
        Log.w(TAG, "Error observing cloud tasbih streams", rootError)
        emit(CloudDataState.Error(rootError.localizedMessage ?: "تعذر الاتصال بالسحابة."))
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = CloudDataState.Loading
    )

    val uiState: StateFlow<TasbihUiState> = combine(
        cloudState,
        _local,
        _statsScope,
        _statsPeriod,
        _competitionFilter
    ) { cloud, local, scope, period, compFilter ->
        val customItems = when (cloud) {
            is CloudDataState.Ready -> cloud.customAdhkar.map { doc ->
                DhikrItem(
                    id = doc.id,
                    title = doc.title,
                    defaultTarget = doc.target.toInt(),
                    colorHex = doc.colorHex,
                    countsAsIstighfar = doc.countsAsIstighfar,
                    custom = true
                )
            }
            else -> emptyList()
        }
        val combinedAdhkar = BUILT_IN_ADHKAR + customItems
        val activeDhikr = combinedAdhkar.firstOrNull { it.id == local.activeDhikrId }
            ?: combinedAdhkar.first()

        val rawLeaderboard = (cloud as? CloudDataState.Ready)?.leaderboard ?: emptyList()
        val today = repository.currentDateKey()
        // Normalize leaderboard entries for today's date so stale daily counts display as 0
        val normalizedLeaderboard = rawLeaderboard.map { entry ->
            if (entry.todayDate == today) entry
            else entry.copy(todayTotal = 0L, todayIstighfar = 0L, dhikrToday = emptyMap())
        }

        TasbihUiState(
            allAdhkar = combinedAdhkar,
            activeDhikr = activeDhikr,
            local = local,
            leaderboard = normalizedLeaderboard,
            statsScope = scope,
            statsPeriod = period,
            competitionFilter = compFilter,
            cloudError = (cloud as? CloudDataState.Error)?.message,
            loadingCloud = cloud is CloudDataState.Loading
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000L),
        initialValue = TasbihUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureUserInitialized(initialDisplayName)
        }
        viewModelScope.launch {
            cloudState.collect { cloud ->
                if (cloud is CloudDataState.Ready) {
                    hydrateFromCloudIfNeeded(cloud)
                }
            }
        }
    }

    private fun hydrateFromCloudIfNeeded(cloud: CloudDataState.Ready) {
        val today = repository.currentDateKey()
        val myEntry = cloud.leaderboard.firstOrNull { it.userId == currentUserId }
        val settings = cloud.settings

        _local.update { current ->
            if (hasPendingLocalTaps) {
                // Keep optimistic local counts while a debounced sync is in flight
                current.copy(
                    displayName = settings?.displayName?.takeIf { it.isNotBlank() } ?: current.displayName,
                    hydratedFromCloud = true
                )
            } else {
                val entryIsToday = myEntry != null && myEntry.todayDate == today
                val cloudTodayTotal = if (entryIsToday) myEntry.todayTotal else 0L
                val cloudTodayIstighfar = if (entryIsToday) myEntry.todayIstighfar else 0L
                val cloudDhikrToday = if (entryIsToday) myEntry.dhikrToday else emptyMap()
                val cloudAllTimeTotal = myEntry?.allTimeTotal ?: 0L
                val cloudAllTimeIstighfar = myEntry?.allTimeIstighfar ?: 0L
                val cloudDhikrTotals = myEntry?.dhikrTotals ?: emptyMap()

                current.copy(
                    displayName = settings?.displayName?.takeIf { it.isNotBlank() }
                        ?: myEntry?.displayName?.takeIf { it.isNotBlank() }
                        ?: current.displayName,
                    soundEnabled = settings?.soundEnabled ?: current.soundEnabled,
                    vibrateEnabled = settings?.vibrateEnabled ?: current.vibrateEnabled,
                    activeDhikrId = if (!current.hydratedFromCloud && settings != null) {
                        settings.activeDhikrId
                    } else {
                        current.activeDhikrId
                    },
                    targetCount = if (!current.hydratedFromCloud && settings != null) {
                        settings.targetCount.toInt()
                    } else {
                        current.targetCount
                    },
                    todayDate = today,
                    todayTotal = maxOf(current.todayTotal, cloudTodayTotal),
                    todayIstighfar = maxOf(current.todayIstighfar, cloudTodayIstighfar),
                    allTimeTotal = maxOf(current.allTimeTotal, cloudAllTimeTotal),
                    allTimeIstighfar = maxOf(current.allTimeIstighfar, cloudAllTimeIstighfar),
                    dhikrTotals = mergeMaxMaps(current.dhikrTotals, cloudDhikrTotals),
                    dhikrToday = if (current.todayDate == today) {
                        mergeMaxMaps(current.dhikrToday, cloudDhikrToday)
                    } else {
                        cloudDhikrToday
                    },
                    hydratedFromCloud = true
                )
            }
        }
    }

    private fun mergeMaxMaps(localMap: Map<String, Long>, cloudMap: Map<String, Long>): Map<String, Long> {
        val merged = LinkedHashMap<String, Long>(cloudMap)
        for ((k, v) in localMap) {
            merged[k] = maxOf(merged[k] ?: 0L, v)
        }
        return merged
    }

    fun onTapTasbih() {
        val activeDhikr = uiState.value.activeDhikr
        val today = repository.currentDateKey()
        hasPendingLocalTaps = true

        _local.update { state ->
            val dateRolledOver = state.todayDate != today
            val baseTodayTotal = if (dateRolledOver) 0L else state.todayTotal
            val baseTodayIstighfar = if (dateRolledOver) 0L else state.todayIstighfar
            val baseDhikrToday = if (dateRolledOver) emptyMap() else state.dhikrToday

            val nextSession = state.sessionCount + 1
            val reachedTarget = state.targetCount > 0 && nextSession == state.targetCount

            val nextDhikrTotals = state.dhikrTotals.toMutableMap().apply {
                this[activeDhikr.id] = (this[activeDhikr.id] ?: 0L) + 1L
            }
            val nextDhikrToday = baseDhikrToday.toMutableMap().apply {
                this[activeDhikr.id] = (this[activeDhikr.id] ?: 0L) + 1L
            }

            state.copy(
                sessionCount = nextSession,
                todayDate = today,
                todayTotal = baseTodayTotal + 1L,
                todayIstighfar = baseTodayIstighfar + if (activeDhikr.countsAsIstighfar) 1L else 0L,
                allTimeTotal = state.allTimeTotal + 1L,
                allTimeIstighfar = state.allTimeIstighfar + if (activeDhikr.countsAsIstighfar) 1L else 0L,
                dhikrTotals = nextDhikrTotals,
                dhikrToday = nextDhikrToday,
                syncing = true,
                targetJustCompleted = reachedTarget
            )
        }

        scheduleDebouncedCloudSync()
    }

    fun clearTargetCompletedFlag() {
        _local.update { it.copy(targetJustCompleted = false) }
    }

    private fun scheduleDebouncedCloudSync() {
        syncJob?.cancel()
        syncJob = viewModelScope.launch {
            delay(550L)
            val snapshot = _local.value
            val result = repository.syncLeaderboardCounts(
                displayName = snapshot.displayName,
                todayDate = snapshot.todayDate,
                todayTotal = snapshot.todayTotal,
                todayIstighfar = snapshot.todayIstighfar,
                allTimeTotal = snapshot.allTimeTotal,
                allTimeIstighfar = snapshot.allTimeIstighfar,
                dhikrTotals = snapshot.dhikrTotals,
                dhikrToday = snapshot.dhikrToday
            )
            hasPendingLocalTaps = false
            _local.update { state ->
                state.copy(
                    syncing = false,
                    errorMessage = result.exceptionOrNull()?.localizedMessage
                )
            }
        }
    }

    fun selectDhikr(dhikr: DhikrItem) {
        _local.update { state ->
            state.copy(
                activeDhikrId = dhikr.id,
                targetCount = dhikr.defaultTarget,
                sessionCount = 0,
                targetJustCompleted = false
            )
        }
        viewModelScope.launch {
            repository.updateUserSettings(
                activeDhikrId = dhikr.id,
                targetCount = dhikr.defaultTarget
            )
        }
    }

    fun cycleTarget() {
        val current = _local.value.targetCount
        val idx = TARGET_CYCLE_OPTIONS.indexOf(current)
        val nextTarget = TARGET_CYCLE_OPTIONS[(idx + 1) % TARGET_CYCLE_OPTIONS.size]
        _local.update { it.copy(targetCount = nextTarget, targetJustCompleted = false) }
        viewModelScope.launch {
            repository.updateUserSettings(targetCount = nextTarget)
        }
    }

    fun toggleSound() {
        val next = !_local.value.soundEnabled
        _local.update { it.copy(soundEnabled = next) }
        viewModelScope.launch {
            repository.updateUserSettings(soundEnabled = next)
        }
    }

    fun toggleVibration() {
        val next = !_local.value.vibrateEnabled
        _local.update { it.copy(vibrateEnabled = next) }
        viewModelScope.launch {
            repository.updateUserSettings(vibrateEnabled = next)
        }
    }

    fun resetSession() {
        _local.update { it.copy(sessionCount = 0, targetJustCompleted = false) }
    }

    fun setStatsScope(scope: StatsScope) {
        _statsScope.value = scope
    }

    fun setStatsPeriod(period: StatsPeriod) {
        _statsPeriod.value = period
    }

    fun setCompetitionFilter(filter: CompetitionFilter) {
        _competitionFilter.value = filter
    }

    fun addCustomDhikr(
        title: String,
        target: Int,
        colorHex: String,
        countsAsIstighfar: Boolean
    ) {
        viewModelScope.launch {
            val result = repository.addCustomDhikr(title, target, colorHex, countsAsIstighfar)
            result.onSuccess { newId ->
                _local.update {
                    it.copy(
                        activeDhikrId = newId,
                        targetCount = target,
                        sessionCount = 0,
                        statusMessage = "تمت إضافة الذكر بنجاح"
                    )
                }
                repository.updateUserSettings(activeDhikrId = newId, targetCount = target)
            }.onFailure { err ->
                _local.update {
                    it.copy(errorMessage = err.localizedMessage ?: "تعذر إضافة الذكر.")
                }
            }
        }
    }

    fun deleteCustomDhikr(dhikrId: String) {
        viewModelScope.launch {
            val result = repository.deleteCustomDhikr(dhikrId)
            result.onSuccess {
                if (_local.value.activeDhikrId == dhikrId) {
                    _local.update {
                        it.copy(
                            activeDhikrId = "istighfar",
                            targetCount = 33,
                            sessionCount = 0
                        )
                    }
                    repository.updateUserSettings(activeDhikrId = "istighfar", targetCount = 33)
                }
            }.onFailure { err ->
                _local.update {
                    it.copy(errorMessage = err.localizedMessage ?: "تعذر حذف الذكر.")
                }
            }
        }
    }

    fun updateDisplayName(newName: String) {
        val trimmed = newName.trim().take(40)
        if (trimmed.isEmpty()) return
        _local.update { it.copy(displayName = trimmed) }
        viewModelScope.launch {
            val result = repository.updateDisplayName(trimmed)
            result.onSuccess {
                _local.update { it.copy(statusMessage = "تم تحديث الاسم بنجاح") }
            }.onFailure { err ->
                _local.update { it.copy(errorMessage = err.localizedMessage ?: "تعذر تحديث الاسم.") }
            }
        }
    }

    fun clearTransientMessages() {
        _local.update { it.copy(statusMessage = null, errorMessage = null) }
    }

    companion object {
        private const val TAG = "TasbihVM"

        fun provideFactory(
            databaseId: String,
            currentUserId: String,
            initialDisplayName: String?
        ): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                val db = FirebaseFirestore.getInstance(databaseId)
                TasbihViewModel(
                    repository = TasbihRepository(db),
                    currentUserId = currentUserId,
                    initialDisplayName = initialDisplayName
                )
            }
        }
    }
}
