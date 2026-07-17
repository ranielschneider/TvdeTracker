package com.ranielschneider.tvdetracker

import android.content.Context
import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ranielschneider.tvdetracker.data.calcularDistanciaTotal
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.service.TrackerService
import com.ranielschneider.tvdetracker.ui.AppDrawer
import com.ranielschneider.tvdetracker.ui.HistoricoScreen
import com.ranielschneider.tvdetracker.ui.MapaScreen
import com.ranielschneider.tvdetracker.ui.ParticleBackground
import com.ranielschneider.tvdetracker.ui.PerfilScreen
import com.ranielschneider.tvdetracker.ui.PulseAnimation
import com.ranielschneider.tvdetracker.ui.ResumoScreen
import com.ranielschneider.tvdetracker.ui.RotasScreen
import com.ranielschneider.tvdetracker.ui.TypingText
import com.ranielschneider.tvdetracker.ui.saudacao
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusa
import com.ranielschneider.tvdetracker.ui.theme.AmareloParusaEscuro
import com.ranielschneider.tvdetracker.ui.theme.AzulPrimario
import com.ranielschneider.tvdetracker.ui.theme.TvdeTrackerTheme
import com.ranielschneider.tvdetracker.ui.theme.VerdeTracking
import com.ranielschneider.tvdetracker.ui.theme.VerdeTrackingEscuro
import com.ranielschneider.tvdetracker.ui.theme.VermelhoStop
import com.ranielschneider.tvdetracker.ui.theme.VermelhoStopEscuro
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TvdeTrackerTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    AppScreen()
                }
            }
        }
    }
}

enum class EstadoTracking { PARADO, A_TRACKING, EM_PAUSA }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScreen() {
    var tela by remember { mutableStateOf("tracker") }
    var ultimaSessaoId by remember { mutableLongStateOf(-1L) }
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("tvde_prefs", Context.MODE_PRIVATE)
    var nome by remember { mutableStateOf(prefs.getString("nome", "") ?: "") }
    var matricula by remember { mutableStateOf(prefs.getString("matricula", "") ?: "") }

    LaunchedEffect(tela) {
        nome = prefs.getString("nome", "") ?: ""
        matricula = prefs.getString("matricula", "") ?: ""
    }

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            AppDrawer(
                nomeUtilizador = nome,
                matricula = matricula,
                telaAtual = tela,
                onPerfil = { tela = "perfil" },
                onHistorico = { tela = "historico" },
                onRotas = { tela = "rotas" },
                onResumo = { tela = "resumo" },
                onFechar = { scope.launch { drawerState.close() } }
            )
        }
    ) {
        when (tela) {
            "mapa" -> MapaScreen(
                sessaoId = ultimaSessaoId,
                onVoltar = { tela = "historico" }
            )
            "historico" -> HistoricoScreen(
                onVerMapa = { sessaoId ->
                    ultimaSessaoId = sessaoId
                    tela = "mapa"
                },
                onVoltar = { tela = "tracker" }
            )
            "perfil" -> PerfilScreen(onVoltar = { tela = "tracker" })
            "rotas" -> RotasScreen(
                onVerMapa = { sessaoId ->
                    ultimaSessaoId = sessaoId
                    tela = "mapa"
                },
                onVoltar = { tela = "tracker" }
            )
            "resumo" -> ResumoScreen(onVoltar = { tela = "tracker" })
            else -> TrackerScreen(
                onVerMapa = { sessaoId ->
                    ultimaSessaoId = sessaoId
                    tela = "mapa"
                },
                onVerHistorico = { tela = "historico" },
                onAbrirMenu = { scope.launch { drawerState.open() } }
            )
        }
    }
}

@Composable
fun TrackerScreen(
    onVerMapa: (Long) -> Unit,
    onVerHistorico: () -> Unit,
    onAbrirMenu: () -> Unit
) {
    val context = LocalContext.current
    var estado by remember { mutableStateOf(EstadoTracking.PARADO) }
    var ultimaSessaoId by remember { mutableLongStateOf(-1L) }
    var ultimaDistanciaKm by remember { mutableStateOf(0.0) }
    var ultimasHorasMs by remember { mutableLongStateOf(0L) }
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
        temPermissao = permissoes[Manifest.permission.ACCESS_FINE_LOCATION] == true
    }

    val corPulso = when (estado) {
        EstadoTracking.A_TRACKING -> VerdeTracking
        EstadoTracking.EM_PAUSA -> AmareloParusa
        EstadoTracking.PARADO -> AzulPrimario
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 24.dp)
    ) {
        val prefs = context.getSharedPreferences("tvde_prefs", Context.MODE_PRIVATE)
        val nome = prefs.getString("nome", "") ?: ""
        val textoSaudacao = saudacao(nome)

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 52.dp, bottom = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "TVDE Tracker",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.6f)
                )
                TypingText(
                    texto = textoSaudacao,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onBackground,
                    velocidade = 55L
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = when (estado) {
                        EstadoTracking.PARADO -> "Pronto para iniciar"
                        EstadoTracking.A_TRACKING -> "A registar percurso..."
                        EstadoTracking.EM_PAUSA -> "Em pausa"
                    },
                    fontSize = 13.sp,
                    color = corPulso.copy(alpha = 0.8f)
                )
            }
            IconButton(onClick = onAbrirMenu) {
                Icon(
                    imageVector = Icons.Default.Menu,
                    contentDescription = "Menu",
                    tint = MaterialTheme.colorScheme.onBackground,
                    modifier = Modifier.size(26.dp)
                )
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            contentAlignment = Alignment.Center
        ) {
            ParticleBackground(
                modifier = Modifier.fillMaxSize(),
                color = corPulso
            )

            PulseAnimation(
                modifier = Modifier.size(280.dp),
                color = corPulso,
                ativo = estado == EstadoTracking.PARADO
            )

            when (estado) {
                EstadoTracking.PARADO -> {
                    if (!temPermissao) {
                        Button(
                            onClick = {
                                pedirPermissao.launch(
                                    arrayOf(
                                        Manifest.permission.ACCESS_FINE_LOCATION,
                                        Manifest.permission.ACCESS_COARSE_LOCATION
                                    )
                                )
                            },
                            modifier = Modifier.fillMaxWidth().height(56.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                        ) {
                            Text("Permitir GPS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        CircularActionButton(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_START
                                }
                                context.startForegroundService(intent)
                                estado = EstadoTracking.A_TRACKING
                                ultimaSessaoId = -1L
                            },
                            icon = Icons.Default.PlayArrow,
                            label = "START",
                            gradiente = listOf(Color(0xFF17C989), VerdeTracking, VerdeTrackingEscuro),
                            corSombra = VerdeTracking,
                            tamanho = 148.dp,
                            tamanhoIcone = 30.dp,
                            tamanhoTexto = 15.sp
                        )
                    }
                }

                EstadoTracking.A_TRACKING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        CircularActionButton(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_PAUSE
                                }
                                context.startService(intent)
                                estado = EstadoTracking.EM_PAUSA
                            },
                            icon = Icons.Default.Pause,
                            label = "PAUSA",
                            gradiente = listOf(AmareloParusa, AmareloParusaEscuro),
                            corSombra = AmareloParusa,
                            corTexto = Color(0xFF241900)
                        )

                        CircularActionButton(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_STOP
                                }
                                context.startService(intent)
                                estado = EstadoTracking.PARADO

                                CoroutineScope(Dispatchers.IO).launch {
                                    val db = TrackerDatabase.getDatabase(context)
                                    val todasSessoes = db.trackerDao().buscarTodasSessoes()
                                    if (todasSessoes.isNotEmpty()) {
                                        val sessao = todasSessoes.last()
                                        val pontos = db.trackerDao().buscarPontosDaSessao(sessao.id)
                                        val pausas = db.trackerDao().buscarPausasDaSessao(sessao.id)
                                        val distancia = calcularDistanciaTotal(pontos)
                                        val horaFim = System.currentTimeMillis()
                                        val tempoPausaTotal = pausas.sumOf {
                                            (it.fimPausa ?: horaFim) - it.inicioPausa
                                        }
                                        val horasConduzidasMs = (horaFim - sessao.horaInicio) - tempoPausaTotal
                                        db.trackerDao().fecharSessao(
                                            sessaoId = sessao.id,
                                            horaFim = horaFim,
                                            distancia = distancia * 1000,
                                            horasConduzidasMs = horasConduzidasMs
                                        )
                                        withContext(Dispatchers.Main) {
                                            ultimaSessaoId = sessao.id
                                            ultimaDistanciaKm = distancia
                                            ultimasHorasMs = horasConduzidasMs
                                        }
                                    }
                                }
                            },
                            icon = Icons.Default.Stop,
                            label = "STOP",
                            gradiente = listOf(VermelhoStop, VermelhoStopEscuro),
                            corSombra = VermelhoStop
                        )
                    }
                }

                EstadoTracking.EM_PAUSA -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(20.dp)) {
                        CircularActionButton(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_RESUME
                                }
                                context.startService(intent)
                                estado = EstadoTracking.A_TRACKING
                            },
                            icon = Icons.Default.PlayArrow,
                            label = "RESUME",
                            gradiente = listOf(VerdeTracking, VerdeTrackingEscuro),
                            corSombra = VerdeTracking
                        )

                        CircularActionButton(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_STOP
                                }
                                context.startService(intent)
                                estado = EstadoTracking.PARADO

                                CoroutineScope(Dispatchers.IO).launch {
                                    val db = TrackerDatabase.getDatabase(context)
                                    val todasSessoes = db.trackerDao().buscarTodasSessoes()
                                    if (todasSessoes.isNotEmpty()) {
                                        val sessao = todasSessoes.last()
                                        val pontos = db.trackerDao().buscarPontosDaSessao(sessao.id)
                                        val pausas = db.trackerDao().buscarPausasDaSessao(sessao.id)
                                        val distancia = calcularDistanciaTotal(pontos)
                                        val horaFim = System.currentTimeMillis()
                                        val tempoPausaTotal = pausas.sumOf {
                                            (it.fimPausa ?: horaFim) - it.inicioPausa
                                        }
                                        val horasConduzidasMs = (horaFim - sessao.horaInicio) - tempoPausaTotal
                                        db.trackerDao().fecharSessao(
                                            sessaoId = sessao.id,
                                            horaFim = horaFim,
                                            distancia = distancia * 1000,
                                            horasConduzidasMs = horasConduzidasMs
                                        )
                                        withContext(Dispatchers.Main) {
                                            ultimaSessaoId = sessao.id
                                            ultimaDistanciaKm = distancia
                                            ultimasHorasMs = horasConduzidasMs
                                        }
                                    }
                                }
                            },
                            icon = Icons.Default.Stop,
                            label = "STOP",
                            gradiente = listOf(VermelhoStop, VermelhoStopEscuro),
                            corSombra = VermelhoStop
                        )
                    }
                }
            }
        }

        if (ultimaSessaoId != -1L) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Conduzido", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            text = formatarMs(ultimasHorasMs),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }

                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                    elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text("Distância", fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
                        Text(
                            text = "%.1f km".format(ultimaDistanciaKm),
                            fontSize = 20.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }

            Button(
                onClick = { onVerMapa(ultimaSessaoId) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                Text("🗺  Ver Mapa do Percurso", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        OutlinedButton(
            onClick = onVerHistorico,
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = RoundedCornerShape(12.dp)
        ) {
            Text("📋  Histórico de Percursos", fontSize = 14.sp, color = AzulPrimario)
        }

        Spacer(modifier = Modifier.height(28.dp))
    }
}

@Composable
fun CircularActionButton(
    onClick: () -> Unit,
    icon: ImageVector,
    label: String,
    gradiente: List<Color>,
    corSombra: Color,
    tamanho: Dp = 120.dp,
    corTexto: Color = Color.White,
    tamanhoIcone: Dp = 24.dp,
    tamanhoTexto: TextUnit = 12.sp
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(targetValue = if (isPressed) 0.94f else 1f, label = "actionScale")

    Box(
        modifier = Modifier
            .size(tamanho)
            .scale(scale)
            .shadow(
                elevation = 16.dp,
                shape = CircleShape,
                ambientColor = corSombra,
                spotColor = corSombra
            )
            .clip(CircleShape)
            .background(brush = Brush.verticalGradient(colors = gradiente))
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = corTexto,
                modifier = Modifier.size(tamanhoIcone)
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = label,
                color = corTexto,
                fontSize = tamanhoTexto,
                fontWeight = FontWeight.Black,
                letterSpacing = 0.8.sp
            )
        }
    }
}

fun formatarMs(ms: Long): String {
    if (ms <= 0) return "0h 00m"
    val horas = java.util.concurrent.TimeUnit.MILLISECONDS.toHours(ms)
    val minutos = java.util.concurrent.TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return "%dh %02dm".format(horas, minutos)
}