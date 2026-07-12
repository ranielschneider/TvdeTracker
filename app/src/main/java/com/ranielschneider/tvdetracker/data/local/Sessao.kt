package com.ranielschneider.tvdetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "sessoes")
data class Sessao(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val horaInicio: Long,
    val horaFim: Long? = null,
    val distanciaTotalMetros: Double = 0.0,
    val horasConduzidasMs: Long = 0L
)