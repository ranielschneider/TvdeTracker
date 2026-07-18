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
import com.ranielschneider.tvdetracker.data.local.TrackerDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

class TrackerService : Service() {

    companion object {
        const val CHANNEL_ID = "tracker_channel"
        const val NOTIFICATION_ID = 1
        const val TAG = "TrackerService"

        const val ACAO_START = "START"
        const val ACAO_PAUSE = "PAUSE"
        const val ACAO_RESUME = "RESUME"
        const val ACAO_STOP = "STOP"

        private const val ACAO_RESTORE = "RESTORE"
    }

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(
        Dispatchers.IO + serviceJob
    )

    /*
     * Impede que START, PAUSE, RESUME e STOP sejam processados
     * simultaneamente em threads diferentes.
     */
    private val actionMutex = Mutex()

    private lateinit var fusedClient:
            com.google.android.gms.location.FusedLocationProviderClient

    private lateinit var locationCallback: LocationCallback

    private var sessaoId: Long = -1L
    private var pausaAtualId: Long = -1L

    private var emPausa: Boolean = false
    private var recebendoLocalizacoes: Boolean = false

    override fun onCreate() {
        super.onCreate()

        Log.d(TAG, "onCreate chamado")

        criarCanalDeNotificacao()

        fusedClient = LocationServices
            .getFusedLocationProviderClient(this)

        criarLocationCallback()
    }

    override fun onStartCommand(
        intent: Intent?,
        flags: Int,
        startId: Int
    ): Int {
        /*
         * Quando o Android recria um serviço START_STICKY,
         * o Intent pode chegar nulo.
         */
        val acao = intent?.action ?: ACAO_RESTORE

        Log.d(TAG, "onStartCommand: acao=$acao")

        serviceScope.launch {
            actionMutex.withLock {
                when (acao) {
                    ACAO_START -> iniciarTracking()
                    ACAO_PAUSE -> pausarTracking()
                    ACAO_RESUME -> retomarTracking()
                    ACAO_STOP -> pararTracking()
                    ACAO_RESTORE -> restaurarTracking()
                }
            }
        }

        return START_STICKY
    }

    private fun criarLocationCallback() {
        locationCallback = object : LocationCallback() {

            override fun onLocationResult(
                result: LocationResult
            ) {
                if (emPausa || sessaoId == -1L) {
                    return
                }

                val location = result.lastLocation ?: return

                serviceScope.launch {
                    val sessionIdSnapshot = sessaoId

                    if (sessionIdSnapshot == -1L || emPausa) {
                        return@launch
                    }

                    runCatching {
                        val dao = TrackerDatabase
                            .getDatabase(applicationContext)
                            .trackerDao()

                        dao.inserirPontoGps(
                            PontoGps(
                                latitude = location.latitude,
                                longitude = location.longitude,
                                timestamp = System.currentTimeMillis(),
                                sessaoId = sessionIdSnapshot
                            )
                        )
                    }.onFailure { error ->
                        Log.e(
                            TAG,
                            "Erro ao guardar ponto GPS",
                            error
                        )
                    }
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private suspend fun iniciarTracking() {
        val dao = TrackerDatabase
            .getDatabase(applicationContext)
            .trackerDao()

        /*
         * Evita criar uma segunda sessão caso já exista
         * uma jornada aberta no banco.
         */
        val sessaoAtiva = dao
            .buscarTodasSessoes()
            .filter { it.horaFim == null }
            .maxByOrNull { it.horaInicio }

        sessaoId = sessaoAtiva?.id
            ?: dao.inserirSessao(
                com.ranielschneider.tvdetracker.data.local.Sessao(
                    horaInicio = System.currentTimeMillis()
                )
            )

        val pausaAtiva = dao.buscarPausaAtiva(sessaoId)

        pausaAtualId = pausaAtiva?.id ?: -1L
        emPausa = pausaAtiva != null

        Log.d(
            TAG,
            "Sessão ativa restaurada/criada: id=$sessaoId"
        )

        atualizarNotificacao(
            if (emPausa) {
                "Em pausa..."
            } else {
                "A registar o teu percurso..."
            }
        )

        iniciarAtualizacoesDeLocalizacao()
    }

    private suspend fun pausarTracking() {
        val dao = TrackerDatabase
            .getDatabase(applicationContext)
            .trackerDao()

        recuperarSessaoAtivaSeNecessario()

        if (sessaoId == -1L) {
            Log.w(
                TAG,
                "PAUSE ignorado: não existe sessão ativa"
            )
            return
        }

        val pausaExistente = dao.buscarPausaAtiva(sessaoId)

        if (pausaExistente != null) {
            pausaAtualId = pausaExistente.id
            emPausa = true

            atualizarNotificacao("Em pausa...")

            Log.d(
                TAG,
                "Sessão já estava em pausa: id=${pausaExistente.id}"
            )

            return
        }

        pausaAtualId = dao.iniciarPausa(
            Pausa(
                sessaoId = sessaoId,
                inicioPausa = System.currentTimeMillis()
            )
        )

        emPausa = true

        atualizarNotificacao("Em pausa...")

        Log.d(
            TAG,
            "Pausa iniciada: id=$pausaAtualId, sessaoId=$sessaoId"
        )
    }

    private suspend fun retomarTracking() {
        val dao = TrackerDatabase
            .getDatabase(applicationContext)
            .trackerDao()

        recuperarSessaoAtivaSeNecessario()

        if (sessaoId == -1L) {
            Log.w(
                TAG,
                "RESUME ignorado: não existe sessão ativa"
            )
            return
        }

        val pausaAtiva = when {
            pausaAtualId != -1L -> {
                dao.buscarPausaAtiva(sessaoId)
            }

            else -> {
                dao.buscarPausaAtiva(sessaoId)
            }
        }

        if (pausaAtiva != null) {
            dao.terminarPausa(
                pausaId = pausaAtiva.id,
                fimPausa = System.currentTimeMillis()
            )

            Log.d(
                TAG,
                "Pausa terminada: id=${pausaAtiva.id}"
            )
        }

        pausaAtualId = -1L
        emPausa = false

        atualizarNotificacao(
            "A registar o teu percurso..."
        )

        iniciarAtualizacoesDeLocalizacao()
    }

    private suspend fun restaurarTracking() {
        val dao = TrackerDatabase
            .getDatabase(applicationContext)
            .trackerDao()

        val sessaoAtiva = dao
            .buscarTodasSessoes()
            .filter { it.horaFim == null }
            .maxByOrNull { it.horaInicio }

        if (sessaoAtiva == null) {
            Log.d(
                TAG,
                "Nenhuma sessão ativa para restaurar"
            )

            stopSelf()
            return
        }

        sessaoId = sessaoAtiva.id

        val pausaAtiva = dao.buscarPausaAtiva(sessaoId)

        pausaAtualId = pausaAtiva?.id ?: -1L
        emPausa = pausaAtiva != null

        atualizarNotificacao(
            if (emPausa) {
                "Em pausa..."
            } else {
                "A registar o teu percurso..."
            }
        )

        iniciarAtualizacoesDeLocalizacao()

        Log.d(
            TAG,
            "Tracking restaurado: sessaoId=$sessaoId, emPausa=$emPausa"
        )
    }

    private suspend fun recuperarSessaoAtivaSeNecessario() {
        if (sessaoId != -1L) {
            return
        }

        val dao = TrackerDatabase
            .getDatabase(applicationContext)
            .trackerDao()

        val sessaoAtiva = dao
            .buscarTodasSessoes()
            .filter { it.horaFim == null }
            .maxByOrNull { it.horaInicio }

        if (sessaoAtiva != null) {
            sessaoId = sessaoAtiva.id

            val pausaAtiva = dao.buscarPausaAtiva(
                sessaoAtiva.id
            )

            pausaAtualId = pausaAtiva?.id ?: -1L
            emPausa = pausaAtiva != null

            Log.d(
                TAG,
                "Sessão recuperada do banco: id=$sessaoId"
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun iniciarAtualizacoesDeLocalizacao() {
        if (recebendoLocalizacoes) {
            return
        }

        val locationRequest = LocationRequest.Builder(
            Priority.PRIORITY_HIGH_ACCURACY,
            2_000L
        )
            .setMinUpdateIntervalMillis(1_000L)
            .setMinUpdateDistanceMeters(0f)
            .build()

        fusedClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            mainLooper
        )

        recebendoLocalizacoes = true

        Log.d(
            TAG,
            "Atualizações de localização iniciadas"
        )
    }

    private fun pararTracking() {
        if (recebendoLocalizacoes) {
            fusedClient.removeLocationUpdates(
                locationCallback
            )

            recebendoLocalizacoes = false
        }

        emPausa = false
        pausaAtualId = -1L
        sessaoId = -1L

        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()

        Log.d(TAG, "Tracking parado")
    }

    override fun onDestroy() {
        Log.d(TAG, "onDestroy chamado")

        if (
            ::locationCallback.isInitialized &&
            recebendoLocalizacoes
        ) {
            fusedClient.removeLocationUpdates(
                locationCallback
            )
        }

        recebendoLocalizacoes = false

        serviceScope.cancel()

        super.onDestroy()
    }

    override fun onBind(
        intent: Intent?
    ): IBinder? = null

    private fun atualizarNotificacao(
        texto: String
    ) {
        val notificacao = NotificationCompat.Builder(
            this,
            CHANNEL_ID
        )
            .setContentTitle("TVDE Tracker")
            .setContentText(texto)
            .setSmallIcon(
                android.R.drawable.ic_menu_mylocation
            )
            .setOngoing(true)
            .build()

        startForeground(
            NOTIFICATION_ID,
            notificacao
        )
    }

    private fun criarCanalDeNotificacao() {
        val canal = NotificationChannel(
            CHANNEL_ID,
            "Tracker de Percurso",
            NotificationManager.IMPORTANCE_LOW
        )

        val manager = getSystemService(
            NotificationManager::class.java
        )

        manager.createNotificationChannel(canal)
    }
}