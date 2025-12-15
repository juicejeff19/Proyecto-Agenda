package com.example.proyectoagenda.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectoagenda.model.AgendaEvent
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.ui.home.HomeViewModel
import java.time.LocalDate

// Colores
val YellowHeader = Color(0xFFFFD700)
val CardBackground = Color(0xFFFFFFFF)
val TodayBackground = Color(0xFFFFFDE7) // Amarillo muy pálido para eventos de hoy

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: HomeViewModel = viewModel(),
    onMenuClicked: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    // Recargar datos cada vez que entramos a Home
    LaunchedEffect(Unit) {
        viewModel.loadEvents()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Mis Eventos",
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YellowHeader)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color(0xFFF5F5F5)) // Fondo gris suave
                .padding(16.dp)
        ) {
            if (uiState.events.isEmpty()) {
                // Estado Vacío
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = Color.Gray,
                            modifier = Modifier.size(64.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("No hay eventos registrados", color = Color.Gray)
                    }
                }
            } else {
                // Lista de Tarjetas
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Ordenamos: Primero los de HOY, luego el resto
                    val sortedList = uiState.events.sortedBy { event ->
                        try {
                            val date = LocalDate.parse(event.date)
                            if (date.isEqual(LocalDate.now())) 0 else 1 // 0 va primero (Hoy)
                        } catch (e: Exception) { 1 }
                    }

                    items(sortedList) { event ->
                        EventCard(event)
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventCard(event: AgendaEvent) {
    // 1. Calculamos si es hoy
    val isToday = try {
        val eventDate = LocalDate.parse(event.date)
        eventDate.isEqual(LocalDate.now())
    } catch (e: Exception) {
        false
    }

    // 2. Definimos estilos basados en si es hoy o no
    val backgroundColor = if (isToday) TodayBackground else CardBackground
    val borderColor = if (isToday) YellowHeader else Color.Transparent
    val borderStroke = if (isToday) BorderStroke(2.dp, borderColor) else null

    Card(
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isToday) 6.dp else 2.dp), // Más sombra si es hoy
        shape = RoundedCornerShape(12.dp),
        border = borderStroke, // Borde amarillo si es hoy
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // Fila Superior: Categoría y Fecha
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                CategoryBadge(event.category)

                // Fecha y etiqueta HOY
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (isToday) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = Color(0xFFE67E5F), // Naranja llamativo
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "¡HOY!",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFFE67E5F)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        // Divisor vertical pequeño
                        Box(
                            modifier = Modifier
                                .height(12.dp)
                                .width(1.dp)
                                .background(Color.Gray)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Icon(Icons.Default.DateRange, null, modifier = Modifier.size(14.dp), tint = Color.Gray)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(event.date, fontSize = 12.sp, color = Color.Gray)
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Título / Descripción
            Text(
                text = event.description,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(8.dp))
            Divider(color = Color.LightGray.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(8.dp))

            // Fila Inferior
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                InfoChip(icon = Icons.Default.Schedule, text = event.time)

                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = event.status.displayName,
                    fontSize = 12.sp,
                    color = Color.Gray,
                    fontWeight = FontWeight.Medium
                )

                if (event.latitude != null) {
                    Spacer(modifier = Modifier.weight(1f))
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Con ubicación",
                        tint = if (isToday) Color(0xFFE67E5F) else Color(0xFF2196F3), // Color diferente si es hoy
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryBadge(category: EventCategory) {
    val color = when (category) {
        EventCategory.CITA -> Color(0xFF4CAF50)
        EventCategory.EXAMEN -> Color(0xFFF44336)
        EventCategory.ENTREGA -> Color(0xFFFF9800)
        else -> Color(0xFF2196F3)
    }

    Surface(
        color = color.copy(alpha = 0.1f),
        shape = RoundedCornerShape(4.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, color.copy(alpha = 0.5f))
    ) {
        Text(
            text = category.displayName.uppercase(),
            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InfoChip(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp), tint = Color.Gray)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text, fontSize = 12.sp, color = Color.Gray)
    }
}