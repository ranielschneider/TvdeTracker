package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking

@Composable
fun DashboardStatsRow(
    tempoTotalHojeMs: Long,
    distanciaHojeKm: Double,
    velocidadeMediaHoje: Double,
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
            value = if (carregando) "..." else formatDuration(tempoTotalHojeMs)
        )

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Route,
            label = "Distância",
            value = if (carregando) "..." else "%.1f km".format(distanciaHojeKm)
        )

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.DateRange,
            label = "Sessões",
            value = if (carregando) "..." else quantidadeSessoes.toString()
        )

        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.Speed,
            label = "Média",
            value = if (carregando) "..." else "%.0f km/h".format(velocidadeMediaHoje)
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
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 5.dp, vertical = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .clip(CircleShape)
                    .background(VerdeTracking.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = VerdeTracking,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.height(9.dp))

            Text(
                text = label,
                fontSize = 11.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.62f),
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(5.dp))

            Text(
                text = value,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 1
            )
        }
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0h 00m"

    val hours = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(ms)
    val minutes = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(ms) % 60

    return "%dh %02dm".format(hours, minutes)
}