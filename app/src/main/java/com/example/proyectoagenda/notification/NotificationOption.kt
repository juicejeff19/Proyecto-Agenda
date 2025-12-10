package com.example.proyectoagenda.notification

enum class NotificationOption(val displayName: String, val minutesOffset: Long) {
    SIN_NOTIFICACION("Sin recordatorio", -1),
    A_TIEMPO("A la hora del evento", 0),
    DIEZ_MINUTOS("10 minutos antes", 10),
    UN_DIA("1 día antes", 1440) // 24 * 60
}