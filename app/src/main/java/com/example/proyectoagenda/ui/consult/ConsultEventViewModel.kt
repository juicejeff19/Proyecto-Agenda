package com.example.proyectoagenda.ui.consult

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoagenda.model.AgendaEvent
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventRepository
import com.example.proyectoagenda.model.EventResult
import com.example.proyectoagenda.model.QueryType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

data class ConsultUiState(
    val selectedQueryType: QueryType = QueryType.RANGO,
    val selectedCategory: EventCategory = EventCategory.CITA,

    // Filtros de Rango y Día
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(7),
    val specificDate: LocalDate = LocalDate.now(),

    // NUEVOS: Filtros para Mes y Año
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue, // 1 = Enero, 12 = Diciembre

    val searchText: String = "",
    val results: List<EventResult> = emptyList()
) {
    fun getFormattedStartDate(): String = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedEndDate(): String = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedSpecificDate(): String = specificDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

    // Helper para mostrar el nombre del mes
    fun getMonthName(): String {
        return java.time.Month.of(selectedMonth)
            .getDisplayName(java.time.format.TextStyle.FULL, java.util.Locale("es", "ES"))
            .replaceFirstChar { it.uppercase() }
    }
}

class ConsultEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ConsultUiState())
    val uiState: StateFlow<ConsultUiState> = _uiState.asStateFlow()

    private val repository = EventRepository(application.applicationContext)
    private var allEventsCache: List<AgendaEvent> = emptyList()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            allEventsCache = repository.getAllEvents()
            applyFilters()
        }
    }

    // --- FUNCIONES DE CAMBIO DE ESTADO ---

    fun onQueryTypeSelected(type: QueryType) {
        _uiState.update { it.copy(selectedQueryType = type) }
        applyFilters()
    }

    fun onCategorySelected(category: EventCategory) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    fun onStartDateChanged(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
    }

    fun onEndDateChanged(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
    }

    fun onSpecificDateChanged(date: LocalDate) {
        _uiState.update { it.copy(specificDate = date) }
        applyFilters()
    }

    // NUEVO: Cambiar Año (Flechas)
    fun onYearChange(increment: Int) {
        _uiState.update { it.copy(selectedYear = it.selectedYear + increment) }
        applyFilters()
    }

    // NUEVO: Cambiar Mes (Flechas)
    fun onMonthChange(increment: Int) {
        _uiState.update {
            var newMonth = it.selectedMonth + increment
            var newYear = it.selectedYear

            // Lógica para cambiar de año si pasamos de Diciembre a Enero o viceversa
            if (newMonth > 12) {
                newMonth = 1
                newYear += 1
            } else if (newMonth < 1) {
                newMonth = 12
                newYear -= 1
            }
            it.copy(selectedMonth = newMonth, selectedYear = newYear)
        }
        applyFilters()
    }

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        applyFilters()
    }

    fun onConsultClicked() {
        applyFilters()
    }

    fun onDeleteEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            loadEvents()
        }
    }

    // --- LÓGICA DE FILTRADO COMPLETA ---
    private fun applyFilters() {
        val state = _uiState.value

        val filtered = allEventsCache.filter { event ->
            try {
                // Parseamos la fecha del evento
                val eventDate = LocalDate.parse(event.date)

                // 1. Filtro por Tipo de Fecha
                val dateMatch = when (state.selectedQueryType) {
                    QueryType.RANGO -> {
                        !eventDate.isBefore(state.startDate) && !eventDate.isAfter(state.endDate)
                    }
                    QueryType.DIA -> {
                        eventDate.isEqual(state.specificDate)
                    }
                    QueryType.ANIO -> {
                        // Coincide solo el año
                        eventDate.year == state.selectedYear
                    }
                    QueryType.MES -> {
                        // Coincide mes Y año
                        eventDate.year == state.selectedYear && eventDate.monthValue == state.selectedMonth
                    }
                }

                // 2. Filtro por Categoría
                val categoryMatch = event.category == state.selectedCategory

                // 3. Filtro por Búsqueda
                val searchMatch = if (state.searchText.isBlank()) true else {
                    event.description.contains(state.searchText, ignoreCase = true)
                }

                dateMatch && categoryMatch && searchMatch
            } catch (e: Exception) {
                false // Si hay una fecha corrupta en el JSON, la ignoramos
            }
        }

        // Mapeo a UI
        val uiResults = filtered.map { event ->
            EventResult(
                id = event.id,
                date = event.date,
                time = event.time,
                category = event.category,
                status = event.status,
                description = event.description,
                latitude = event.latitude,
                longitude = event.longitude
            )
        }

        _uiState.update { it.copy(results = uiResults) }
    }
}