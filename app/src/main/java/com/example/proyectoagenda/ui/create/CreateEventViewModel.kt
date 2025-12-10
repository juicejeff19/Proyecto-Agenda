package com.example.proyectoagenda.ui.create

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.provider.ContactsContract
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoagenda.model.AgendaEvent
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventRepository
import com.example.proyectoagenda.model.EventStatus
import com.example.proyectoagenda.notification.NotificationOption
import com.example.proyectoagenda.notification.NotificationReceiver
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter

data class CreateEventUiState(
    val selectedCategory: EventCategory = EventCategory.CITA,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val description: String = "",
    val status: EventStatus = EventStatus.PENDIENTE,
    val location: String = "",
    val selectedContactName: String = "",
    val availableContacts: List<String> = emptyList(),
    val latitude: Double? = null,
    val longitude: Double? = null,
    // NUEVO: Estado para el dropdown de notificación
    val notificationOption: NotificationOption = NotificationOption.SIN_NOTIFICACION
) {
    fun getFormattedDate(): String = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "dd/mm/aaaa"
    fun getFormattedTime(): String = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:-- -----"
}

class CreateEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    private val repository = EventRepository(application.applicationContext)
    private var currentEditingId: Long? = null

    // --- LOGICA DE NOTIFICACIONES ---
    private fun scheduleNotification(event: AgendaEvent) {
        // 1. Si eligió "Sin notificación", no hacemos nada (o cancelamos si existía, pero por simplicidad lo dejamos así)
        if (event.notificationOption == NotificationOption.SIN_NOTIFICACION) return

        val context = getApplication<Application>().applicationContext
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        // 2. Calcular el momento exacto del evento
        val eventDateTime = LocalDateTime.parse("${event.date}T${event.time}") // Formato ISO simple

        // 3. Restar el tiempo seleccionado (10 min, 1 dia, etc)
        val triggerTime = eventDateTime.minusMinutes(event.notificationOption.minutesOffset)

        // Convertir a milisegundos
        val triggerMillis = triggerTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

        // 4. Si la fecha ya pasó, no programamos nada
        if (triggerMillis < System.currentTimeMillis()) return

        // 5. Crear el Intent para el BroadcastReceiver
        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("EVENT_ID", event.id)
            putExtra("EVENT_TITLE", event.category.displayName)
            putExtra("EVENT_DESC", event.description)
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            event.id.toInt(), // ID único para que no se sobrescriban alarmas de otros eventos
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // 6. Programar la alarma
        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                triggerMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Manejo básico si falta permiso exacto en Android 12+
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerMillis, pendingIntent)
        }
    }

    // --- FUNCIONES EXISTENTES ---

    // Nueva función para actualizar la opción de notificación
    fun onNotificationOptionSelected(option: NotificationOption) {
        _uiState.update { it.copy(notificationOption = option) }
    }

    fun onCategorySelected(category: EventCategory) { _uiState.update { it.copy(selectedCategory = category) } }
    fun onDateSelected(date: LocalDate) { _uiState.update { it.copy(date = date) } }
    fun onTimeSelected(time: LocalTime) { _uiState.update { it.copy(time = time) } }
    fun onDescriptionChanged(text: String) { _uiState.update { it.copy(description = text) } }
    fun onStatusSelected(status: EventStatus) { _uiState.update { it.copy(status = status) } }
    fun onLocationChanged(text: String) { _uiState.update { it.copy(location = text) } }
    fun onContactSelected(contactName: String) { _uiState.update { it.copy(selectedContactName = contactName) } }

    fun onLocationSelected(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lon,
                location = "Lat: ${String.format("%.4f", lat)}, Lon: ${String.format("%.4f", lon)}"
            )
        }
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (state.date == null || state.time == null || state.description.isBlank()) {
            Toast.makeText(getApplication(), "Faltan datos", Toast.LENGTH_SHORT).show()
            return
        }

        val eventIdToSave = currentEditingId ?: System.currentTimeMillis()

        val eventToSave = AgendaEvent(
            id = eventIdToSave,
            category = state.selectedCategory,
            status = state.status,
            date = state.date.toString(),
            time = state.time.toString(),
            description = state.description,
            location = state.location,
            contact = state.selectedContactName,
            latitude = state.latitude,
            longitude = state.longitude,
            // Guardamos la opción seleccionada
            notificationOption = state.notificationOption
        )

        viewModelScope.launch {
            if (currentEditingId != null) {
                repository.updateEvent(eventToSave)
                // Reprogramamos la notificación al actualizar
                scheduleNotification(eventToSave)
                Toast.makeText(getApplication(), "Evento Actualizado", Toast.LENGTH_SHORT).show()
            } else {
                repository.saveEvent(eventToSave)
                // Programamos la notificación al crear
                scheduleNotification(eventToSave)
                Toast.makeText(getApplication(), "Evento Creado", Toast.LENGTH_SHORT).show()
            }
            _uiState.update { CreateEventUiState() }
            currentEditingId = null
        }
    }

    fun fetchDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = mutableListOf<String>()
            val context = getApplication<Application>().applicationContext
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
                null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC"
            )
            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                while (it.moveToNext()) {
                    if (nameIndex != -1) {
                        val name = it.getString(nameIndex)
                        if (!name.isNullOrBlank()) contacts.add(name)
                    }
                }
            }
            withContext(Dispatchers.Main) {
                if (contacts.isNotEmpty()) {
                    _uiState.update {
                        it.copy(availableContacts = contacts, selectedContactName = if (it.selectedContactName.isBlank()) contacts[0] else it.selectedContactName)
                    }
                } else {
                    _uiState.update { it.copy(availableContacts = listOf("Sin contactos encontrados")) }
                }
            }
        }
    }

    fun loadEventForEdit(eventId: Long) {
        viewModelScope.launch {
            val event = repository.getEventById(eventId)
            if (event != null) {
                currentEditingId = event.id
                _uiState.update {
                    it.copy(
                        selectedCategory = event.category,
                        status = event.status,
                        date = try { LocalDate.parse(event.date) } catch (e: Exception) { null },
                        time = try { LocalTime.parse(event.time) } catch (e: Exception) { null },
                        description = event.description,
                        location = event.location,
                        latitude = event.latitude,
                        longitude = event.longitude,
                        selectedContactName = event.contact,
                        notificationOption = event.notificationOption
                    )
                }
            }
        }
    }

    fun clearForm() {
        _uiState.update { CreateEventUiState() }
        currentEditingId = null
    }
}