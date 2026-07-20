package com.ranielschneider.tvdetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
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
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranielschneider.tvdetracker.data.calcularDistanciaTotal
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.ui.theme.AzulPrimario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@Composable
fun MapaScreen(
    sessaoId: Long,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current

    var pontos by remember {
        mutableStateOf<List<PontoGps>>(emptyList())
    }

    var sessao by remember {
        mutableStateOf<Sessao?>(null)
    }

    var kmTotal by remember {
        mutableDoubleStateOf(0.0)
    }

    val cameraPositionState = rememberCameraPositionState()

    val mapUiSettings = remember {
        MapUiSettings(
            compassEnabled = true,
            indoorLevelPickerEnabled = true,
            mapToolbarEnabled = false,
            myLocationButtonEnabled = false,
            rotationGesturesEnabled = true,
            scrollGesturesEnabled = true,
            scrollGesturesEnabledDuringRotateOrZoom = true,
            tiltGesturesEnabled = true,
            zoomControlsEnabled = true,
            zoomGesturesEnabled = true
        )
    }

    LaunchedEffect(sessaoId) {
        val dao = TrackerDatabase
            .getDatabase(context)
            .trackerDao()

        sessao = dao.buscarSessaoPorId(sessaoId)
        pontos = dao.buscarPontosDaSessao(sessaoId)
        kmTotal = calcularDistanciaTotal(pontos)
    }

    LaunchedEffect(pontos) {
        if (pontos.isEmpty()) {
            return@LaunchedEffect
        }

        if (pontos.size == 1) {
            val ponto = pontos.first()

            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        ponto.latitude,
                        ponto.longitude
                    ),
                    16f
                )
            )

            return@LaunchedEffect
        }

        val boundsBuilder = LatLngBounds.Builder()

        pontos.forEach { ponto ->
            boundsBuilder.include(
                LatLng(
                    ponto.latitude,
                    ponto.longitude
                )
            )
        }

        cameraPositionState.animate(
            CameraUpdateFactory.newLatLngBounds(
                boundsBuilder.build(),
                120
            )
        )
    }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            uiSettings = mapUiSettings
        ) {
            if (pontos.size >= 2) {
                Polyline(
                    points = pontos.map { ponto ->
                        LatLng(
                            ponto.latitude,
                            ponto.longitude
                        )
                    },
                    width = 9f,
                    color = Color(0xFF1565C0)
                )
            }
        }

        IconButton(
            onClick = onVoltar,
            modifier = Modifier
                .align(Alignment.TopStart)
                .windowInsetsPadding(WindowInsets.statusBars)
                .padding(
                    start = 16.dp,
                    top = 12.dp
                )
                .background(
                    color = AzulPrimario,
                    shape = CircleShape
                )
        ) {
            Icon(
                imageVector =
                    Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Voltar",
                tint = Color.White
            )
        }

        sessao?.let { sessaoAtual ->
            JourneyMapInfoCard(
                sessao = sessaoAtual,
                distanciaKm = kmTotal,
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .padding(
                        start = 76.dp,
                        top = 12.dp,
                        end = 16.dp
                    )
            )
        }
    }
}

@Composable
private fun JourneyMapInfoCard(
    sessao: Sessao,
    distanciaKm: Double,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface.copy(
                    alpha = 0.96f
                )
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 8.dp
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 14.dp
            ),
            verticalArrangement =
                Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = formatarDataMapa(sessao.horaInicio),
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
                color =
                    MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.60f
                    )
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement =
                    Arrangement.SpaceBetween
            ) {
                MapMetricItem(
                    label = "Início",
                    value = formatarHora(sessao.horaInicio)
                )

                MapMetricItem(
                    label = "Fim",
                    value = sessao.horaFim?.let {
                        formatarHora(it)
                    } ?: "--:--"
                )

                MapMetricItem(
                    label = "Duração",
                    value = calcularDuracao(
                        sessao.horaInicio,
                        sessao.horaFim
                    )
                )

                MapMetricItem(
                    label = "Distância",
                    value = "%.2f km".format(distanciaKm)
                )
            }
        }
    }
}

@Composable
private fun MapMetricItem(
    label: String,
    value: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.55f
            )
        )

        Text(
            text = value,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun formatarDataMapa(
    timestamp: Long
): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}

fun formatarHora(
    timestamp: Long
): String {
    val formatter = SimpleDateFormat(
        "HH:mm",
        Locale.getDefault()
    )

    return formatter.format(Date(timestamp))
}

fun calcularDuracao(
    inicio: Long,
    fim: Long?
): String {
    if (fim == null) {
        return "--:--"
    }

    val diferenca = (fim - inicio).coerceAtLeast(0L)

    val horas = TimeUnit.MILLISECONDS
        .toHours(diferenca)

    val minutos = TimeUnit.MILLISECONDS
        .toMinutes(diferenca) % 60

    return "%dh %02dm".format(
        horas,
        minutos
    )
}