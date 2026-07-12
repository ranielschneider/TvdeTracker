package com.ranielschneider.tvdetracker.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import com.ranielschneider.tvdetracker.AzulPrimario
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import java.util.Calendar
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResumoScreen(onVoltar: () -> Unit) {
    val context = LocalContext.current
    var todasSessoes by remember { mutableStateOf<List<Sessao>>(emptyList()) }
    var filtroSelecionado by remember { mutableIntStateOf(0) }
    val filtros = listOf("Dia", "Semana", "Mês", "Ano")

    LaunchedEffect(Unit) {
        val db = TrackerDatabase.getDatabase(context)
        todasSessoes = db.trackerDao().buscarTodasSessoes()
    }

    val sessoesFiltradas = filtrarSessoes(todasSessoes, filtroSelecionado)
    val totalDias = sessoesFiltradas.map { formatarData(it.horaInicio) }.distinct().size
    val totalKm = sessoesFiltradas.sumOf { it.distanciaTotalMetros } / 1000.0
    val totalConduzidasMs = sessoesFiltradas.sumOf { it.horasConduzidasMs }
    val totalDuracaoMs = sessoesFiltradas.sumOf {
        (it.horaFim ?: it.horaInicio) - it.horaInicio
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Resumo de Trabalho", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Voltar"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = AzulPrimario,
                    titleContentColor = Color.White,
                    navigationIconContentColor = Color.White
                )
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            item {
                // Filtros
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    filtros.forEachIndexed { index, label ->
                        FilterChip(
                            selected = filtroSelecionado == index,
                            onClick = { filtroSelecionado = index },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Cards de resumo
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumoCard(
                        modifier = Modifier.weight(1f),
                        label = "Dias Trabalhados",
                        valor = "$totalDias"
                    )
                    ResumoCard(
                        modifier = Modifier.weight(1f),
                        label = "Distância Total",
                        valor = "%.1f km".format(totalKm)
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    ResumoCard(
                        modifier = Modifier.weight(1f),
                        label = "Horas Conduzidas",
                        valor = formatarMs(totalConduzidasMs)
                    )
                    ResumoCard(
                        modifier = Modifier.weight(1f),
                        label = "Tempo Total",
                        valor = formatarMs(totalDuracaoMs)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))

                Text(
                    text = "Sessões (${sessoesFiltradas.size})",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = AzulPrimario
                )

                Spacer(modifier = Modifier.height(8.dp))
            }

            items(sessoesFiltradas.size) { index ->
                val sessao = sessoesFiltradas.reversed()[index]
                SessaoCard(sessao = sessao, onClick = {})
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun ResumoCard(modifier: Modifier = Modifier, label: String, valor: String) {
    Card(
        modifier = modifier,
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = label, fontSize = 12.sp, color = Color.Gray)
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = valor, fontSize = 20.sp, fontWeight = FontWeight.Bold, color = AzulPrimario)
        }
    }
}

fun filtrarSessoes(sessoes: List<Sessao>, filtro: Int): List<Sessao> {
    val agora = Calendar.getInstance()
    return sessoes.filter { sessao ->
        val cal = Calendar.getInstance().apply { timeInMillis = sessao.horaInicio }
        when (filtro) {
            0 -> cal.get(Calendar.DAY_OF_YEAR) == agora.get(Calendar.DAY_OF_YEAR) &&
                    cal.get(Calendar.YEAR) == agora.get(Calendar.YEAR)
            1 -> cal.get(Calendar.WEEK_OF_YEAR) == agora.get(Calendar.WEEK_OF_YEAR) &&
                    cal.get(Calendar.YEAR) == agora.get(Calendar.YEAR)
            2 -> cal.get(Calendar.MONTH) == agora.get(Calendar.MONTH) &&
                    cal.get(Calendar.YEAR) == agora.get(Calendar.YEAR)
            3 -> cal.get(Calendar.YEAR) == agora.get(Calendar.YEAR)
            else -> true
        }
    }
}

fun formatarMs(ms: Long): String {
    if (ms <= 0) return "0h 00m"
    val horas = TimeUnit.MILLISECONDS.toHours(ms)
    val minutos = TimeUnit.MILLISECONDS.toMinutes(ms) % 60
    return "%dh %02dm".format(horas, minutos)
}