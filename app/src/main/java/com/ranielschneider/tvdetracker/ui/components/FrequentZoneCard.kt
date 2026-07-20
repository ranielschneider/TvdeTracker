package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BarChart
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.ui.model.DailyDrivingTime
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusa
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking
import com.ranielschneider.tvdetracker.ui.theme.VermelhoStop
import java.util.concurrent.TimeUnit

private const val DAILY_DRIVING_LIMIT_HOURS = 10f

@Composable
fun FrequentZoneCard(
    modifier: Modifier = Modifier,
    dailyDrivingTimes: List<DailyDrivingTime> = emptyList(),
    isLoading: Boolean = false
) {
    val totalWeekMs = dailyDrivingTimes.sumOf { day ->
        day.drivingTimeMs
    }

    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 2.dp
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 18.dp,
                    vertical = 18.dp
                )
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = "Condução nos últimos 7 dias",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    Spacer(
                        modifier = Modifier.height(3.dp)
                    )

                    Text(
                        text = "Tempo efetivamente conduzido",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.58f
                        )
                    )
                }

                Box(
                    modifier = Modifier
                        .size(42.dp)
                        .clip(RoundedCornerShape(13.dp))
                        .background(
                            VerdeTracking.copy(alpha = 0.10f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.BarChart,
                        contentDescription = null,
                        tint = VerdeTracking,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }

            Spacer(
                modifier = Modifier.height(22.dp)
            )

            if (isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(150.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "A carregar dados...",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.55f
                        )
                    )
                }
            } else if (dailyDrivingTimes.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Ainda não existem dados de condução.",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.55f
                        )
                    )
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(156.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Bottom
                ) {
                    dailyDrivingTimes.forEach { day ->
                        DrivingDayBar(
                            day = day,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }

                Spacer(
                    modifier = Modifier.height(16.dp)
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "Total da semana",
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface.copy(
                                alpha = 0.55f
                            )
                        )

                        Text(
                            text = formatDrivingDuration(totalWeekMs),
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Text(
                        text = "Limite diário: 10h",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium,
                        color = MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.62f
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun DrivingDayBar(
    day: DailyDrivingTime,
    modifier: Modifier = Modifier
) {
    val hours = day.drivingTimeMs / 3_600_000f

    val targetProgress = (
            hours / DAILY_DRIVING_LIMIT_HOURS
            ).coerceIn(
            minimumValue = 0f,
            maximumValue = 1f
        )

    val animatedProgress by animateFloatAsState(
        targetValue = targetProgress,
        animationSpec = tween(
            durationMillis = 650
        ),
        label = "weeklyDrivingBar"
    )

    val barColor = when {
        hours >= 9f -> VermelhoStop
        hours >= 7f -> AmareloParusa
        else -> VerdeTracking
    }

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = formatCompactHours(
                day.drivingTimeMs
            ),
            fontSize = 10.sp,
            fontWeight = FontWeight.SemiBold,
            color = if (day.isToday) {
                VerdeTracking
            } else {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.65f
                )
            }
        )

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Box(
            modifier = Modifier
                .height(102.dp)
                .width(22.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(
                    MaterialTheme.colorScheme.onSurface.copy(
                        alpha = 0.07f
                    )
                ),
            contentAlignment = Alignment.BottomCenter
        ) {
            if (animatedProgress > 0f) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight(animatedProgress)
                        .width(22.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(barColor)
                )
            }
        }

        Spacer(
            modifier = Modifier.height(7.dp)
        )

        Text(
            text = day.dayLabel,
            fontSize = 11.sp,
            fontWeight = if (day.isToday) {
                FontWeight.Bold
            } else {
                FontWeight.Medium
            },
            color = if (day.isToday) {
                VerdeTracking
            } else {
                MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.58f
                )
            }
        )
    }
}

private fun formatCompactHours(
    timeMs: Long
): String {
    if (timeMs <= 0L) {
        return "0h"
    }

    val hours = TimeUnit.MILLISECONDS
        .toHours(timeMs)

    val minutes = TimeUnit.MILLISECONDS
        .toMinutes(timeMs) % 60

    return when {
        hours <= 0L -> "${minutes}m"
        minutes == 0L -> "${hours}h"
        else -> "${hours}h${minutes}"
    }
}

private fun formatDrivingDuration(
    timeMs: Long
): String {
    if (timeMs <= 0L) {
        return "0h 00min"
    }

    val hours = TimeUnit.MILLISECONDS
        .toHours(timeMs)

    val minutes = TimeUnit.MILLISECONDS
        .toMinutes(timeMs) % 60

    return "%dh %02dmin".format(
        hours,
        minutes
    )
}