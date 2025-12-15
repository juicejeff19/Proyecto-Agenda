package com.example.proyectoagenda.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
data class AgendaBackup(
    val events: List<AgendaEvent>
)

fun exportEventsAsJson(repo: EventRepository): String {
    val all = repo.getAllEvents()
    val backup = AgendaBackup(all)
    return Json.encodeToString(backup)
}

suspend fun importEventsFromJson(repo: EventRepository, json: String) {
    val backup = Json.decodeFromString<AgendaBackup>(json)

    repo.clearAllEvents()

    for (event in backup.events) {
        repo.insertEvent(event)
    }
}