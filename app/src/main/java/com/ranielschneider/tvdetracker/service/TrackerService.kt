package com.ranielschneider.tvdetracker.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
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
    }

    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    private lateinit var fusedClient: com.google.android.gms.location.FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    private var sessaoId: Long = -1

    override fun onCreate() {
        super.onCreate()
        criarCanalDeNotificacao()
        fusedClient = LocationServices.getFusedLocationProviderClient(this)
    }

    @SuppressLint("MissingPermission")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val notificacao = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("TVDE Tracker")
            .setContentText("A registar o teu percurso...")
            .setSmallIcon(android.R.drawable.ic_menu_mylocation)
            .build()

        startForeground(NOTIFICATION_ID, notificacao)

        // Callback de GPS — definido antes de ser usado
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
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

        // Cria a sessão primeiro, só depois pede updates de GPS
        scope.launch {
            val db = TrackerDatabase.getDatabase(applicationContext)
            sessaoId = db.trackerDao().inserirSessao(
                Sessao(horaInicio = System.currentTimeMillis())
            )

            val locationRequest = LocationRequest.Builder(
                Priority.PRIORITY_HIGH_ACCURACY, 2000L
            )
                .setMinUpdateIntervalMillis(1000L)
                .setMinUpdateDistanceMeters(0f)
                .build()

            fusedClient.requestLocationUpdates(locationRequest, locationCallback, mainLooper)
        }

        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            fusedClient.removeLocationUpdates(locationCallback)
        }
        job.cancel()
        stopForeground(STOP_FOREGROUND_REMOVE)
    }

    override fun onBind(intent: Intent?): IBinder? = null

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