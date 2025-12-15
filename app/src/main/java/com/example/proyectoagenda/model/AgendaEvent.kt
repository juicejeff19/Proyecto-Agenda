package com.example.proyectoagenda.model

import kotlinx.serialization.Serializable
import com.example.proyectoagenda.notification.NotificationOption

@Serializable
data class AgendaEvent(
    val id: Long = System.currentTimeMillis(),
    val category: EventCategory,
    val status: EventStatus,
    val date: String,
    val time: String,
    val description: String,
    val location: String,
    val contact: String,
    val latitude: Double? = null,
    val longitude: Double? = null,
    val notificationOption: NotificationOption = NotificationOption.SIN_NOTIFICACION
)
