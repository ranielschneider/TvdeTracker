package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.ui.EstadoTracking
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusa
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking
import com.ranielschneider.tvdetracker.ui.theme.VermelhoStop

@Composable
fun MainJourneyButton(
    estado: EstadoTracking,
    temPermissao: Boolean,
    onPedirPermissao: () -> Unit,
    onIniciar: () -> Unit,
    onPausar: () -> Unit,
    onRetomar: () -> Unit,
    onEncerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (estado) {
        EstadoTracking.PARADO -> {
            StartJourneyButton(
                temPermissao = temPermissao,
                onPedirPermissao = onPedirPermissao,
                onIniciar = onIniciar,
                modifier = modifier
            )
        }

        EstadoTracking.A_TRACKING -> {
            TrackingActions(
                onPausar = onPausar,
                onEncerrar = onEncerrar,
                modifier = modifier
            )
        }

        EstadoTracking.EM_PAUSA -> {
            PausedActions(
                onRetomar = onRetomar,
                onEncerrar = onEncerrar,
                modifier = modifier
            )
        }
    }
}

@Composable
private fun StartJourneyButton(
    temPermissao: Boolean,
    onPedirPermissao: () -> Unit,
    onIniciar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = if (temPermissao) onIniciar else onPedirPermissao,
        modifier = modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = RoundedCornerShape(38.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = VerdeTracking
        ),
        contentPadding = PaddingValues(horizontal = 24.dp)
    ) {
        Icon(
            imageVector = if (temPermissao) {
                Icons.Default.PlayArrow
            } else {
                Icons.Default.Place
            },
            contentDescription = null,
            modifier = Modifier.size(34.dp)
        )

        Spacer(modifier = Modifier.size(14.dp))

        Column(
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = if (temPermissao) {
                    "INICIAR JORNADA"
                } else {
                    "PERMITIR GPS"
                },
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = if (temPermissao) {
                    "Comece a registar a sua jornada"
                } else {
                    "Necessário para iniciar o rastreio"
                },
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.86f)
            )
        }
    }
}

@Composable
private fun TrackingActions(
    onPausar: () -> Unit,
    onEncerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        JourneyActionButton(
            text = "PAUSAR",
            icon = Icons.Default.Pause,
            containerColor = AmareloParusa,
            onClick = onPausar,
            modifier = Modifier.weight(1f)
        )

        JourneyActionButton(
            text = "ENCERRAR",
            icon = Icons.Default.Stop,
            containerColor = VermelhoStop,
            onClick = onEncerrar,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun PausedActions(
    onRetomar: () -> Unit,
    onEncerrar: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        JourneyActionButton(
            text = "RETOMAR",
            icon = Icons.Default.PlayArrow,
            containerColor = VerdeTracking,
            onClick = onRetomar,
            modifier = Modifier.weight(1f)
        )

        JourneyActionButton(
            text = "ENCERRAR",
            icon = Icons.Default.Stop,
            containerColor = VermelhoStop,
            onClick = onEncerrar,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun JourneyActionButton(
    text: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(62.dp),
        shape = RoundedCornerShape(22.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor
        )
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null
        )

        Spacer(modifier = Modifier.size(6.dp))

        Text(
            text = text,
            fontWeight = FontWeight.Bold
        )
    }
}