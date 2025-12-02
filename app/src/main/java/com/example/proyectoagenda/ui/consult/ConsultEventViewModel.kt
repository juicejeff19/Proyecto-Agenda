package com.example.proyectoagenda.ui.consult

import androidx.lifecycle.ViewModel
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventResult
import com.example.proyectoagenda.model.EventStatus
import com.example.proyectoagenda.model.QueryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ConsultUiState(
    val selectedQueryType: QueryType = QueryType.RANGO,
    val selectedCategory: EventCategory = EventCategory.CITA,

    // Campos dinámicos de fecha
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(7),
    val specificDate: LocalDate = LocalDate.now(), // Para "Por día"
    val year: String = "2025", // Para "Por año"

    val searchText: String = "",

    // Lista simulada de resultados
    val results: List<EventResult> = listOf(
        EventResult(1, "2018-05-11", "00:11", EventCategory.CITA, EventStatus.PENDIENTE, "Cita para comer"),
        EventResult(2, "2024-10-20", "14:30", EventCategory.JUNTA, EventStatus.EN_PROGRESO, "Revisión mensual"),
        EventResult(3, "2024-12-01", "09:00", EventCategory.EXAMEN, EventStatus.COMPLETADO, "Examen final Android")
    )
) {
    fun getFormattedStartDate(): String = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedEndDate(): String = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedSpecificDate(): String = specificDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

class ConsultEventViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ConsultUiState())
    val uiState: StateFlow<ConsultUiState> = _uiState.asStateFlow()

    fun onQueryTypeSelected(type: QueryType) {
        _uiState.update { it.copy(selectedQueryType = type) }
    }

    fun onCategorySelected(category: EventCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
    }

    fun onStartDateChanged(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun onEndDateChanged(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun onSpecificDateChanged(date: LocalDate) {
        _uiState.update { it.copy(specificDate = date) }
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
    }

    fun onConsultClicked() {
        // Aquí iría la lógica real de consulta a la base de datos (Room)
        println("Consultando con filtros: ${_uiState.value}")
    }
}