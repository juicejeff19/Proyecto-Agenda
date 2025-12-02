package com.example.proyectoagenda.ui.create

import androidx.lifecycle.ViewModel
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Este data class representa TODO lo que se ve en la pantalla en un momento dado
data class CreateEventUiState(
    val selectedCategory: EventCategory = EventCategory.CITA, // Seleccionado por defecto
    val date: LocalDate? = null,
    val time: LocalTime? = null,
    val description: String = "",
    val status: EventStatus = EventStatus.PENDIENTE,
    val location: String = "",
    // Usamos strings simples para los spinners por ahora para simplificar la UI
    val selectedContactName: String = "Alejandro", // Valor default como en la imagen
    val availableContacts: List<String> = listOf("Alejandro", "Maria", "Carlos", "Sofia")
) {
    // Helpers para mostrar fechas y horas formateadas
    fun getFormattedDate(): String = date?.format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) ?: "dd/mm/aaaa"
    fun getFormattedTime(): String = time?.format(DateTimeFormatter.ofPattern("HH:mm")) ?: "--:-- -----"
}

class CreateEventViewModel : ViewModel() {

    // El estado mutable interno
    private val _uiState = MutableStateFlow(CreateEventUiState())
    // El estado inmutable expuesto a la UI
    val uiState: StateFlow<CreateEventUiState> = _uiState.asStateFlow()

    // Funciones para que la UI avise cuando algo cambia

    fun onCategorySelected(category: EventCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onDateSelected(date: LocalDate) {
        _uiState.update { it.copy(date = date) }
    }

    fun onTimeSelected(time: LocalTime) {
        _uiState.update { it.copy(time = time) }
    }

    fun onDescriptionChanged(text: String) {
        _uiState.update { it.copy(description = text) }
    }

    fun onStatusSelected(status: EventStatus) {
        _uiState.update { it.copy(status = status) }
    }

    fun onLocationChanged(text: String) {
        _uiState.update { it.copy(location = text) }
    }

    fun onContactSelected(contactName: String) {
        _uiState.update { it.copy(selectedContactName = contactName) }
    }

    fun onSaveClicked() {
        // Aquí irá la lógica para guardar en la Base de Datos Local más adelante.
        println("Guardando evento: ${_uiState.value}")
    }
}