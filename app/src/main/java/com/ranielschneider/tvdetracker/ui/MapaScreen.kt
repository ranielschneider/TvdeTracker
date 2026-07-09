package com.ranielschneider.tvdetracker.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase

@Composable
fun MapaScreen(sessaoId: Long) {
    val context = LocalContext.current
    var pontos by remember { mutableStateOf<List<PontoGps>>(emptyList()) }
    var kmTotal by remember { mutableStateOf(0.0) }

    LaunchedEffect(sessaoId) {
        val db = TrackerDatabase.getDatabase(context)
        pontos = db.trackerDao().buscarPontosDaSessao(sessaoId)
        kmTotal = calcularDistanciaTotal(pontos)
    }

    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(pontos) {
        if (pontos.isNotEmpty()) {
            val primeiro = pontos.first()
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(primeiro.latitude, primeiro.longitude), 15f
                    )
                )
            )
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState
        ) {
            if (pontos.isNotEmpty()) {
                Polyline(
                    points = pontos.map { LatLng(it.latitude, it.longitude) },
                    width = 8f
                )
            }
        }

        Text(
            text = "%.2f km".format(kmTotal),
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 32.dp)
        )
    }
}

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