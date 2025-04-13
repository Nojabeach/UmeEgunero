package com.tfg.umeegunero.data.local.dao

import androidx.room.*
import com.tfg.umeegunero.data.local.entity.PreferenciasEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenciasDao {
    @Query("SELECT * FROM preferencias WHERE id = :id")
    fun getPreferencias(id: String = "preferencias_globales"): Flow<PreferenciasEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferencias(preferencias: PreferenciasEntity)

    @Update
    suspend fun updatePreferencias(preferencias: PreferenciasEntity)

    @Query("UPDATE preferencias SET ultimaSincronizacion = :timestamp WHERE id = :id")
    suspend fun actualizarUltimaSincronizacion(timestamp: Long, id: String = "preferencias_globales")

    @Query("UPDATE preferencias SET modoOffline = :modoOffline WHERE id = :id")
    suspend fun actualizarModoOffline(modoOffline: Boolean, id: String = "preferencias_globales")
} 