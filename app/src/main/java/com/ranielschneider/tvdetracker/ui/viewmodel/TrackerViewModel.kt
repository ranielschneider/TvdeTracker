package com.ranielschneider.tvdetracker.ui.viewmodel

import android.app.Application
import android.content.Intent
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.service.TrackerService
import com.ranielschneider.tvdetracker.ui.model.HomeUiState
import com.ranielschneider.tvdetracker.ui.model.TrackingState
import com.ranielschneider.tvdetracker.ui.utils.calculateAverageSpeedKmH
import com.ranielschneider.tvdetracker.ui.utils.calculateTotalTimeMs
import com.ranielschneider.tvdetracker.ui.utils.finishActiveSession
import com.ranielschneider.tvdetracker.ui.utils.isSameDay
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
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        refreshHomeData()
    }

    fun refreshHomeData() {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }

            runCatching {
                withContext(Dispatchers.IO) {
                    val sessions = trackerDao.buscarTodasSessoes()
                    val lastSession = sessions.maxByOrNull { it.horaInicio }

                    val lastSessionPoints = if (lastSession != null) {
                        trackerDao.buscarPontosDaSessao(lastSession.id)
                    } else {
                        emptyList()
                    }

                    val activeSession = sessions
                        .filter { it.horaFim == null }
                        .maxByOrNull { it.horaInicio }

                    val activePause = activeSession?.let { session ->
                        trackerDao.buscarPausaAtiva(session.id)
                    }

                    val trackingState = when {
                        activeSession == null -> TrackingState.STOPPED
                        activePause != null -> TrackingState.PAUSED
                        else -> TrackingState.TRACKING
                    }

                    val currentTime = System.currentTimeMillis()

                    val todaySessions = sessions.filter { session ->
                        isSameDay(
                            first = session.horaInicio,
                            second = currentTime
                        )
                    }

                    val totalTimeTodayMs = calculateTotalTimeMs(
                        sessions = todaySessions,
                        currentTimeMillis = currentTime
                    )

                    val drivingTimeTodayMs = todaySessions.sumOf { session ->
                        if (session.horaFim == null) {
                            val pauses = trackerDao.buscarPausasDaSessao(session.id)

                            val pauseTimeMs = pauses.sumOf { pause ->
                                (pause.fimPausa ?: currentTime) - pause.inicioPausa
                            }

                            ((currentTime - session.horaInicio) - pauseTimeMs)
                                .coerceAtLeast(0L)
                        } else {
                            session.horasConduzidasMs
                        }
                    }

                    val distanceTodayKm = todaySessions.sumOf { session ->
                        session.distanciaTotalMetros
                    } / 1000.0

                    val averageSpeedTodayKmH = calculateAverageSpeedKmH(
                        distanceKm = distanceTodayKm,
                        drivingTimeMs = drivingTimeTodayMs
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
                        averageSpeedTodayKmH = averageSpeedTodayKmH,
                        sessionsTodayCount = todaySessions.size,
                        errorMessage = null
                    )
                }
            }.onSuccess { newState ->
                _uiState.value = newState
            }.onFailure { error ->
                _uiState.update {
                    it.copy(
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

        _uiState.update {
            it.copy(
                trackingState = TrackingState.TRACKING,
                errorMessage = null
            )
        }
    }

    fun pauseTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_PAUSE
        )

        _uiState.update {
            it.copy(
                trackingState = TrackingState.PAUSED,
                errorMessage = null
            )
        }
    }

    fun resumeTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_RESUME
        )

        _uiState.update {
            it.copy(
                trackingState = TrackingState.TRACKING,
                errorMessage = null
            )
        }
    }

    fun stopTracking() {
        sendServiceAction(
            action = TrackerService.ACAO_STOP
        )

        _uiState.update {
            it.copy(
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
                _uiState.update {
                    it.copy(
                        errorMessage = error.message
                            ?: "Não foi possível encerrar a jornada."
                    )
                }
            }
        }
    }

    fun clearError() {
        _uiState.update {
            it.copy(errorMessage = null)
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