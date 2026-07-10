package com.ranielschneider.tvdetracker.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase

@Composable
fun HistoricoScreen(onVerMapa: (Long) -> Unit) {
    val context = LocalContext.current
    var sessoes by remember { mutableStateOf<List<Sessao>>(emptyList()) }

    LaunchedEffect(Unit) {
        val db = TrackerDatabase.getDatabase(context)
        sessoes = db.trackerDao().buscarTodasSessoes().reversed()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Histórico de Percursos",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (sessoes.isEmpty()) {
            Text(text = "Nenhum percurso registado ainda.", color = Color.Gray)
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(sessoes) { sessao ->
                    SessaoCard(sessao = sessao, onClick = { onVerMapa(sessao.id) })
                }
            }
        }
    }
}

@Composable
fun SessaoCard(sessao: Sessao, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = formatarData(sessao.horaInicio),
                fontSize = 14.sp,
                color = Color.Gray
            )
            Spacer(modifier = Modifier.height(4.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                InfoItem(label = "Início", valor = formatarHora(sessao.horaInicio))
                InfoItem(label = "Fim", valor = sessao.horaFim?.let { formatarHora(it) } ?: "--:--")
                InfoItem(label = "Duração", valor = calcularDuracao(sessao.horaInicio, sessao.horaFim))
                InfoItem(
                    label = "Distância",
                    valor = "%.2f km".format(sessao.distanciaTotalMetros / 1000.0)
                )
            }
        }
    }
}

fun formatarData(timestamp: Long): String {
    val sdf = java.text.SimpleDateFormat("dd/MM/yyyy", java.util.Locale.getDefault())
    return sdf.format(java.util.Date(timestamp))
}