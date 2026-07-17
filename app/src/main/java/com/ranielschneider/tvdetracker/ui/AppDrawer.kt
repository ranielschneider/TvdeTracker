package com.ranielschneider.tvdetracker.ui
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Map
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.ui.theme.AzulPrimario

@Composable
fun AppDrawer(
    nomeUtilizador: String,
    matricula: String,
    telaAtual: String = "perfil",
    onPerfil: () -> Unit,
    onHistorico: () -> Unit,
    onRotas: () -> Unit,
    onResumo: () -> Unit,
    onFechar: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(Color(0xFFFAFCFB).copy(alpha = 0.75f))
            .padding(top = 40.dp, start = 20.dp, end = 20.dp)
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(color = AzulPrimario, shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = if (nomeUtilizador.isNotEmpty()) nomeUtilizador.first().uppercase() else "?",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = if (nomeUtilizador.isNotEmpty()) nomeUtilizador else "Condutor",
            color = Color(0xFF0B1220),
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold
        )
        if (matricula.isNotEmpty()) {
            Text(
                text = matricula,
                color = Color(0xFF64748B),
                fontSize = 13.sp
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        DrawerItem(
            icon = Icons.Default.AccountCircle,
            label = "Perfil",
            selecionado = telaAtual == "perfil",
            onClick = { onPerfil(); onFechar() }
        )
        DrawerItem(
            icon = Icons.Default.History,
            label = "Histórico",
            selecionado = telaAtual == "historico",
            onClick = { onHistorico(); onFechar() }
        )
        DrawerItem(
            icon = Icons.Default.Map,
            label = "Rotas",
            selecionado = telaAtual == "rotas",
            onClick = { onRotas(); onFechar() }
        )

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider(color = Color(0xFFE6EAF0))
        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem(
            icon = Icons.Default.DateRange,
            label = "Resumo de Trabalho",
            selecionado = telaAtual == "resumo",
            onClick = { onResumo(); onFechar() }
        )
    }
}

@Composable
fun DrawerItem(
    icon: ImageVector,
    label: String,
    selecionado: Boolean = false,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (selecionado) AzulPrimario.copy(alpha = 0.12f) else Color.Transparent)
            .clickable { onClick() }
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AzulPrimario,
            modifier = Modifier.size(22.dp)
        )
        Spacer(modifier = Modifier.width(14.dp))
        Text(
            text = label,
            fontSize = 15.sp,
            fontWeight = if (selecionado) FontWeight.SemiBold else FontWeight.Normal,
            color = if (selecionado) Color(0xFF0B1220) else Color(0xFF475569)
        )
    }
}