package com.ranielschneider.tvdetracker.ui.utils

import android.content.Context
import com.ranielschneider.tvdetracker.data.calcularDistanciaTotal
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Calendar

fun isSameDay(
    first: Long,
    second: Long
): Boolean {
    val firstCalendar = Calendar.getInstance().apply {
        timeInMillis = first
    }

    val secondCalendar = Calendar.getInstance().apply {
        timeInMillis = second
    }

    return firstCalendar.get(Calendar.YEAR) ==
            secondCalendar.get(Calendar.YEAR) &&
            firstCalendar.get(Calendar.DAY_OF_YEAR) ==
            secondCalendar.get(Calendar.DAY_OF_YEAR)
}

fun calculateTotalTimeMs(
    sessions: List<Sessao>,
    currentTimeMillis: Long = System.currentTimeMillis()
): Long {
    return sessions.sumOf { session ->
        val endTime = session.horaFim ?: currentTimeMillis
        (endTime - session.horaInicio).coerceAtLeast(0L)
    }
}

fun calculateAverageSpeedKmH(
    distanceKm: Double,
    drivingTimeMs: Long
): Double {
    if (drivingTimeMs <= 0L) return 0.0

    val drivingHours = drivingTimeMs / 3_600_000.0

    return if (drivingHours > 0.0) {
        distanceKm / drivingHours
    } else {
        0.0
    }
}

suspend fun finishActiveSession(
    context: Context
) {
    withContext(Dispatchers.IO) {
        val dao = TrackerDatabase
            .getDatabase(context)
            .trackerDao()

        val activeSession = dao
            .buscarTodasSessoes()
            .filter { it.horaFim == null }
            .maxByOrNull { it.horaInicio }
            ?: return@withContext

        val endTime = System.currentTimeMillis()
        val points = dao.buscarPontosDaSessao(activeSession.id)
        val pauses = dao.buscarPausasDaSessao(activeSession.id)
        val distanceKm = calcularDistanciaTotal(points)

        val totalPauseTimeMs = pauses.sumOf { pause ->
            (pause.fimPausa ?: endTime) - pause.inicioPausa
        }

        val drivingTimeMs =
            ((endTime - activeSession.horaInicio) - totalPauseTimeMs)
                .coerceAtLeast(0L)

        dao.fecharSessao(
            sessaoId = activeSession.id,
            horaFim = endTime,
            distancia = distanceKm * 1000.0,
            horasConduzidasMs = drivingTimeMs
        )
    }
}