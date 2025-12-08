package com.example.proyectoagenda.ui.consult

import android.app.DatePickerDialog
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventResult
import com.example.proyectoagenda.model.QueryType
import com.example.proyectoagenda.ui.create.ReadOnlyTextField
import java.time.LocalDate
import java.util.Calendar

// Colores específicos
val OrangeTab = Color(0xFFE67E5F)
val GreenTab = Color(0xFF4CAF50)
val YellowHeader = Color(0xFFFFD700)
val GrayTabUnselected = Color(0xFFF5F5F5)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ConsultEventScreen(
    viewModel: ConsultEventViewModel = viewModel(),
    onMenuClicked: () -> Unit = {},
    onEditEvent: (Long) -> Unit // Callback para editar
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Estado para controlar qué evento se muestra en el mapa
    var eventToShowOnMap by remember { mutableStateOf<EventResult?>(null) }

    // Helpers para date pickers
    val calendar = Calendar.getInstance()
    fun showDatePicker(onDateSelected: (LocalDate) -> Unit) {
        DatePickerDialog(
            context,
            { _, year, month, day -> onDateSelected(LocalDate.of(year, month + 1, day)) },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Consultas",
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
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .fillMaxSize()
        ) {
            // Sección superior scrolleable (Filtros)
            Column(modifier = Modifier.verticalScroll(scrollState).weight(1f, fill = false)) {

                // 1. Pestañas de Tipo de Consulta
                Text("Consulta:", fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(0.dp)
                ) {
                    QueryType.values().forEach { type ->
                        QueryTypeTab(
                            text = type.displayName,
                            isSelected = type == uiState.selectedQueryType,
                            onClick = { viewModel.onQueryTypeSelected(type) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 2. Pestañas de Categoría
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Categoria:", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
                    Row(
                        modifier = Modifier.horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        EventCategory.values().forEach { category ->
                            CategoryTabConsult(
                                text = category.displayName,
                                isSelected = category == uiState.selectedCategory,
                                onClick = { viewModel.onCategorySelected(category) }
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 3. Campos dinámicos según tipo de consulta
                when (uiState.selectedQueryType) {
                    QueryType.RANGO -> {
                        ReadOnlyTextField(
                            label = "Fecha Inicial",
                            value = uiState.getFormattedStartDate(),
                            onClick = { showDatePicker(viewModel::onStartDateChanged) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        ReadOnlyTextField(
                            label = "Fecha Final",
                            value = uiState.getFormattedEndDate(),
                            onClick = { showDatePicker(viewModel::onEndDateChanged) }
                        )
                    }
                    QueryType.DIA -> {
                        ReadOnlyTextField(
                            label = "Día",
                            value = uiState.getFormattedSpecificDate(),
                            onClick = { showDatePicker(viewModel::onSpecificDateChanged) }
                        )
                    }
                    else -> {
                        Text("Filtro específico no implementado visualmente aún", color = Color.Gray)
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))

                // 4. Botón Consultar
                Button(
                    onClick = viewModel::onConsultClicked,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF5F5F5)),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color.LightGray),
                    shape = RoundedCornerShape(4.dp)
                ) {
                    Text("Consultar", color = Color.Black, fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.height(16.dp))

                // 5. Barra de Búsqueda
                OutlinedTextField(
                    value = uiState.searchText,
                    onValueChange = viewModel::onSearchTextChanged,
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("") },
                    leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = Color.Gray) },
                    shape = RoundedCornerShape(24.dp),
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        unfocusedContainerColor = Color.White,
                        focusedContainerColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // 6. Tabla de Resultados (Lista fija abajo)
            Row(modifier = Modifier.fillMaxWidth().background(Color(0xFFFAFAFA)).padding(vertical = 8.dp)) {
                Text("Fecha", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                Text("Hora", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), fontSize = 12.sp)
                Text("Categoria", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                Text("Status", fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
                Text("Descripción", fontWeight = FontWeight.Bold, modifier = Modifier.weight(2f), fontSize = 12.sp)
            }
            Divider()

            // Lista scrollable de resultados
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(uiState.results) { event ->
                    ResultRow(
                        event = event,
                        onDeleteClick = { viewModel.onDeleteEvent(event.id) },
                        onMapClick = {
                            if (event.latitude != null && event.longitude != null) {
                                eventToShowOnMap = event
                            }
                        },
                        // CONECTAMOS LA ACCIÓN DE EDITAR
                        onEditClick = { onEditEvent(event.id) }
                    )
                    Divider(color = Color.LightGray, thickness = 0.5.dp)
                }
            }
        }
    }

    // --- DIÁLOGO DEL MAPA ---
    if (eventToShowOnMap != null) {
        Dialog(
            onDismissRequest = { eventToShowOnMap = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(modifier = Modifier.fillMaxSize().background(Color.White)) {
                EventMapViewer(
                    latitude = eventToShowOnMap!!.latitude!!,
                    longitude = eventToShowOnMap!!.longitude!!
                )

                IconButton(
                    onClick = { eventToShowOnMap = null },
                    modifier = Modifier.align(Alignment.TopEnd).padding(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar Mapa",
                        tint = Color.Black,
                        modifier = Modifier.background(Color.White.copy(alpha = 0.7f), RoundedCornerShape(50))
                    )
                }
            }
        }
    }
}

@Composable
fun ResultRow(
    event: EventResult,
    onDeleteClick: () -> Unit,
    onMapClick: () -> Unit,
    onEditClick: () -> Unit // Parámetro recibido
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = true }
                .padding(vertical = 12.dp, horizontal = 4.dp)
        ) {
            Text(event.date, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
            Text(event.time, modifier = Modifier.weight(1f), fontSize = 12.sp)
            Text(event.category.displayName, modifier = Modifier.weight(1.5f), fontSize = 12.sp)
            Text(event.status.displayName, modifier = Modifier.weight(1.5f), fontSize = 12.sp, color = Color.Gray)
            Text(event.description, modifier = Modifier.weight(2f), fontSize = 12.sp)
        }

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.background(Color.White)
        ) {
            // Ver Mapa
            if (event.latitude != null && event.longitude != null) {
                DropdownMenuItem(
                    text = { Text("Ver Ubicación") },
                    onClick = {
                        expanded = false
                        onMapClick()
                    },
                    leadingIcon = {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Blue)
                    }
                )
            }

            // Actualizar - AHORA CONECTADO
            DropdownMenuItem(
                text = { Text("Actualizar") },
                onClick = {
                    expanded = false
                    onEditClick() // Llamada al callback
                },
                leadingIcon = {
                    Icon(Icons.Default.Edit, contentDescription = null, tint = Color.Gray)
                }
            )

            Divider()

            // Borrar
            DropdownMenuItem(
                text = { Text("Borrar", color = Color.Red) },
                onClick = {
                    expanded = false
                    onDeleteClick()
                },
                leadingIcon = {
                    Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red)
                }
            )
        }
    }
}

@Composable
fun QueryTypeTab(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .background(if (isSelected) OrangeTab else Color.White)
            .border(0.5.dp, Color.LightGray)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 10.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp
        )
    }
}

@Composable
fun CategoryTabConsult(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(4.dp))
            .background(if (isSelected) GreenTab else Color.White)
            .border(0.5.dp, Color.LightGray, RoundedCornerShape(4.dp))
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 6.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}