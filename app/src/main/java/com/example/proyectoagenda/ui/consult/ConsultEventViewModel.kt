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

// ... Tu ConsultUiState se queda igual ...
data class ConsultUiState(
    val selectedQueryType: QueryType = QueryType.RANGO,
    val selectedCategory: EventCategory = EventCategory.CITA,
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(7),
    val specificDate: LocalDate = LocalDate.now(),
    val year: String = "2025",
    val searchText: String = "",
    val results: List<EventResult> = emptyList() // Empieza vacía
) {
    fun getFormattedStartDate(): String = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedEndDate(): String = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedSpecificDate(): String = specificDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
}

class ConsultEventViewModel(application: Application) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ConsultUiState())
    val uiState: StateFlow<ConsultUiState> = _uiState.asStateFlow()

    private val repository = EventRepository(application.applicationContext)
    private var allEventsCache: List<AgendaEvent> = emptyList()

    // Cargar datos al iniciar
    init {
        loadEvents()
    }

    // Función pública para recargar datos (útil cuando regresas de crear un evento)
    fun loadEvents() {
        viewModelScope.launch {
            allEventsCache = repository.getAllEvents()
            applyFilters() // Aplicar filtros inmediatos
        }
    }

    // ... Setters de estado (onQueryTypeSelected, etc) ...
    // IMPORTANTE: Cada vez que cambiamos un filtro, llamamos a applyFilters()

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

    fun onSearchTextChanged(text: String) {
        _uiState.update { it.copy(searchText = text) }
        applyFilters()
    }

    // Botón consultar (re-aplica filtros explícitamente)
    fun onConsultClicked() {
        applyFilters()
    }

    // LÓGICA DE FILTRADO
    private fun applyFilters() {
        val state = _uiState.value

        val filtered = allEventsCache.filter { event ->
            // 1. Convertir fecha del evento (String) a LocalDate para comparar
            val eventDate = LocalDate.parse(event.date)

            // 2. Filtro por Tipo de Fecha
            val dateMatch = when (state.selectedQueryType) {
                QueryType.RANGO -> {
                    // eventDate >= startDate && eventDate <= endDate
                    !eventDate.isBefore(state.startDate) && !eventDate.isAfter(state.endDate)
                }
                QueryType.DIA -> {
                    eventDate.isEqual(state.specificDate)
                }
                QueryType.ANIO -> {
                    eventDate.year.toString() == state.year
                }
                else -> true // Mes u otros no implementados pasan todo
            }

            // 3. Filtro por Categoría
            val categoryMatch = event.category == state.selectedCategory

            // 4. Filtro por Búsqueda de Texto (en descripción)
            val searchMatch = if (state.searchText.isBlank()) true else {
                event.description.contains(state.searchText, ignoreCase = true)
            }

            dateMatch && categoryMatch && searchMatch
        }

        // 5. Mapear al modelo visual (EventResult)
        val uiResults = filtered.map { event ->
            EventResult(
                id = event.id.toInt(),
                date = LocalDate.parse(event.date).format(DateTimeFormatter.ofPattern("yyyy-MM-dd")),
                time = event.time,
                category = event.category,
                status = event.status,
                description = event.description
            )
        }

        _uiState.update { it.copy(results = uiResults) }
    }
}