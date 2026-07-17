package com.ranielschneider.tvdetracker.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Calendar

fun saudacao(nome: String): String {
    val hora = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
    val periodo = when {
        hora < 12 -> "Bom dia"
        hora < 18 -> "Boa tarde"
        else -> "Boa noite"
    }
    return if (nome.isNotEmpty()) "$periodo, $nome! 👋" else "Bem-vindo ao TVDE Tracker! 👋"
}

@Composable
fun TypingText(
    texto: String,
    modifier: Modifier = Modifier,
    fontSize: TextUnit = 20.sp,
    fontWeight: FontWeight = FontWeight.SemiBold,
    color: Color = Color(0xFF1A2B4A),
    velocidade: Long = 60L
) {
    var textoVisivel by remember(texto) { mutableStateOf("") }
    var indice by remember(texto) { mutableIntStateOf(0) }

    // Cursor piscando
    val infiniteTransition = rememberInfiniteTransition(label = "cursor")
    val cursorAlpha by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 0f,
        animationSpec = infiniteRepeatable(
            animation = tween(500, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "cursorBlink"
    )

    LaunchedEffect(texto) {
        textoVisivel = ""
        indice = 0
        for (i in texto.indices) {
            delay(velocidade)
            textoVisivel = texto.substring(0, i + 1)
            indice = i + 1
        }
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = textoVisivel,
            fontSize = fontSize,
            fontWeight = fontWeight,
            color = color
        )
        // Cursor só aparece enquanto está a escrever
        if (indice < texto.length) {
            Text(
                text = "|",
                fontSize = fontSize,
                fontWeight = fontWeight,
                color = color.copy(alpha = cursorAlpha)
            )
        }
    }
}