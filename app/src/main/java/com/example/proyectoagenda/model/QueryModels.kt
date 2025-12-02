package com.example.proyectoagenda.model

enum class QueryType(val displayName: String) {
    RANGO("Por rango"),
    ANIO("Por año"),
    DIA("Por día"),
    MES("Por mes")
}

// Un modelo simple para simular los resultados de la tabla
data class EventResult(
    val id: Int,
    val date: String,
    val time: String,
    val category: EventCategory,
    val status: EventStatus,
    val description: String
)