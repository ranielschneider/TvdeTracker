package com.ranielschneider.tvdetracker.ui

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.BarChart
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Route
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.rememberCameraPositionState
import com.ranielschneider.tvdetracker.data.calcularDistanciaTotal
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.service.TrackerService
import com.ranielschneider.tvdetracker.ui.components.HomeHeader
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusa
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking
import com.ranielschneider.tvdetracker.ui.theme.VermelhoStop
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import java.util.concurrent.TimeUnit

enum class EstadoTracking {
    PARADO,
    A_TRACKING,
    EM_PAUSA
}

@Composable
fun TrackerScreen(
    onVerMapa: (Long) -> Unit,
    onVerHistorico: () -> Unit,
    onAbrirMenu: () -> Unit,
    onAbrirRotas: () -> Unit,
    onAbrirResumo: () -> Unit,
    onAbrirPerfil: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val prefs = remember(context) {
        context.getSharedPreferences("tvde_prefs", Context.MODE_PRIVATE)
    }

    val nome = prefs.getString("nome", "").orEmpty()

    var estado by remember { mutableStateOf(EstadoTracking.PARADO) }
    var sessoes by remember { mutableStateOf<List<Sessao>>(emptyList()) }
    var pontosUltimaSessao by remember { mutableStateOf<List<PontoGps>>(emptyList()) }
    var carregando by remember { mutableStateOf(true) }

    var temPermissao by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val pedirPermissao = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissoes ->
        temPermissao =
            permissoes[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                    permissoes[Manifest.permission.ACCESS_COARSE_LOCATION] == true
    }

    suspend fun atualizarDados() {
        carregando = true

        val resultado = withContext(Dispatchers.IO) {
            val dao = TrackerDatabase.getDatabase(context).trackerDao()
            val lista = dao.buscarTodasSessoes()
            val ultima = lista.maxByOrNull { it.horaInicio }
            val pontos = if (ultima != null) {
                dao.buscarPontosDaSessao(ultima.id)
            } else {
                emptyList()
            }
            lista to pontos
        }

        sessoes = resultado.first
        pontosUltimaSessao = resultado.second
        carregando = false
    }

    LaunchedEffect(Unit) {
        atualizarDados()
    }

    val sessoesHoje = sessoes.filter {
        isSameDay(it.horaInicio, System.currentTimeMillis())
    }
    val ultimaSessao = sessoes.maxByOrNull { it.horaInicio }

    val tempoTotalHojeMs = sessoesHoje.sumOf { sessao ->
        val fim = sessao.horaFim ?: System.currentTimeMillis()
        (fim - sessao.horaInicio).coerceAtLeast(0L)
    }

    val tempoConduzidoHojeMs = sessoesHoje.sumOf {
        it.horasConduzidasMs
    }

    val distanciaHojeKm = sessoesHoje.sumOf {
        it.distanciaTotalMetros
    } / 1000.0

    val velocidadeMediaHoje = if (tempoConduzidoHojeMs > 0L) {
        distanciaHojeKm / (tempoConduzidoHojeMs / 3_600_000.0)
    } else {
        0.0
    }

    Scaffold(
        bottomBar = {
            HomeBottomNavigation(
                onAbrirRotas = onAbrirRotas,
                onAbrirResumo = onAbrirResumo,
                onAbrirPerfil = onAbrirPerfil
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(
                start = 20.dp,
                top = 20.dp,
                end = 20.dp,
                bottom = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            item {
                HomeHeader(
                    nome = nome,
                    estado = estado,
                    onAbrirMenu = onAbrirMenu
                )
            }

            item {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    Text(
                        text = "Resumo de hoje",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )

                    DashboardStatsRow(
                        tempoTotalHojeMs = tempoTotalHojeMs,
                        distanciaHojeKm = distanciaHojeKm,
                        velocidadeMediaHoje = velocidadeMediaHoje,
                        quantidadeSessoes = sessoesHoje.size,
                        carregando = carregando
                    )
                }
            }

            item {
                MainJourneyButton(
                    estado = estado,
                    temPermissao = temPermissao,
                    onPedirPermissao = {
                        pedirPermissao.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    onIniciar = {
                        val intent = Intent(context, TrackerService::class.java).apply {
                            action = TrackerService.ACAO_START
                        }
                        context.startForegroundService(intent)
                        estado = EstadoTracking.A_TRACKING
                    },
                    onPausar = {
                        val intent = Intent(context, TrackerService::class.java).apply {
                            action = TrackerService.ACAO_PAUSE
                        }
                        context.startService(intent)
                        estado = EstadoTracking.EM_PAUSA
                    },
                    onRetomar = {
                        val intent = Intent(context, TrackerService::class.java).apply {
                            action = TrackerService.ACAO_RESUME
                        }
                        context.startService(intent)
                        estado = EstadoTracking.A_TRACKING
                    },
                    onEncerrar = {
                        val intent = Intent(context, TrackerService::class.java).apply {
                            action = TrackerService.ACAO_STOP
                        }
                        context.startService(intent)
                        estado = EstadoTracking.PARADO

                        scope.launch {
                            finalizarUltimaSessao(context)
                            atualizarDados()
                        }
                    }
                )
            }

            item {
                LiveMapCard(
                    pontos = pontosUltimaSessao,
                    ultimaSessaoId = ultimaSessao?.id,
                    onVerMapa = onVerMapa
                )
            }

            item {
                LastJourneyDashboardCard(
                    sessao = ultimaSessao,
                    onVerMapa = onVerMapa,
                    onVerHistorico = onVerHistorico
                )
            }

            item {
                FrequentZoneCard()
            }
        }
    }
}

@Composable
private fun DashboardStatsRow(
    tempoTotalHojeMs: Long,
    distanciaHojeKm: Double,
    velocidadeMediaHoje: Double,
    quantidadeSessoes: Int,
    carregando: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        DashboardStatCard(
            modifier = Modifier.weight(1f),
            icon = Icons.Default.AccessTime,
            label = "Tempo",
            value = if (carregando) "..." else formatDurationHome(tempoTotalHojeMs)
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

@Composable
private fun MainJourneyButton(
    estado: EstadoTracking,
    temPermissao: Boolean,
    onPedirPermissao: () -> Unit,
    onIniciar: () -> Unit,
    onPausar: () -> Unit,
    onRetomar: () -> Unit,
    onEncerrar: () -> Unit
) {
    when (estado) {
        EstadoTracking.PARADO -> {
            Button(
                onClick = if (temPermissao) onIniciar else onPedirPermissao,
                modifier = Modifier
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

                Column(horizontalAlignment = Alignment.Start) {
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

        EstadoTracking.A_TRACKING -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onPausar,
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AmareloParusa
                    )
                ) {
                    Icon(Icons.Default.Pause, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("PAUSAR", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEncerrar,
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VermelhoStop
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("ENCERRAR", fontWeight = FontWeight.Bold)
                }
            }
        }

        EstadoTracking.EM_PAUSA -> {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = onRetomar,
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VerdeTracking
                    )
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("RETOMAR", fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = onEncerrar,
                    modifier = Modifier
                        .weight(1f)
                        .height(62.dp),
                    shape = RoundedCornerShape(22.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = VermelhoStop
                    )
                ) {
                    Icon(Icons.Default.Stop, contentDescription = null)
                    Spacer(modifier = Modifier.size(6.dp))
                    Text("ENCERRAR", fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
private fun LiveMapCard(
    pontos: List<PontoGps>,
    ultimaSessaoId: Long?,
    onVerMapa: (Long) -> Unit
) {
    val cameraPositionState = rememberCameraPositionState()

    LaunchedEffect(pontos) {
        pontos.lastOrNull()?.let { ponto ->
            cameraPositionState.animate(
                CameraUpdateFactory.newCameraPosition(
                    CameraPosition.fromLatLngZoom(
                        LatLng(ponto.latitude, ponto.longitude),
                        13f
                    )
                )
            )
        }
    }

    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Map,
                    contentDescription = null,
                    tint = VerdeTracking
                )
                Spacer(modifier = Modifier.size(8.dp))
                Text(
                    text = "Mapa em tempo real",
                    modifier = Modifier.weight(1f),
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "Ver mapa completo",
                    color = VerdeTracking,
                    fontSize = 13.sp
                )
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
            ) {
                if (pontos.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(VerdeTracking.copy(alpha = 0.05f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Default.Place,
                                contentDescription = null,
                                tint = VerdeTracking,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "A última localização aparecerá aqui",
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                            )
                        }
                    }
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
                            ultimaSessaoId?.let(onVerMapa)
                        }
                    ) {
                        val latLngs = pontos.map {
                            LatLng(it.latitude, it.longitude)
                        }

                        if (latLngs.size > 1) {
                            Polyline(
                                points = latLngs,
                                color = VerdeTracking,
                                width = 8f
                            )
                        }

                        latLngs.lastOrNull()?.let { last ->
                            Marker(
                                state = MarkerState(position = last),
                                title = "Última localização"
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LastJourneyDashboardCard(
    sessao: Sessao?,
    onVerMapa: (Long) -> Unit,
    onVerHistorico: () -> Unit
) {
    Card(
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
            Row(verticalAlignment = Alignment.CenterVertically) {
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
                Text(
                    text = "Ver todas",
                    color = VerdeTracking,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (sessao == null) {
                Text(
                    text = "Ainda não existem jornadas registadas.",
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                )
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
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

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(40.dp))
                            .background(VerdeTracking.copy(alpha = 0.12f))
                            .padding(horizontal = 14.dp, vertical = 8.dp)
                    ) {
                        Text(
                            text = if (sessao.horaFim == null) "EM CURSO" else "CONCLUÍDA",
                            color = VerdeTracking,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    JourneyInfo(
                        icon = Icons.Default.Route,
                        value = "%.1f km".format(sessao.distanciaTotalMetros / 1000.0),
                        label = "Distância"
                    )
                    JourneyInfo(
                        icon = Icons.Default.AccessTime,
                        value = formatDurationHome(
                            (sessao.horaFim ?: System.currentTimeMillis()) -
                                    sessao.horaInicio
                        ),
                        label = "Tempo total"
                    )
                    JourneyInfo(
                        icon = Icons.Default.DirectionsCar,
                        value = formatDurationHome(sessao.horasConduzidasMs),
                        label = "Conduzido"
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    Button(
                        onClick = { onVerMapa(sessao.id) },
                        enabled = sessao.horaFim != null,
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = VerdeTracking
                        )
                    ) {
                        Icon(Icons.Default.Map, contentDescription = null)
                        Spacer(modifier = Modifier.size(6.dp))
                        Text("Ver no mapa")
                    }
                }
            }
        }
    }
}

@Composable
private fun JourneyInfo(
    icon: ImageVector,
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
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

@Composable
private fun FrequentZoneCard() {
    Card(
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(VerdeTracking.copy(alpha = 0.10f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Place,
                    contentDescription = null,
                    tint = VerdeTracking
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 14.dp)
            ) {
                Text(
                    text = "Zona mais frequente (7 dias)",
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Disponível numa próxima etapa",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.58f)
                )
            }

            Icon(
                imageVector = Icons.Default.BarChart,
                contentDescription = null,
                tint = VerdeTracking
            )
        }
    }
}

@Composable
private fun HomeBottomNavigation(
    onAbrirRotas: () -> Unit,
    onAbrirResumo: () -> Unit,
    onAbrirPerfil: () -> Unit
) {
    NavigationBar(
        containerColor = MaterialTheme.colorScheme.surface,
        modifier = Modifier.navigationBarsPadding()
    ) {
        NavigationBarItem(
            selected = true,
            onClick = {},
            icon = {
                Icon(Icons.Default.Home, contentDescription = null)
            },
            label = {
                Text("Início")
            },
            colors = homeNavigationColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = onAbrirRotas,
            icon = {
                Icon(Icons.Default.Map, contentDescription = null)
            },
            label = {
                Text("Rotas")
            },
            colors = homeNavigationColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = onAbrirResumo,
            icon = {
                Icon(Icons.Default.BarChart, contentDescription = null)
            },
            label = {
                Text("Estatísticas")
            },
            colors = homeNavigationColors()
        )

        NavigationBarItem(
            selected = false,
            onClick = onAbrirPerfil,
            icon = {
                Icon(Icons.Default.Settings, contentDescription = null)
            },
            label = {
                Text("Configurações")
            },
            colors = homeNavigationColors()
        )
    }
}

@Composable
private fun homeNavigationColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = VerdeTracking,
    selectedTextColor = VerdeTracking,
    indicatorColor = VerdeTracking.copy(alpha = 0.10f)
)

private suspend fun finalizarUltimaSessao(context: Context) {
    withContext(Dispatchers.IO) {
        val dao = TrackerDatabase.getDatabase(context).trackerDao()
        val sessoes = dao.buscarTodasSessoes()

        val sessao = sessoes
            .filter { it.horaFim == null }
            .maxByOrNull { it.horaInicio }
            ?: return@withContext

        val horaFim = System.currentTimeMillis()
        val pontos = dao.buscarPontosDaSessao(sessao.id)
        val pausas = dao.buscarPausasDaSessao(sessao.id)
        val distanciaKm = calcularDistanciaTotal(pontos)

        val tempoPausaTotal = pausas.sumOf { pausa ->
            (pausa.fimPausa ?: horaFim) - pausa.inicioPausa
        }

        val horasConduzidasMs =
            ((horaFim - sessao.horaInicio) - tempoPausaTotal)
                .coerceAtLeast(0L)

        dao.fecharSessao(
            sessaoId = sessao.id,
            horaFim = horaFim,
            distancia = distanciaKm * 1000.0,
            horasConduzidasMs = horasConduzidasMs
        )
    }
}

private fun isSameDay(first: Long, second: Long): Boolean {
    val firstCalendar = Calendar.getInstance().apply {
        timeInMillis = first
    }
    val secondCalendar = Calendar.getInstance().apply {
        timeInMillis = second
    }

    return firstCalendar.get(Calendar.YEAR) ==
            secondCalendar.get(Calendar.YEAR) &&
            firstCalendar.get(Calendar.DAY_OF_YEAR) ==
            secondCalendar.get(Calendar.DAY_OF_YEAR)
}

private fun formatDurationHome(ms: Long): String {
    if (ms <= 0L) return "0h 00m"

    val horas = TimeUnit.MILLISECONDS.toHours(ms)
    val minutos = TimeUnit.MILLISECONDS.toMinutes(ms) % 60

    return "%dh %02dm".format(horas, minutos)
}