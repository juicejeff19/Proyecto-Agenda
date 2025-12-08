package com.example.proyectoagenda.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.proyectoagenda.model.AgendaEvent
import com.example.proyectoagenda.model.EventRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val events: List<AgendaEvent> = emptyList()
)

class HomeViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EventRepository(application.applicationContext)
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadEvents()
    }

    fun loadEvents() {
        viewModelScope.launch {
            // Cargamos todos los eventos y los invertimos para ver los últimos creados primero (opcional)
            val allEvents = repository.getAllEvents().sortedByDescending { it.id }
            _uiState.update { it.copy(events = allEvents) }
        }
    }
}