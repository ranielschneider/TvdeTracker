package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Route
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.ui.calcularDuracao
import com.ranielschneider.tvdetracker.ui.formatarData
import com.ranielschneider.tvdetracker.ui.formatarHora
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking

@Composable
fun LastJourneyCard(
    sessao: Sessao?,
    onVerMapa: (Long) -> Unit,
    onVerHistorico: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Header(
                onVerHistorico = onVerHistorico
            )

            Spacer(modifier = Modifier.height(14.dp))

            if (sessao == null) {
                EmptyState()
            } else {
                JourneyContent(
                    sessao = sessao,
                    onVerMapa = onVerMapa
                )
            }
        }
    }
}

@Composable
private fun Header(
    onVerHistorico: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Default.Route,
            contentDescription = null,
            tint = VerdeTracking
        )

        Spacer(modifier = Modifier.size(8.dp))

        Text(
            text = "Última jornada",
            modifier = Modifier.weight(1f),
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp
        )

        Button(
            onClick = onVerHistorico,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeTracking.copy(alpha = 0.10f),
                contentColor = VerdeTracking
            ),
            contentPadding = PaddingValues(
                horizontal = 12.dp,
                vertical = 0.dp
            )
        ) {
            Text(
                text = "Ver todas",
                fontSize = 12.sp
            )
        }
    }
}

@Composable
private fun EmptyState() {
    Text(
        text = "Ainda não existem jornadas registadas.",
        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
    )
}

@Composable
private fun JourneyContent(
    sessao: Sessao,
    onVerMapa: (Long) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = formatarData(sessao.horaInicio),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
            )

            Text(
                text = "${formatarHora(sessao.horaInicio)} → ${
                    sessao.horaFim?.let { formatarHora(it) } ?: "--:--"
                }",
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold
            )
        }

        StatusBadge(
            emCurso = sessao.horaFim == null
        )
    }

    Spacer(modifier = Modifier.height(16.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        JourneyInfo(
            icon = Icons.Default.Route,
            value = "%.1f km".format(
                sessao.distanciaTotalMetros / 1000.0
            ),
            label = "Distância"
        )

        JourneyInfo(
            icon = Icons.Default.AccessTime,
            value = calcularDuracao(
                sessao.horaInicio,
                sessao.horaFim
            ),
            label = "Tempo total"
        )

        JourneyInfo(
            icon = Icons.Default.DirectionsCar,
            value = formatDuration(
                sessao.horasConduzidasMs
            ),
            label = "Conduzido"
        )
    }

    Spacer(modifier = Modifier.height(12.dp))

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.End
    ) {
        Button(
            onClick = {
                onVerMapa(sessao.id)
            },
            enabled = sessao.horaFim != null,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = VerdeTracking
            )
        ) {
            Icon(
                imageVector = Icons.Default.Map,
                contentDescription = null
            )

            Spacer(modifier = Modifier.size(6.dp))

            Text("Ver no mapa")
        }
    }
}

@Composable
private fun StatusBadge(
    emCurso: Boolean
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(40.dp))
            .background(VerdeTracking.copy(alpha = 0.12f))
            .padding(
                horizontal = 14.dp,
                vertical = 8.dp
            )
    ) {
        Text(
            text = if (emCurso) {
                "EM CURSO"
            } else {
                "CONCLUÍDA"
            },
            color = VerdeTracking,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp
        )
    }
}

@Composable
private fun JourneyInfo(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = VerdeTracking,
            modifier = Modifier.size(22.dp)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = value,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )

        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
        )
    }
}

private fun formatDuration(ms: Long): String {
    if (ms <= 0L) return "0h 00m"

    val hours =
        java.util.concurrent.TimeUnit.MILLISECONDS.toHours(ms)

    val minutes =
        java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(ms) % 60

    return "%dh %02dm".format(
        hours,
        minutes
    )
}