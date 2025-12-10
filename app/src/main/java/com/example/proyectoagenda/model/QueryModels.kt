package com.example.proyectoagenda.model

import com.example.proyectoagenda.notification.NotificationOption

enum class QueryType(val displayName: String) {
    RANGO("Por rango"),
    ANIO("Por año"),
    DIA("Por día"),
    MES("Por mes")
}

// Un modelo simple para simular los resultados de la tabla
data class EventResult(
    val id: Long,
    val date: String,
    val time: String,
    val category: EventCategory,
    val status: EventStatus,
    val description: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notificationOption: NotificationOption = NotificationOption.SIN_NOTIFICACION
)