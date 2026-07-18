package com.ranielschneider.tvdetracker.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking

@Composable
fun MiniMap(
    pontos: List<PontoGps>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(pontos) {
        pontos.lastOrNull()?.let { ponto ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(
                            ponto.latitude,
                            ponto.longitude
                        ),
                        13f
                    )
                )
            )
        }
    }

    Box(
        modifier = modifier
    ) {
        if (pontos.isEmpty()) {
            EmptyMapState(
                modifier = Modifier.fillMaxSize()
            )
        } else {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                uiSettings = MapUiSettings(
                    zoomControlsEnabled = false,
                    compassEnabled = false,
                    myLocationButtonEnabled = false,
                    mapToolbarEnabled = false,
                    scrollGesturesEnabled = false,
                    zoomGesturesEnabled = false,
                    rotationGesturesEnabled = false,
                    tiltGesturesEnabled = false
                ),
                onMapClick = {
                    onClick()
                }
            ) {
                val coordenadas = pontos.map { ponto ->
                    LatLng(
                        ponto.latitude,
                        ponto.longitude
                    )
                }

                if (coordenadas.size > 1) {
                    Polyline(
                        points = coordenadas,
                        color = VerdeTracking,
                        width = 8f
                    )
                }

                coordenadas.lastOrNull()?.let { ultimaCoordenada ->
                    Marker(
                        state = MarkerState(
                            position = ultimaCoordenada
                        ),
                        title = "Última localização"
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyMapState(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.background(
            VerdeTracking.copy(alpha = 0.05f)
        ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.foundation.layout.Column(
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.Place,
                contentDescription = null,
                tint = VerdeTracking
            )

            Text(
                text = "A última localização aparecerá aqui",
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
            )
        }
    }
}