package com.example.proyectoagenda.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

// Modelo unificado para guardar en el JSON
data class AgendaEvent(
    val id: Long = System.currentTimeMillis(), // ID único basado en tiempo
    val category: EventCategory,
    val status: EventStatus,
    val date: String, // Formato "yyyy-MM-dd"
    val time: String, // Formato "HH:mm"
    val description: String,
    val location: String,
    val contact: String
)

class EventRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "agenda.json"

    // Guardar un evento
    fun saveEvent(event: AgendaEvent) {
        val events = getAllEvents().toMutableList()
        events.add(event)
        saveList(events)
    }

    // Obtener todos los eventos
    fun getAllEvents(): List<AgendaEvent> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()

        val jsonString = file.readText()
        val type = object : TypeToken<List<AgendaEvent>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }

    // Escribir la lista en el archivo
    private fun saveList(events: List<AgendaEvent>) {
        val jsonString = gson.toJson(events)
        val file = File(context.filesDir, fileName)
        file.writeText(jsonString)
    }
}