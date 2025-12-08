package com.example.proyectoagenda.ui.create

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel // Cambiamos a AndroidViewModel para tener acceso al Context
import androidx.lifecycle.viewModelScope
import com.example.proyectoagenda.model.AgendaEvent
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventRepository
import com.example.proyectoagenda.model.EventStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// ... Tu data class CreateEventUiState se queda igual ...
data class CreateEventUiState(
    val selectedCategory: EventCategory = EventCategory.CITA,
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val description: String = "",
    val status: EventStatus = EventStatus.PENDIENTE,
    val location: String = "",
    val selectedContactName: String = "Alejandro",
    val availableContacts: List<String> = listOf("Alejandro", "Maria", "Carlos", "Sofia"),
    val latitude: Double? = null, // NUEVO
    val longitude: Double? = null // NUEVO
) {
    fun getFormattedDate(): String = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "dd/mm/aaaa"
    fun getFormattedTime(): String = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:-- -----"
}

// HEREDAMOS DE AndroidViewModel
class CreateEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CreateEventUiState())
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    // Inicializamos el repositorio pasando el contexto de la aplicación
    private val repository = EventRepository(application.applicationContext)

    // ... Tus funciones de cambio de estado (onCategorySelected, etc) se quedan igual ...
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
            Toast.makeText(getApplication(), "Faltan datos obligatorios", Toast.LENGTH_SHORT).show()
            return
        }

        val newEvent = AgendaEvent(
            category = state.selectedCategory,
            status = state.status,
            date = state.date.toString(),
            time = state.time.toString(),
            description = state.description,
            location = state.location,
            contact = state.selectedContactName,
            // --- AGREGAMOS ESTO ---
            latitude = state.latitude,
            longitude = state.longitude
        )

        viewModelScope.launch {
            repository.saveEvent(newEvent)
            Toast.makeText(getApplication(), "Evento Guardado", Toast.LENGTH_LONG).show()
            _uiState.update { CreateEventUiState() } // Limpiar formulario
        }
    }

    // NUEVA FUNCIÓN: Llamada cuando el mapa confirma la ubicación
    fun onLocationSelected(lat: Double, lon: Double) {
        _uiState.update {
            it.copy(
                latitude = lat,
                longitude = lon,
                // Guardamos en el string 'location' algo legible para el JSON actual
                location = "Lat: ${String.format("%.4f", lat)}, Lon: ${String.format("%.4f", lon)}"
            )
        }
    }
}