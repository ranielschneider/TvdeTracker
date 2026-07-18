package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking

@Composable
fun LiveMapCard(
    pontos: List<PontoGps>,
    onOpenMap: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {

        Column {

            Surface(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenMap() },
                color = MaterialTheme.colorScheme.surface
            ) {

                androidx.compose.foundation.layout.Row(
                    modifier = Modifier.padding(
                        horizontal = 18.dp,
                        vertical = 16.dp
                    ),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Icon(
                        imageVector = Icons.Default.Map,
                        contentDescription = null,
                        tint = VerdeTracking
                    )

                    Text(
                        text = "Mapa em tempo real",
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = 12.dp),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )

                    Text(
                        text = "Abrir",
                        color = VerdeTracking
                    )

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = VerdeTracking
                    )
                }
            }

            HorizontalDivider()

            MiniMap(
                pontos = pontos,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
                onClick = onOpenMap
            )
        }
    }
}