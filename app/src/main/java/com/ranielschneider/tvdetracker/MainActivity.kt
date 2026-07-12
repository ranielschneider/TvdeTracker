package com.ranielschneider.tvdetracker

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ranielschneider.tvdetracker.data.calcularDistanciaTotal
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.service.TrackerService
import com.ranielschneider.tvdetracker.ui.HistoricoScreen
import com.ranielschneider.tvdetracker.ui.MapaScreen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

val VerdeTracking = Color(0xFF2E7D32)
val VermelhoStop = Color(0xFFC62828)
val AzulPrimario = Color(0xFF1565C0)
val AmareloParusa = Color(0xFFF9A825)

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    AppScreen()
                }
            }
        }
    }
}

@Composable
fun AppScreen() {
    var tela by remember { mutableStateOf("tracker") }
    var ultimaSessaoId by remember { mutableLongStateOf(-1L) }

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
        else -> TrackerScreen(
            onVerMapa = { sessaoId ->
                ultimaSessaoId = sessaoId
                tela = "mapa"
            },
            onVerHistorico = { tela = "historico" }
        )
    }
}

enum class EstadoTracking { PARADO, A_TRACKING, EM_PAUSA }

@Composable
fun TrackerScreen(onVerMapa: (Long) -> Unit, onVerHistorico: () -> Unit) {
    val context = LocalContext.current
    var estado by remember { mutableStateOf(EstadoTracking.PARADO) }
    var ultimaSessaoId by remember { mutableLongStateOf(-1L) }
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

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "TVDE Tracker",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = AzulPrimario
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = when (estado) {
                EstadoTracking.PARADO -> "⚪ Pronto para iniciar"
                EstadoTracking.A_TRACKING -> "🟢 A registar percurso..."
                EstadoTracking.EM_PAUSA -> "🟡 Em pausa"
            },
            fontSize = 16.sp,
            color = when (estado) {
                EstadoTracking.PARADO -> Color.Gray
                EstadoTracking.A_TRACKING -> VerdeTracking
                EstadoTracking.EM_PAUSA -> AmareloParusa
            }
        )

        Spacer(modifier = Modifier.height(48.dp))

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
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                Text("Permitir GPS", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        } else {
            when (estado) {
                EstadoTracking.PARADO -> {
                    Button(
                        onClick = {
                            val intent = Intent(context, TrackerService::class.java).apply {
                                action = TrackerService.ACAO_START
                            }
                            context.startForegroundService(intent)
                            estado = EstadoTracking.A_TRACKING
                            ultimaSessaoId = -1L
                        },
                        modifier = Modifier.size(160.dp),
                        shape = CircleShape,
                        colors = ButtonDefaults.buttonColors(containerColor = VerdeTracking)
                    ) {
                        Text("▶  START", fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    }
                }

                EstadoTracking.A_TRACKING -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_PAUSE
                                }
                                context.startService(intent)
                                estado = EstadoTracking.EM_PAUSA
                            },
                            modifier = Modifier.size(130.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = AmareloParusa)
                        ) {
                            Text("⏸  PAUSA", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.Black)
                        }

                        Button(
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
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(130.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = VermelhoStop)
                        ) {
                            Text("⏹  STOP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                EstadoTracking.EM_PAUSA -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                        Button(
                            onClick = {
                                val intent = Intent(context, TrackerService::class.java).apply {
                                    action = TrackerService.ACAO_RESUME
                                }
                                context.startService(intent)
                                estado = EstadoTracking.A_TRACKING
                            },
                            modifier = Modifier.size(130.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = VerdeTracking)
                        ) {
                            Text("▶  RESUME", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }

                        Button(
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
                                        }
                                    }
                                }
                            },
                            modifier = Modifier.size(130.dp),
                            shape = CircleShape,
                            colors = ButtonDefaults.buttonColors(containerColor = VermelhoStop)
                        ) {
                            Text("⏹  STOP", fontSize = 14.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            if (ultimaSessaoId != -1L) {
                Spacer(modifier = Modifier.height(24.dp))
                Button(
                    onClick = { onVerMapa(ultimaSessaoId) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
                ) {
                    Text("🗺  Ver Mapa do Percurso", fontSize = 15.sp, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = onVerHistorico,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("📋  Histórico de Percursos", fontSize = 15.sp, color = AzulPrimario)
            }
        }
    }
}