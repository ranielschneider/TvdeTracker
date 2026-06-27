package com.ranielschneider.tvdetracker

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TesteRoomScreen()
                }
            }
        }
    }
}

@androidx.compose.runtime.Composable
fun TesteRoomScreen() {
    val context = androidx.compose.ui.platform.LocalContext.current
    var resultado by remember { mutableStateOf("Carregando...") }

    LaunchedEffect(Unit) {
        val db = TrackerDatabase.getDatabase(context)
        val dao = db.trackerDao()

        // 1. Cria uma sessao de teste
        val sessaoId = dao.inserirSessao(
            Sessao(horaInicio = System.currentTimeMillis())
        )

        // 2. Cria 3 pontos GPS de teste, ligados a essa sessao
        dao.inserirPontoGps(PontoGps(latitude = 41.15, longitude = -8.61, timestamp = System.currentTimeMillis(), sessaoId = sessaoId))
        dao.inserirPontoGps(PontoGps(latitude = 41.16, longitude = -8.62, timestamp = System.currentTimeMillis(), sessaoId = sessaoId))
        dao.inserirPontoGps(PontoGps(latitude = 41.17, longitude = -8.63, timestamp = System.currentTimeMillis(), sessaoId = sessaoId))

        // 3. Busca de volta para confirmar
        val pontos = dao.buscarPontosDaSessao(sessaoId)

        resultado = "Sessao criada com id=$sessaoId\nPontos encontrados: ${pontos.size}\n" +
                pontos.joinToString("\n") { "lat=${it.latitude}, lng=${it.longitude}" }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Text(text = resultado)
    }
}