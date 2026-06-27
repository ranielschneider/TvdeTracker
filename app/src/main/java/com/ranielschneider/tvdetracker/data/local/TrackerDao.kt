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
}