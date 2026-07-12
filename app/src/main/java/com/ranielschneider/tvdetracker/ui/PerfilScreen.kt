package com.ranielschneider.tvdetracker.ui

import android.content.Context
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
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
import com.ranielschneider.tvdetracker.AzulPrimario

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PerfilScreen(onVoltar: () -> Unit) {
    val context = LocalContext.current
    val prefs = context.getSharedPreferences("tvde_prefs", Context.MODE_PRIVATE)

    var nome by remember { mutableStateOf(prefs.getString("nome", "") ?: "") }
    var matricula by remember { mutableStateOf(prefs.getString("matricula", "") ?: "") }
    var nomeCarro by remember { mutableStateOf(prefs.getString("nomeCarro", "") ?: "") }
    var guardado by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Perfil", fontWeight = FontWeight.Bold) },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp)
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Informações do Condutor",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = AzulPrimario
            )

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = nome,
                onValueChange = { nome = it; guardado = false },
                label = { Text("Nome") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = matricula,
                onValueChange = { matricula = it.uppercase(); guardado = false },
                label = { Text("Matrícula") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = nomeCarro,
                onValueChange = { nomeCarro = it; guardado = false },
                label = { Text("Modelo do Carro") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = {
                    prefs.edit()
                        .putString("nome", nome)
                        .putString("matricula", matricula)
                        .putString("nomeCarro", nomeCarro)
                        .apply()
                    guardado = true
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = AzulPrimario)
            ) {
                Text("Guardar", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            if (guardado) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "✅ Perfil guardado com sucesso!",
                    color = Color(0xFF2E7D32),
                    fontSize = 14.sp
                )
            }
        }
    }
}