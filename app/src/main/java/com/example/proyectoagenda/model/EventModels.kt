package com.example.proyectoagenda.model

// Representa las opciones de las pestañas superiores
enum class EventCategory(val displayName: String) {
    CITA("Cita"),
    JUNTA("Junta"),
    ENTREGA("Entrega de Proyecto"),
    EXAMEN("Examen"),
    OTRO("Otro")
}

// Representa las opciones del Spinner de Status
enum class EventStatus(val displayName: String) {
    PENDIENTE("Pendiente"),
    EN_PROGRESO("En Progreso"),
    COMPLETADO("Completado"),
    CANCELADO("Cancelado")
}

// (Opcional por ahora) Podrías tener un modelo para Contactos más adelante
data class Contact(val id: Int, val name: String)