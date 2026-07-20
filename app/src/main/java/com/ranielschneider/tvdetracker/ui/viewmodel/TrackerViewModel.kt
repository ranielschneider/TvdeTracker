package com.ranielschneider.tvdetracker.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.service.TrackerService
import com.ranielschneider.tvdetracker.ui.model.DailyDrivingTime
import com.ranielschneider.tvdetracker.ui.model.HomeUiState
import com.ranielschneider.tvdetracker.ui.model.TrackingState
import com.ranielschneider.tvdetracker.ui.utils.calculateAverageSpeedKmH
import com.ranielschneider.tvdetracker.ui.utils.calculateTotalTimeMs
import com.ranielschneider.tvdetracker.ui.utils.finishActiveSession
import com.ranielschneider.tvdetracker.ui.utils.isSameDay
import java.util.Calendar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TrackerViewModel(
    application: Application
) : AndroidViewModel(application) {

    private val appContext = application.applicationContext

    private val trackerDao by lazy {
        TrackerDatabase
            .getDatabase(appContext)
            .trackerDao()
    }

    private val _uiState = MutableStateFlow(HomeUiState())

    val uiState: StateFlow<HomeUiState> =
        _uiState.asStateFlow()

    init {
        refreshHomeData()
    }

    fun refreshHomeData() {
        viewModelScope.launch {
            _uiState.update { currentState ->
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    val sessions = trackerDao
                        .buscarTodasSessoes()
                        .sortedBy { session ->
                            session.horaInicio
                        }

                    val currentTime = System.currentTimeMillis()

                    val lastSession = sessions
                        .maxByOrNull { session ->
                            session.horaInicio
                        }

                    val lastSessionPoints =
                        if (lastSession != null) {
                            trackerDao.buscarPontosDaSessao(
                                lastSession.id
                            )
                        } else {
                            emptyList()
                        }

                    val activeSession = sessions
                        .filter { session ->
                            session.horaFim == null
                        }
                        .maxByOrNull { session ->
                            session.horaInicio
                        }

                    val activePause = activeSession?.let { session ->
                        trackerDao.buscarPausaAtiva(
                            session.id
                        )
                    }

                    val trackingState = when {
                        activeSession == null ->
                            TrackingState.STOPPED

                        activePause != null ->
                            TrackingState.PAUSED

                        else ->
                            TrackingState.TRACKING
                    }

                    val drivingTimeBySessionId =
                        sessions.associate { session ->
                            session.id to calculateSessionDrivingTime(
                                session = session,
                                currentTimeMillis = currentTime
                            )
                        }

                    val todaySessions = sessions.filter { session ->
                        isSameDay(
                            first = session.horaInicio,
                            second = currentTime
                        )
                    }

                    val totalTimeTodayMs =
                        calculateTotalTimeMs(
                            sessions = todaySessions,
                            currentTimeMillis = currentTime
                        )

                    val drivingTimeTodayMs =
                        todaySessions.sumOf { session ->
                            drivingTimeBySessionId[
                                session.id
                            ] ?: 0L
                        }

                    val distanceTodayKm =
                        todaySessions.sumOf { session ->
                            session.distanciaTotalMetros
                        } / 1_000.0

                    val averageSpeedTodayKmH =
                        calculateAverageSpeedKmH(
                            distanceKm = distanceTodayKm,
                            drivingTimeMs = drivingTimeTodayMs
                        )

                    val drivingTimeLast7Days =
                        buildCurrentWeekDrivingTime(
                            sessions = sessions,
                            drivingTimeBySessionId =
                                drivingTimeBySessionId,
                            currentTimeMillis = currentTime
                        )

                    HomeUiState(
                        isLoading = false,
                        trackingState = trackingState,
                        sessions = sessions,
                        lastSession = lastSession,
                        lastSessionPoints = lastSessionPoints,
                        totalTimeTodayMs = totalTimeTodayMs,
                        drivingTimeTodayMs = drivingTimeTodayMs,
                        distanceTodayKm = distanceTodayKm,
                        averageSpeedTodayKmH =
                            averageSpeedTodayKmH,
                        sessionsTodayCount =
                            todaySessions.size,
                        drivingTimeLast7Days =
                            drivingTimeLast7Days,
                        errorMessage = null
                    )
                }
            }.onSuccess { newState ->
                _uiState.value = newState
            }.onFailure { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        isLoading = false,
                        errorMessage = error.message
                            ?: "Não foi possível carregar os dados."
                    )
                }
            }
        }
    }

    fun startTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_START,
            foreground = true
        )

        _uiState.update { currentState ->
            currentState.copy(
                trackingState = TrackingState.TRACKING,
                errorMessage = null
            )
        }
    }

    fun pauseTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_PAUSE
        )

        _uiState.update { currentState ->
            currentState.copy(
                trackingState = TrackingState.PAUSED,
                errorMessage = null
            )
        }
    }

    fun resumeTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_RESUME
        )

        _uiState.update { currentState ->
            currentState.copy(
                trackingState = TrackingState.TRACKING,
                errorMessage = null
            )
        }
    }

    fun stopTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_STOP
        )

        _uiState.update { currentState ->
            currentState.copy(
                trackingState = TrackingState.STOPPED,
                errorMessage = null
            )
        }

        viewModelScope.launch {
            runCatching {
                finishActiveSession(appContext)
            }.onSuccess {
                refreshHomeData()
            }.onFailure { error ->
                _uiState.update { currentState ->
                    currentState.copy(
                        errorMessage = error.message
                            ?: "Não foi possível encerrar a jornada."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update { currentState ->
            currentState.copy(
                errorMessage = null
            )
        }
    }

    private suspend fun calculateSessionDrivingTime(
        session: Sessao,
        currentTimeMillis: Long
    ): Long {
        if (session.horaFim != null) {
            return session.horasConduzidasMs
                .coerceAtLeast(0L)
        }

        val pauses = trackerDao.buscarPausasDaSessao(
            session.id
        )

        val totalPauseTimeMs = pauses.sumOf { pause ->
            val pauseEnd =
                pause.fimPausa ?: currentTimeMillis

            (pauseEnd - pause.inicioPausa)
                .coerceAtLeast(0L)
        }

        return (
                currentTimeMillis -
                        session.horaInicio -
                        totalPauseTimeMs
                ).coerceAtLeast(0L)
    }

    private fun buildCurrentWeekDrivingTime(
        sessions: List<Sessao>,
        drivingTimeBySessionId: Map<Long, Long>,
        currentTimeMillis: Long
    ): List<DailyDrivingTime> {
        val today = Calendar.getInstance().apply {
            timeInMillis = currentTimeMillis
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }

        val monday = today.clone() as Calendar

        val daysSinceMonday = when (
            monday.get(Calendar.DAY_OF_WEEK)
        ) {
            Calendar.SUNDAY -> 6
            else -> monday.get(Calendar.DAY_OF_WEEK) -
                    Calendar.MONDAY
        }

        monday.add(
            Calendar.DAY_OF_YEAR,
            -daysSinceMonday
        )

        val dayLabels = listOf(
            "Seg",
            "Ter",
            "Qua",
            "Qui",
            "Sex",
            "Sáb",
            "Dom"
        )

        return dayLabels.mapIndexed { index, dayLabel ->
            val dayCalendar =
                monday.clone() as Calendar

            dayCalendar.add(
                Calendar.DAY_OF_YEAR,
                index
            )

            val dayStartMs = dayCalendar.timeInMillis

            val dayDrivingTimeMs =
                sessions
                    .filter { session ->
                        isSameDay(
                            first = session.horaInicio,
                            second = dayStartMs
                        )
                    }
                    .sumOf { session ->
                        drivingTimeBySessionId[
                            session.id
                        ] ?: 0L
                    }

            DailyDrivingTime(
                dayLabel = dayLabel,
                dateStartMs = dayStartMs,
                drivingTimeMs = dayDrivingTimeMs,
                isToday = isSameDay(
                    first = dayStartMs,
                    second = currentTimeMillis
                )
            )
        }
    }

    private fun sendServiceAction(
        action: String,
        foreground: Boolean = false
    ) {
        val intent = Intent(
            appContext,
            TrackerService::class.java
        ).apply {
            this.action = action
        }

        if (foreground) {
            ContextCompat.startForegroundService(
                appContext,
                intent
            )
        } else {
            appContext.startService(intent)
        }
    }
}