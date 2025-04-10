package com.tfg.umeegunero.data.repository

import android.content.ContentResolver
import android.content.ContentValues
import android.content.Context
import android.provider.CalendarContract
import com.tfg.umeegunero.data.model.EventoCalendario
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repositorio para gestionar eventos del calendario
 */
@Singleton
class CalendarRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val contentResolver: ContentResolver = context.contentResolver

    /**
     * Añade un evento al calendario del dispositivo
     */
    fun addEventoCalendario(evento: EventoCalendario): Long {
        val values = ContentValues().apply {
            put(CalendarContract.Events.TITLE, evento.titulo)
            put(CalendarContract.Events.DESCRIPTION, evento.descripcion)
            put(CalendarContract.Events.DTSTART, evento.fechaInicio.time)
            put(CalendarContract.Events.DTEND, evento.fechaFin.time)
            if (evento.ubicacion.isNotBlank()) {
                put(CalendarContract.Events.EVENT_LOCATION, evento.ubicacion)
            }
            put(CalendarContract.Events.CALENDAR_ID, 1) // Calendario principal
        }

        val uri = contentResolver.insert(CalendarContract.Events.CONTENT_URI, values)
        val eventoId = uri?.lastPathSegment?.toLongOrNull() ?: -1

        // Añadir recordatorio
        if (eventoId != -1L && evento.recordatorio > 0) {
            val reminderValues = ContentValues().apply {
                put(CalendarContract.Reminders.EVENT_ID, eventoId)
                put(CalendarContract.Reminders.METHOD, CalendarContract.Reminders.METHOD_ALERT)
                put(CalendarContract.Reminders.MINUTES, evento.recordatorio.toInt())
            }
            contentResolver.insert(CalendarContract.Reminders.CONTENT_URI, reminderValues)
        }

        return eventoId
    }
} 