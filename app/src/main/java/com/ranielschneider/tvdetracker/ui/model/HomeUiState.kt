package com.ranielschneider.tvdetracker.ui.model

import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.Sessao

data class HomeUiState(
    val isLoading: Boolean = true,
    val trackingState: TrackingState = TrackingState.STOPPED,
    val sessions: List<Sessao> = emptyList(),
    val lastSession: Sessao? = null,
    val lastSessionPoints: List<PontoGps> = emptyList(),

    val totalTimeTodayMs: Long = 0L,
    val drivingTimeTodayMs: Long = 0L,
    val distanceTodayKm: Double = 0.0,
    val averageSpeedTodayKmH: Double = 0.0,
    val sessionsTodayCount: Int = 0,

    val drivingTimeLast7Days: List<DailyDrivingTime> =
        emptyList(),

    val errorMessage: String? = null
)

data class DailyDrivingTime(
    val dayLabel: String,
    val dateStartMs: Long,
    val drivingTimeMs: Long,
    val isToday: Boolean = false
)