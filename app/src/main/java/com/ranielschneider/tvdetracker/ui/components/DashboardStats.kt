package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.animateFloat
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusa
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking
import java.util.concurrent.TimeUnit


@Composable
fun DashboardStatsRow(
    tempoTotalHojeMs: Long,
    distanciaHojeKm: Double,
    tempoConduzidoHojeMs: Long,
    quantidadeSessoes: Int,
    carregando: Boolean,
    modifier: Modifier = Modifier
) {

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AccessTime,
            label = "Tempo",
            value = if (carregando) {
                "..."
            } else {
                formatDuration(tempoTotalHojeMs)
            }
        )

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Route,
            label = "Distância",
            value = if (carregando) {
                "..."
            } else {
                "%.1f km".format(distanciaHojeKm)
            }
        )

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
            label = "Sessões",
            value = if (carregando) {
                "..."
            } else {
                quantidadeSessoes.toString()
            }
        )

        DrivingLimitMiniCard(
            modifier = Modifier.weight(1f),
            drivingTimeMs = tempoConduzidoHojeMs,
            carregando = carregando
        )
    }
}


@Composable
private fun DashboardStatCard(
    modifier: Modifier,
    icon: ImageVector,
    label: String,
    value: String
) {

    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    horizontal = 5.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(
                        VerdeTracking.copy(alpha = 0.12f)
                    ),
                contentAlignment = Alignment.Center
            ) {

                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VerdeTracking,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(
                modifier = Modifier.height(9.dp)
            )

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme
                    .onSurface
                    .copy(alpha = 0.62f)
            )

            Spacer(
                modifier = Modifier.height(5.dp)
            )

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
@Composable
private fun DrivingLimitMiniCard(
    modifier: Modifier,
    drivingTimeMs: Long,
    carregando: Boolean
) {

    val limiteMs =
        TimeUnit.HOURS.toMillis(10)


    val progressoReal =
        (drivingTimeMs.toFloat() / limiteMs)
            .coerceIn(0f, 1f)


    // Mantém uma pequena presença visual quando está zerado
    val progressoVisual =
        if (progressoReal <= 0f) {
            0.08f
        } else {
            progressoReal
        }


    val progressoAnimado by animateFloatAsState(
        targetValue = progressoVisual,
        animationSpec = tween(
            durationMillis = 1200
        ),
        label = "drivingFill"
    )


    val cor = when {

        progressoReal < 0.5f ->
            VerdeTracking

        progressoReal < 0.8f ->
            AmareloParusa

        else ->
            Color.Red
    }


    val restante =
        (limiteMs - drivingTimeMs)
            .coerceAtLeast(0L)


    val ondaOffset by rememberInfiniteTransition(
        label = "waterWave"
    ).animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 1800
            )
        ),
        label = "waveMovement"
    )


    Card(
        modifier = modifier.height(132.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor =
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {

        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(
                    RoundedCornerShape(20.dp)
                )
        ) {


            // Água preenchendo o card
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight(
                        progressoAnimado
                    )
                    .align(
                        Alignment.BottomCenter
                    )
            ) {


                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            cor.copy(
                                alpha = 0.85f
                            )
                        )
                )


                // onda no topo da água
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(12.dp)
                ) {

                    val path = Path()

                    val largura =
                        size.width

                    val altura =
                        size.height


                    path.moveTo(
                        0f,
                        altura / 2
                    )


                    for (x in 0..largura.toInt() step 10) {

                        val y =
                            altura / 2 +
                                    kotlin.math.sin(
                                        (
                                                x / 25f
                                                        +
                                                        ondaOffset * 6
                                                )
                                            .toDouble()
                                    )
                                        .toFloat() *
                                    3f


                        path.lineTo(
                            x.toFloat(),
                            y
                        )
                    }


                    path.lineTo(
                        largura,
                        altura
                    )

                    path.lineTo(
                        0f,
                        altura
                    )

                    path.close()


                    drawPath(
                        path = path,
                        color = cor.copy(
                            alpha = 0.85f
                        )
                    )
                }
            }



            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment =
                    Alignment.CenterHorizontally,
                verticalArrangement =
                    Arrangement.Center
            ) {


                val textoCor =
                    if (progressoReal > 0.55f) {
                        Color.White
                    } else {
                        MaterialTheme
                            .colorScheme
                            .onSurface
                    }



                Icon(
                    imageVector =
                        Icons.Default.DirectionsCar,
                    contentDescription = null,
                    tint = textoCor,
                    modifier = Modifier.size(18.dp)
                )


                Spacer(
                    modifier = Modifier.height(2.dp)
                )


                Text(
                    text = "Condução",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = textoCor
                )


                Text(
                    text =
                        if (carregando) {
                            "..."
                        } else {
                            formatHours(
                                drivingTimeMs
                            )
                        },
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    color = textoCor
                )


                Text(
                    text =
                        if (carregando) {
                            "..."
                        } else {
                            "restam ${
                                formatHours(restante)
                            }"
                        },
                    fontSize = 8.sp,
                    color = textoCor.copy(
                        alpha = 0.8f
                    )
                )
            }
        }
    }
}



private fun formatDuration(
    ms: Long
): String {

    if (ms <= 0L) return "0h 00m"


    val horas =
        TimeUnit.MILLISECONDS
            .toHours(ms)


    val minutos =
        TimeUnit.MILLISECONDS
            .toMinutes(ms) % 60


    return "%dh %02dm".format(
        horas,
        minutos
    )
}



private fun formatHours(
    ms: Long
): String {

    val horas =
        TimeUnit.MILLISECONDS
            .toHours(ms)


    val minutos =
        TimeUnit.MILLISECONDS
            .toMinutes(ms) % 60


    return "${horas}h${minutos}m"
}