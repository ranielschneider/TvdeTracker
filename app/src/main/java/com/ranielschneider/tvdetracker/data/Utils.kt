package com.ranielschneider.tvdetracker.data

import com.ranielschneider.tvdetracker.data.local.PontoGps

fun calcularDistanciaTotal(pontos: List<PontoGps>): Double {
    if (pontos.size < 2) return 0.0
    var distanciaTotal = 0.0
    for (i in 0 until pontos.size - 1) {
        val resultado = FloatArray(1)
        android.location.Location.distanceBetween(
            pontos[i].latitude, pontos[i].longitude,
            pontos[i + 1].latitude, pontos[i + 1].longitude,
            resultado
        )
        distanciaTotal += resultado[0]
    }
    return distanciaTotal / 1000.0
}