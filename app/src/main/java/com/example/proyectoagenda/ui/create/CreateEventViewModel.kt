package com.example.proyectoagenda.ui.create

import android.annotation.SuppressLint
import android.app.Application
import android.provider.ContactsContract
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoagenda.model.AgendaEvent
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventRepository
import com.example.proyectoagenda.model.EventStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

data class CreateEventUiState(
    val selectedCategory: EventCategory = EventCategory.CITA,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val description: String = "",
    val status: EventStatus = EventStatus.PENDIENTE,
    val location: String = "",
    // Quitamos los hardcodeados por defecto
    val selectedContactName: String = "",
    val availableContacts: List<String> = emptyList(), // Lista vacía al inicio
    val latitude: Double? = null,
    val longitude: Double? = null
) {
    fun getFormattedDate(): String = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "dd/mm/aaaa"
    fun getFormattedTime(): String = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:-- -----"
}

class CreateEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    private val repository = EventRepository(application.applicationContext)
    private var currentEditingId: Long? = null

    // --- FUNCION NUEVA: Leer contactos del teléfono ---
    fun fetchDeviceContacts() {
        viewModelScope.launch(Dispatchers.IO) {
            val contacts = mutableListOf<String>()
            val context = getApplication<Application>().applicationContext

            // Query a la base de datos de contactos de Android
            val cursor = context.contentResolver.query(
                ContactsContract.Contacts.CONTENT_URI,
                arrayOf(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY),
                null, null,
                ContactsContract.Contacts.DISPLAY_NAME_PRIMARY + " ASC" // Orden alfabético
            )

            cursor?.use {
                val nameIndex = it.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME_PRIMARY)
                while (it.moveToNext()) {
                    if (nameIndex != -1) {
                        val name = it.getString(nameIndex)
                        if (!name.isNullOrBlank()) {
                            contacts.add(name)
                        }
                    }
                }
            }

            // Actualizamos la UI en el hilo principal
            withContext(Dispatchers.Main) {
                if (contacts.isNotEmpty()) {
                    _uiState.update {
                        it.copy(
                            availableContacts = contacts,
                            // Si no hay contacto seleccionado, seleccionamos el primero por defecto
                            selectedContactName = if (it.selectedContactName.isBlank()) contacts[0] else it.selectedContactName
                        )
                    }
                } else {
                    // Fallback si no hay contactos
                    _uiState.update {
                        it.copy(availableContacts = listOf("Sin contactos encontrados"))
                    }
                }
            }
        }
    }

    // ... Resto de funciones (onCategorySelected, onSave, etc.) IGUAL QUE ANTES ...

    fun onCategorySelected(category: EventCategory) { _uiState.update { it.copy(selectedCategory = category) } }
    fun onDateSelected(date: LocalDate) { _uiState.update { it.copy(date = date) } }
    fun onTimeSelected(time: LocalTime) { _uiState.update { it.copy(time = time) } }
    fun onDescriptionChanged(text: String) { _uiState.update { it.copy(description = text) } }
    fun onStatusSelected(status: EventStatus) { _uiState.update { it.copy(status = status) } }
    fun onLocationChanged(text: String) { _uiState.update { it.copy(location = text) } }
    fun onContactSelected(contactName: String) { _uiState.update { it.copy(selectedContactName = contactName) } }

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
            longitude = state.longitude
        )

        viewModelScope.launch {
            if (currentEditingId != null) {
                repository.updateEvent(eventToSave)
                Toast.makeText(getApplication(), "Evento Actualizado", Toast.LENGTH_SHORT).show()
            } else {
                repository.saveEvent(eventToSave)
                Toast.makeText(getApplication(), "Evento Creado", Toast.LENGTH_SHORT).show()
            }
            _uiState.update { CreateEventUiState() }
            currentEditingId = null
        }
    }

    fun clearForm() {
        _uiState.update { CreateEventUiState() }
        currentEditingId = null
    }

    fun onLocationSelected(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lon,
                location = "Lat: ${String.format("%.4f", lat)}, Lon: ${String.format("%.4f", lon)}"
            )
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
                        selectedContactName = event.contact
                    )
                }
            }
        }
    }
}