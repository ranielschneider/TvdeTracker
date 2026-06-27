package com.ranielschneider.tvdetracker.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.ForeignKey

@Entity(
    tableName = "pontos_gps",
    foreignKeys = [
        ForeignKey(
            entity = Sessao::class,
            parentColumns = ["id"],
            childColumns = ["sessaoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PontoGps(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val latitude: Double,
    val longitude: Double,
    val timestamp: Long,
    val sessaoId: Long
)