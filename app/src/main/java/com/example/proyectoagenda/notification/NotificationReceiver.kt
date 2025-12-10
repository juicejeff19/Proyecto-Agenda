package com.example.proyectoagenda.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.proyectoagenda.R // Asegúrate de tener un icono, o usa android.R.drawable.ic_dialog_info

class NotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val eventId = intent.getLongExtra("EVENT_ID", -1)
        val title = intent.getStringExtra("EVENT_TITLE") ?: "Evento"
        val desc = intent.getStringExtra("EVENT_DESC") ?: "Tienes un evento pendiente"

        showNotification(context, eventId, title, desc)
    }

    private fun showNotification(context: Context, id: Long, title: String, desc: String) {
        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channelId = "agenda_channel_id"

        // Crear canal (Necesario en Android 8+)
        if (Build.VERSION_CODES.O <= Build.VERSION.SDK_INT) {
            val channel = NotificationChannel(
                channelId,
                "Recordatorios de Agenda",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setContentTitle(title)
            .setContentText(desc)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar) // Icono por defecto de Android
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        manager.notify(id.toInt(), notification)
    }
}