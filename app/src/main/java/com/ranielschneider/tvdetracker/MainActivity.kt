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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.ranielschneider.tvdetracker.service.TrackerService

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    TrackerScreen()
                }
            }
        }
    }
}

@Composable
fun TrackerScreen() {
    val context = LocalContext.current
    var statusTracking by remember { mutableStateOf(false) }
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
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (statusTracking) "🟢 A registar percurso..." else "⚪ Parado",
            fontSize = 20.sp
        )

        Spacer(modifier = Modifier.height(32.dp))

        if (!temPermissao) {
            Button(onClick = {
                pedirPermissao.launch(
                    arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION
                    )
                )
            }) {
                Text("Permitir GPS")
            }
        } else {
            if (!statusTracking) {
                Button(onClick = {
                    val intent = Intent(context, TrackerService::class.java)
                    context.startForegroundService(intent)
                    statusTracking = true
                }) {
                    Text("▶ Start")
                }
            } else {
                Button(onClick = {
                    val intent = Intent(context, TrackerService::class.java)
                    context.stopService(intent)
                    statusTracking = false
                }) {
                    Text("⏹ Fim")
                }
            }
        }
    }
}