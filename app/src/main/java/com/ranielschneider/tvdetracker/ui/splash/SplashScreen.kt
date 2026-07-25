package com.ranielschneider.tvdetracker.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(
    onAnimationFinished: () -> Unit
) {
    var startLogoAnimation by remember {
        mutableStateOf(false)
    }

    var startRoadAnimation by remember {
        mutableStateOf(false)
    }

    val configuration = LocalConfiguration.current
    val density = LocalDensity.current

    val screenWidthPx = with(density) {
        configuration.screenWidthDp.dp.toPx()
    }

    val screenHeightPx = with(density) {
        configuration.screenHeightDp.dp.toPx()
    }

    LaunchedEffect(Unit) {
        delay(150)

        startLogoAnimation = true

        delay(350)

        startRoadAnimation = true

        delay(1_150)

        onAnimationFinished()
    }

    val logoAlpha by animateFloatAsState(
        targetValue =
            if (startLogoAnimation) {
                1f
            } else {
                0f
            },
        animationSpec = tween(
            durationMillis = 450,
            easing = FastOutSlowInEasing
        ),
        label = "logoAlpha"
    )

    val logoScale by animateFloatAsState(
        targetValue =
            if (startLogoAnimation) {
                1f
            } else {
                0.92f
            },
        animationSpec = tween(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        ),
        label = "logoScale"
    )

    val via1OffsetX by animateFloatAsState(
        targetValue =
            if (startRoadAnimation) {
                -(screenWidthPx * 1.8f)
            } else {
                0f
            },
        animationSpec = tween(
            durationMillis = 700,
            easing = FastOutSlowInEasing
        ),
        label = "via1OffsetX"
    )

    val via2OffsetY by animateFloatAsState(
        targetValue =
            if (startRoadAnimation) {
                screenHeightPx * 1.8f
            } else {
                0f
            },
        animationSpec = tween(
            durationMillis = 760,
            delayMillis = 130,
            easing = FastOutSlowInEasing
        ),
        label = "via2OffsetY"
    )

    val via3OffsetX by animateFloatAsState(
        targetValue =
            if (startRoadAnimation) {
                screenWidthPx * 1.8f
            } else {
                0f
            },
        animationSpec = tween(
            durationMillis = 820,
            delayMillis = 260,
            easing = FastOutSlowInEasing
        ),
        label = "via3OffsetX"
    )

    val via4OffsetY by animateFloatAsState(
        targetValue =
            if (startRoadAnimation) {
                -(screenHeightPx * 1.8f)
            } else {
                0f
            },
        animationSpec = tween(
            durationMillis = 780,
            delayMillis = 390,
            easing = FastOutSlowInEasing
        ),
        label = "via4OffsetY"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Color(0xFF16A34A)
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = via1OffsetX
                }
        ) {
            val path = Path().apply {
                moveTo(
                    -size.width * 0.10f,
                    size.height * 0.18f
                )

                cubicTo(
                    size.width * 0.30f,
                    size.height * 0.16f,
                    size.width * 0.42f,
                    size.height * 0.40f,
                    size.width * 0.18f,
                    size.height * 0.62f
                )

                cubicTo(
                    size.width * 0.02f,
                    size.height * 0.77f,
                    size.width * 0.16f,
                    size.height * 0.90f,
                    size.width * 0.38f,
                    size.height * 0.92f
                )
            }

            drawPath(
                path = path,
                color = Color.White.copy(
                    alpha = 0.16f
                ),
                style = Stroke(
                    width = 15f,
                    cap = StrokeCap.Round
                )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = via2OffsetY
                }
        ) {
            val path = Path().apply {
                moveTo(
                    size.width * 0.20f,
                    -size.height * 0.10f
                )

                cubicTo(
                    size.width * 0.30f,
                    size.height * 0.20f,
                    size.width * 0.72f,
                    size.height * 0.44f,
                    size.width * 0.82f,
                    size.height * 1.10f
                )
            }

            drawPath(
                path = path,
                color = Color.White.copy(
                    alpha = 0.12f
                ),
                style = Stroke(
                    width = 11f,
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = path,
                color = Color.White.copy(
                    alpha = 0.16f
                ),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    pathEffect =
                        androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(
                                18f,
                                22f
                            )
                        )
                )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationX = via3OffsetX
                }
        ) {
            val path = Path().apply {
                moveTo(
                    size.width * 1.10f,
                    size.height * 0.25f
                )

                cubicTo(
                    size.width * 0.68f,
                    size.height * 0.33f,
                    size.width * 0.58f,
                    size.height * 0.60f,
                    size.width * 0.90f,
                    size.height * 0.72f
                )

                cubicTo(
                    size.width * 1.04f,
                    size.height * 0.78f,
                    size.width * 0.94f,
                    size.height * 0.90f,
                    size.width * 0.72f,
                    size.height * 1.05f
                )
            }

            drawPath(
                path = path,
                color = Color.White.copy(
                    alpha = 0.18f
                ),
                style = Stroke(
                    width = 17f,
                    cap = StrokeCap.Round
                )
            )
        }

        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = via4OffsetY
                }
        ) {
            val path = Path().apply {
                moveTo(
                    size.width * 0.08f,
                    size.height * 1.08f
                )

                cubicTo(
                    size.width * 0.28f,
                    size.height * 0.78f,
                    size.width * 0.64f,
                    size.height * 0.74f,
                    size.width * 0.92f,
                    size.height * 0.98f
                )
            }

            drawPath(
                path = path,
                color = Color.White.copy(
                    alpha = 0.14f
                ),
                style = Stroke(
                    width = 13f,
                    cap = StrokeCap.Round
                )
            )

            drawPath(
                path = path,
                color = Color.White.copy(
                    alpha = 0.16f
                ),
                style = Stroke(
                    width = 3f,
                    cap = StrokeCap.Round,
                    pathEffect =
                        androidx.compose.ui.graphics.PathEffect.dashPathEffect(
                            floatArrayOf(
                                20f,
                                24f
                            )
                        )
                )
            )
        }

        Column(
            modifier = Modifier.graphicsLayer {
                alpha = logoAlpha
                scaleX = logoScale
                scaleY = logoScale
            },
            horizontalAlignment =
                Alignment.CenterHorizontally
        ) {
            Text(
                text = "TVDE",
                color = Color.White,
                fontSize = 56.sp,
                fontWeight = FontWeight.Black,
                letterSpacing = (-1).sp
            )

            Text(
                text = "TRACKER",
                color = Color.White.copy(
                    alpha = 0.92f
                ),
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 6.sp
            )
        }
    }
}