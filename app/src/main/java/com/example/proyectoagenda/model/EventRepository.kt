package com.example.proyectoagenda.model

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

class EventRepository(private val context: Context) {

    private val gson = Gson()
    private val fileName = "agenda.json"

    fun insertEvent(event: AgendaEvent) {
        val events = getAllEvents().toMutableList()
        events.add(event)
        saveList(events)
    }

    fun clearAllEvents() {
        val file = File(context.filesDir, fileName)
        file.writeText("[]") // lista vacía
    }

    fun getAllEvents(): List<AgendaEvent> {
        val file = File(context.filesDir, fileName)
        if (!file.exists()) return emptyList()

        val jsonString = file.readText()
        val type = object : TypeToken<List<AgendaEvent>>() {}.type
        return gson.fromJson(jsonString, type) ?: emptyList()
    }

    private fun saveList(events: List<AgendaEvent>) {
        val jsonString = gson.toJson(events)
        val file = File(context.filesDir, fileName)
        file.writeText(jsonString)
    }

    fun deleteEvent(eventId: Long) {
        val events = getAllEvents().toMutableList()
        events.removeIf { it.id == eventId }
        saveList(events)
    }

    fun getEventById(id: Long): AgendaEvent? =
        getAllEvents().find { it.id == id }

    fun updateEvent(updated: AgendaEvent) {
        val list = getAllEvents().toMutableList()
        val index = list.indexOfFirst { it.id == updated.id }
        if (index != -1) {
            list[index] = updated
            saveList(list)
        }
    }
}