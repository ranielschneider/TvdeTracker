package com.ranielschneider.tvdetracker.ui

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import kotlin.math.cos
import kotlin.math.sin
import kotlin.random.Random

data class Particle(
    val x: Float,
    val y: Float,
    val radius: Float,
    val speed: Float,
    val angle: Float,
    val alpha: Float,
    val orbitRadius: Float,
    val orbitSpeed: Float
)

@Composable
fun ParticleBackground(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1565C0)
) {
    val particles = remember {
        List(40) {
            Particle(
                x = Random.nextFloat(),
                y = Random.nextFloat(),
                radius = Random.nextFloat() * 3f + 1.5f,
                speed = Random.nextFloat() * 0.3f + 0.1f,
                angle = Random.nextFloat() * 360f,
                alpha = Random.nextFloat() * 0.4f + 0.1f,
                orbitRadius = Random.nextFloat() * 0.08f + 0.02f,
                orbitSpeed = Random.nextFloat() * 0.5f + 0.2f
            )
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "particles")

    val time by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "time"
    )

    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height

        particles.forEach { p ->
            val angle = Math.toRadians((time * p.orbitSpeed + p.angle).toDouble())
            val cx = (p.x * w) + (p.orbitRadius * w * cos(angle).toFloat())
            val cy = (p.y * h) + (p.orbitRadius * h * sin(angle).toFloat())

            // Partícula com brilho suave
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        color.copy(alpha = p.alpha),
                        color.copy(alpha = p.alpha * 0.3f),
                        Color.Transparent
                    ),
                    center = Offset(cx, cy),
                    radius = p.radius * 4f
                ),
                radius = p.radius * 4f,
                center = Offset(cx, cy)
            )

            // Núcleo da partícula
            drawCircle(
                color = color.copy(alpha = p.alpha * 1.5f),
                radius = p.radius,
                center = Offset(cx, cy)
            )
        }
    }
}

@Composable
fun PulseAnimation(
    modifier: Modifier = Modifier,
    color: Color = Color(0xFF1565C0),
    ativo: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")

    val wave1 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave1"
    )

    val wave2 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave2"
    )

    val wave3 by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, delayMillis = 1200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wave3"
    )

    Canvas(modifier = modifier) {
        val centerX = size.width / 2
        val centerY = size.height / 2
        val maxRadius = size.minDimension / 2

        // Pulso quando ativo
        if (ativo) {
            listOf(wave1, wave2, wave3).forEach { progress ->
                val radius = maxRadius * progress
                val alpha = (1f - progress) * 0.3f
                drawCircle(
                    color = color.copy(alpha = alpha),
                    radius = radius,
                    center = Offset(centerX, centerY)
                )
            }
        }

        // Efeito neon por baixo do botão
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    color.copy(alpha = 0.4f),
                    color.copy(alpha = 0.15f),
                    Color.Transparent
                ),
                center = Offset(centerX, centerY + maxRadius * 0.55f),
                radius = maxRadius * 0.65f
            ),
            radius = maxRadius * 0.65f,
            center = Offset(centerX, centerY + maxRadius * 0.55f)
        )
    }
}