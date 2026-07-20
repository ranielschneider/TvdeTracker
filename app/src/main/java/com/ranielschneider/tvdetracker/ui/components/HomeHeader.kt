package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.ui.model.TrackingState
import com.ranielschneider.tvdetracker.ui.saudacao
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusa
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking

@Composable
fun HomeHeader(
    nome: String,
    estado: TrackingState,
    onAbrirMenu: () -> Unit,
    modifier: Modifier = Modifier
) {
    val corEstado = when (estado) {
        TrackingState.STOPPED,
        TrackingState.TRACKING -> VerdeTracking

        TrackingState.PAUSED -> AmareloParusa
    }

    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AvatarUsuario(nome = nome)

        Column(
            modifier = Modifier
                .weight(1f)
                .padding(
                    start = 12.dp,
                    end = 6.dp
                )
        ) {
            Text(
                text = saudacao(nome),
                fontSize = 19.sp,
                lineHeight = 23.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(
                modifier = Modifier.size(4.dp)
            )

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                StatusLed(color = corEstado)

                Spacer(
                    modifier = Modifier.size(7.dp)
                )

                Text(
                    text = textoEstado(estado),
                    color = corEstado,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }

        IconButton(
            onClick = onAbrirMenu,
            modifier = Modifier.size(48.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Abrir menu",
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

@Composable
private fun AvatarUsuario(
    nome: String,
    modifier: Modifier = Modifier
) {
    val inicial = nome
        .trim()
        .firstOrNull()
        ?.uppercase()
        ?: "T"

    Box(
        modifier = modifier
            .size(54.dp)
            .clip(CircleShape)
            .background(VerdeTracking),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = inicial,
            color = Color.White,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun StatusLed(
    color: Color,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(
        label = "homeHeaderStatus"
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.35f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = 900,
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Reverse
        ),
        label = "homeHeaderStatusAlpha"
    )

    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(
                color.copy(alpha = alpha)
            )
    )
}

private fun textoEstado(
    estado: TrackingState
): String {
    return when (estado) {
        TrackingState.STOPPED -> "Pronto para iniciar"
        TrackingState.TRACKING -> "A registar percurso"
        TrackingState.PAUSED -> "Jornada em pausa"
    }
}