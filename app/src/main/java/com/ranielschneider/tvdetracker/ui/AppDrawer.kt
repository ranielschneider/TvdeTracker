package com.ranielschneider.tvdetracker.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.ranielschneider.tvdetracker.AzulPrimario

@Composable
fun AppDrawer(
    nomeUtilizador: String,
    matricula: String,
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
            .background(Color.White)
    ) {
        // Header do Drawer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AzulPrimario)
                .padding(24.dp)
        ) {
            Icon(
                imageVector = Icons.Default.AccountCircle,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = if (nomeUtilizador.isNotEmpty()) nomeUtilizador else "Condutor",
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
            if (matricula.isNotEmpty()) {
                Text(
                    text = matricula,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 14.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(8.dp))

        DrawerItem(
            icon = Icons.Default.AccountCircle,
            label = "Perfil",
            onClick = { onPerfil(); onFechar() }
        )

        DrawerItem(
            icon = Icons.Default.History,
            label = "Histórico",
            onClick = { onHistorico(); onFechar() }
        )

        DrawerItem(
            icon = Icons.Default.Map,
            label = "Rotas",
            onClick = { onRotas(); onFechar() }
        )

        HorizontalDivider(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))

        DrawerItem(
            icon = Icons.Default.DateRange,
            label = "Resumo de Trabalho",
            onClick = { onResumo(); onFechar() }
        )
    }
}

@Composable
fun DrawerItem(icon: ImageVector, label: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = AzulPrimario,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = Color.DarkGray
        )
    }
}