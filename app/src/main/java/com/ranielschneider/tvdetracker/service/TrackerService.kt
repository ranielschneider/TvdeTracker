package com.ranielschneider.tvdetracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.ranielschneider.tvdetracker.data.local.Pausa
import com.ranielschneider.tvdetracker.data.local.PontoGps
import com.ranielschneider.tvdetracker.data.local.Sessao
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class TrackerService : Service() {

    companion object {
        const val CHANNEL_ID = "tracker_channel"
        const val NOTIFICATION_ID = 1
        const val TAG = "TrackerService"

        const val ACAO_START = "START"
        const val ACAO_PAUSE = "PAUSE"
        const val ACAO_RESUME = "RESUME"
        const val ACAO_STOP = "STOP"
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var sessaoId: Long = -1
    private var pausaAtualId: Long = -1
    private var emPausa: Boolean = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "onCreate chamado")
        criarCanalDeNotificacao()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val acao = intent?.action ?: ACAO_START
        Log.d(TAG, "onStartCommand: acao=$acao")

        when (acao) {
            ACAO_START -> iniciarTracking()
            ACAO_PAUSE -> pausarTracking()
            ACAO_RESUME -> retomarTracking()
            ACAO_STOP -> pararTracking()
        }

        return START_STICKY
    }

    @SuppressLint("MissingPermission")
    private fun iniciarTracking() {
        atualizarNotificacao("A registar o teu percurso...")

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                if (emPausa) return
                val location = result.lastLocation ?: return
                scope.launch {
                    if (sessaoId == -1L) return@launch
                    val db = TrackerDatabase.getDatabase(applicationContext)
                    db.trackerDao().inserirPontoGps(
                        PontoGps(
                            latitude = location.latitude,
                            longitude = location.longitude,
                            timestamp = System.currentTimeMillis(),
                            sessaoId = sessaoId
                        )
                    )
                }
            }
        }

        scope.launch {
            val db = TrackerDatabase.getDatabase(applicationContext)
            sessaoId = db.trackerDao().inserirSessao(
                Sessao(horaInicio = System.currentTimeMillis())
            )
            Log.d(TAG, "Sessão criada com id=$sessaoId")

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000L
            )
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(0f)
                .build()

            fusedClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }
    }

    private fun pausarTracking() {
        emPausa = true
        atualizarNotificacao("Em pausa...")
        scope.launch {
            val db = TrackerDatabase.getDatabase(applicationContext)
            pausaAtualId = db.trackerDao().iniciarPausa(
                Pausa(
                    sessaoId = sessaoId,
                    inicioPausa = System.currentTimeMillis()
                )
            )
            Log.d(TAG, "Pausa iniciada com id=$pausaAtualId")
        }
    }

    private fun retomarTracking() {
        emPausa = false
        atualizarNotificacao("A registar o teu percurso...")
        scope.launch {
            if (pausaAtualId != -1L) {
                val db = TrackerDatabase.getDatabase(applicationContext)
                db.trackerDao().terminarPausa(
                    pausaId = pausaAtualId,
                    fimPausa = System.currentTimeMillis()
                )
                Log.d(TAG, "Pausa $pausaAtualId terminada")
                pausaAtualId = -1
            }
        }
    }

    private fun pararTracking() {
        if (::locationCallback.isInitialized) {
            fusedClient.removeLocationUpdates(locationCallback)
        }
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy chamado")
        if (::locationCallback.isInitialized) {
            fusedClient.removeLocationUpdates(locationCallback)
        }
        job.cancel()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    private fun atualizarNotificacao(texto: String) {
        val notificacao = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TVDE Tracker")
            .setContentText(texto)
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()
        startForeground(NOTIFICATION_ID, notificacao)
    }

    private fun criarCanalDeNotificacao() {
        val canal = NotificationChannel(
            CHANNEL_ID,
            "Tracker de Percurso",
            NotificationManager.IMPORTANCE_LOW
        )
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(canal)
    }
}