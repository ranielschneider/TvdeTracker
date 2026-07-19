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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import com.ranielschneider.tvdetracker.ui.theme.AzulPrimario
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoricoScreen(
    onVerMapa: (Long) -> Unit,
    onVoltar: () -> Unit
) {
    val context = LocalContext.current

    var sessoes by remember {
        mutableStateOf<List<Sessao>>(emptyList())
    }

    LaunchedEffect(Unit) {
        val dao = TrackerDatabase
            .getDatabase(context)
            .trackerDao()

        sessoes = dao
            .buscarTodasSessoes()
            .sortedByDescending { it.horaInicio }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Histórico de Percursos",
                        fontWeight = FontWeight.Bold
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onVoltar
                    ) {
                        Icon(
                            imageVector =
                                Icons.AutoMirrored.Filled.ArrowBack,
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
        if (sessoes.isEmpty()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Nenhum percurso registado ainda.",
                    color = MaterialTheme.colorScheme.onBackground.copy(
                        alpha = 0.55f
                    ),
                    fontSize = 15.sp
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentPadding = androidx.compose.foundation.layout.PaddingValues(
                    start = 16.dp,
                    top = 16.dp,
                    end = 16.dp,
                    bottom = 24.dp
                ),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = sessoes,
                    key = { sessao -> sessao.id }
                ) { sessao ->
                    SessaoCard(
                        sessao = sessao,
                        onClick = {
                            onVerMapa(sessao.id)
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun SessaoCard(
    sessao: Sessao,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(
            defaultElevation = 3.dp
        ),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 16.dp,
                vertical = 16.dp
            )
        ) {
            Text(
                text = formatarData(sessao.horaInicio),
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.55f
                ),
                fontWeight = FontWeight.SemiBold
            )

            Spacer(
                modifier = Modifier.height(10.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text =
                        "${formatarHora(sessao.horaInicio)} → " +
                                (
                                        sessao.horaFim?.let {
                                            formatarHora(it)
                                        } ?: "--:--"
                                        ),
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )

                Text(
                    text = if (sessao.horaFim == null) {
                        "EM CURSO"
                    } else {
                        "CONCLUÍDA"
                    },
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (sessao.horaFim == null) {
                        Color(0xFFF59E0B)
                    } else {
                        AzulPrimario
                    }
                )
            }

            Spacer(
                modifier = Modifier.height(12.dp)
            )

            HorizontalDivider(
                color = MaterialTheme.colorScheme.onSurface.copy(
                    alpha = 0.08f
                )
            )

            Spacer(
                modifier = Modifier.height(14.dp)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                HistoricoInfoItem(
                    label = "Duração",
                    valor = calcularDuracao(
                        inicio = sessao.horaInicio,
                        fim = sessao.horaFim
                    )
                )

                HistoricoInfoItem(
                    label = "Conduzido",
                    valor = calcularDuracaoMs(
                        sessao.horasConduzidasMs
                    )
                )

                HistoricoInfoItem(
                    label = "Distância",
                    valor = "%.2f km".format(
                        sessao.distanciaTotalMetros / 1000.0
                    )
                )
            }
        }
    }
}

@Composable
private fun HistoricoInfoItem(
    label: String,
    valor: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurface.copy(
                alpha = 0.50f
            )
        )

        Text(
            text = valor,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

fun formatarData(
    timestamp: Long
): String {
    val formatter = SimpleDateFormat(
        "dd/MM/yyyy",
        Locale.getDefault()
    )

    return formatter.format(
        Date(timestamp)
    )
}

fun calcularDuracaoMs(
    ms: Long
): String {
    if (ms <= 0L) {
        return "0h 00m"
    }

    val horas = TimeUnit.MILLISECONDS
        .toHours(ms)

    val minutos = TimeUnit.MILLISECONDS
        .toMinutes(ms) % 60

    return "%dh %02dm".format(
        horas,
        minutos
    )
}