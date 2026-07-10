package com.ranielschneider.tvdetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranielschneider.tvdetracker.AzulPrimario
import com.ranielschneider.tvdetracker.data.calcularDistanciaTotal
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import java.util.concurrent.TimeUnit

@Composable
fun MapaScreen(sessaoId: Long, onVoltar: () -> Unit) {
    val context = LocalContext.current
    var pontos by remember { mutableStateOf<List<PontoGps>>(emptyList()) }
    var sessao by remember { mutableStateOf<Sessao?>(null) }
    var kmTotal by remember { mutableStateOf(0.0) }

    LaunchedEffect(sessaoId) {
        val db = TrackerDatabase.getDatabase(context)
        sessao = db.trackerDao().buscarSessaoPorId(sessaoId)
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
                    width = 8f,
                    color = Color(0xFF1976D2)
                )
            }
        }

        // Botão voltar
        IconButton(
            onClick = onVoltar,
            modifier = Modifier
                .padding(16.dp)
                .background(color = AzulPrimario, shape = RoundedCornerShape(50))
                .align(Alignment.TopStart)
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        // Card de informações em baixo
        sessao?.let { s ->
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .padding(16.dp)
                    .navigationBarsPadding()
                    .background(
                        color = Color.White.copy(alpha = 0.97f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = formatarData(s.horaInicio),
                    fontSize = 13.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    InfoItem(label = "Início", valor = formatarHora(s.horaInicio))
                    InfoItem(label = "Fim", valor = s.horaFim?.let { formatarHora(it) } ?: "--:--")
                    InfoItem(label = "Duração", valor = calcularDuracao(s.horaInicio, s.horaFim))
                    InfoItem(label = "Distância", valor = "%.2f km".format(kmTotal))
                }
            }
        }
    }
}

@Composable
fun InfoItem(label: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(text = label, fontSize = 11.sp, color = Color.Gray)
        Text(text = valor, fontSize = 15.sp, fontWeight = FontWeight.Bold)
    }
}

fun formatarHora(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("HH:mm", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}

fun calcularDuracao(inicio: Long, fim: Long?): String {
    if (fim == null) return "--:--"
    val diff = fim - inicio
    val horas = TimeUnit.MILLISECONDS.toHours(diff)
    val minutos = TimeUnit.MILLISECONDS.toMinutes(diff) % 60
    return "%dh %02dm".format(horas, minutos)
}