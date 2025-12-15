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

    // CAMBIO 1: Ahora es nullable (EventCategory?) y por defecto null (significa TODOS)
    val selectedCategory: EventCategory? = null,

    // Filtros de Rango y Día
    val startDate: LocalDate = LocalDate.now(),
    val endDate: LocalDate = LocalDate.now().plusDays(7),
    val specificDate: LocalDate = LocalDate.now(),

    // Filtros para Mes y Año
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,

    val searchText: String = "",
    val results: List<EventResult> = emptyList()
) {
    fun getFormattedStartDate(): String = startDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedEndDate(): String = endDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
    fun getFormattedSpecificDate(): String = specificDate.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))

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

    fun onCategorySelected(category: EventCategory?) {
        _uiState.update { it.copy(selectedCategory = category) }
        applyFilters()
    }

    // ⭐ CORRECCIÓN PARA FILTRO POR RANGO (START DATE) ⭐
    fun onStartDateChanged(date: LocalDate) {
        _uiState.update { it.copy(startDate = date) }
        applyFilters() // <--- AGREGADO: Aplica el filtro inmediatamente.
    }

    // ⭐ CORRECCIÓN PARA FILTRO POR RANGO (END DATE) ⭐
    fun onEndDateChanged(date: LocalDate) {
        _uiState.update { it.copy(endDate = date) }
        applyFilters() // <--- AGREGADO: Aplica el filtro inmediatamente.
    }

    // El filtro por día ya estaba correcto, pero lo confirmamos:
    fun onSpecificDateChanged(date: LocalDate) {
        _uiState.update { it.copy(specificDate = date) }
        applyFilters() // <--- YA EXISTENTE
    }

    fun onYearChange(increment: Int) {
        _uiState.update { it.copy(selectedYear = it.selectedYear + increment) }
        applyFilters()
    }

    fun onMonthChange(increment: Int) {
        _uiState.update {
            var newMonth = it.selectedMonth + increment
            var newYear = it.selectedYear
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

    // Dejamos onConsultClicked() para que el usuario pueda forzar la consulta.
    fun onConsultClicked() {
        applyFilters()
    }

    fun onDeleteEvent(eventId: Long) {
        viewModelScope.launch {
            repository.deleteEvent(eventId)
            loadEvents()
        }
    }

    // --- LÓGICA DE FILTRADO (Sin cambios, pero clave para que las correcciones funcionen) ---
    private fun applyFilters() {
        val state = _uiState.value

        val filtered = allEventsCache.filter { event ->
            try {
                val eventDate = LocalDate.parse(event.date)

                // 1. Filtro por Fecha
                val dateMatch = when (state.selectedQueryType) {
                    // FILTRO POR RANGO: Comprueba que la fecha del evento esté entre startDate y endDate (ambos inclusive)
                    QueryType.RANGO -> !eventDate.isBefore(state.startDate) && !eventDate.isAfter(state.endDate)
                    // FILTRO POR DÍA: Comprueba que la fecha del evento sea IGUAL a specificDate
                    QueryType.DIA -> eventDate.isEqual(state.specificDate)
                    QueryType.ANIO -> eventDate.year == state.selectedYear
                    QueryType.MES -> eventDate.year == state.selectedYear && eventDate.monthValue == state.selectedMonth
                }

                // 2. Filtro por Categoría
                val categoryMatch = if (state.selectedCategory == null) {
                    true
                } else {
                    event.category == state.selectedCategory
                }

                // 3. Filtro por Búsqueda
                val searchMatch = if (state.searchText.isBlank()) true else {
                    event.description.contains(state.searchText, ignoreCase = true)
                }

                dateMatch && categoryMatch && searchMatch
            } catch (e: Exception) {
                false
            }
        }

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
        }.sortedBy { it.date } // Opcional: ordenar por fecha para mejor visualización

        _uiState.update { it.copy(results = uiResults) }
    }
}