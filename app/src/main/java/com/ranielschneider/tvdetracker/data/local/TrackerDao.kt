package com.ranielschneider.tvdetracker.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface TrackerDao {

    @Insert
    suspend fun inserirSessao(sessao: Sessao): Long

    @Insert
    suspend fun inserirPontoGps(ponto: PontoGps): Unit

    @Query("SELECT * FROM sessoes WHERE id = :sessaoId")
    suspend fun buscarSessaoPorId(sessaoId: Long): Sessao?

    @Query("SELECT * FROM pontos_gps WHERE sessaoId = :sessaoId ORDER BY timestamp ASC")
    suspend fun buscarPontosDaSessao(sessaoId: Long): List<PontoGps>

    @Query("SELECT * FROM sessoes")
    suspend fun buscarTodasSessoes(): List<Sessao>

    @Query("UPDATE sessoes SET horaFim = :horaFim, distanciaTotalMetros = :distancia, horasConduzidasMs = :horasConduzidasMs WHERE id = :sessaoId")
    suspend fun fecharSessao(sessaoId: Long, horaFim: Long, distancia: Double, horasConduzidasMs: Long)

    @Insert
    suspend fun iniciarPausa(pausa: Pausa): Long

    @Query("UPDATE pausas SET fimPausa = :fimPausa WHERE id = :pausaId")
    suspend fun terminarPausa(pausaId: Long, fimPausa: Long)

    @Query("SELECT * FROM pausas WHERE sessaoId = :sessaoId")
    suspend fun buscarPausasDaSessao(sessaoId: Long): List<Pausa>

    @Query("SELECT * FROM pausas WHERE sessaoId = :sessaoId AND fimPausa IS NULL LIMIT 1")
    suspend fun buscarPausaAtiva(sessaoId: Long): Pausa?
}

