package com.tfg.umeegunero.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferencias")
data class PreferenciasEntity(
    @PrimaryKey
    val id: String = "preferencias_globales",
    val sincronizacionAutomatica: Boolean = true,
    val intervaloSincronizacion: Long = 30 * 60 * 1000, // 30 minutos por defecto
    val ultimaSincronizacion: Long = 0,
    val modoOffline: Boolean = false
) 