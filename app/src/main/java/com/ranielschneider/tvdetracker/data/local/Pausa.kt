package com.ranielschneider.tvdetracker.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "pausas",
    foreignKeys = [
        ForeignKey(
            entity = Sessao::class,
            parentColumns = ["id"],
            childColumns = ["sessaoId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Pausa(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sessaoId: Long,
    val inicioPausa: Long,
    val fimPausa: Long? = null
)