package com.tuempresa.proyectoagenda.ui.create

import android.Manifest
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
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
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.proyectoagenda.model.EventCategory
import com.example.proyectoagenda.model.EventStatus
import com.example.proyectoagenda.ui.create.CreateEventViewModel
import com.example.proyectoagenda.ui.create.LocationPickerMap
import java.time.LocalDate
import java.time.LocalTime
import java.util.Calendar

// Colores personalizados
val YellowPrimary = Color(0xFFFFD700)
val BlueHighlight = Color(0xFF2196F3)
val GrayBackground = Color(0xFFF5F5F5)
val GrayUnselected = Color(0xFFE0E0E0)

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CreateEventScreen(
    viewModel: CreateEventViewModel = viewModel(),
    onMenuClicked: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    val scrollState = rememberScrollState()
    val context = LocalContext.current
    var showMap by remember { mutableStateOf(false) }

    // --- LÓGICA DE PERMISOS PARA CONTACTOS ---
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            viewModel.fetchDeviceContacts()
        }
    }

    // Al iniciar la pantalla, verificamos si tenemos permiso
    LaunchedEffect(Unit) {
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CONTACTS) -> {
                // Ya tenemos permiso, cargamos contactos
                viewModel.fetchDeviceContacts()
            }
            else -> {
                // No tenemos permiso, lo pedimos
                launcher.launch(Manifest.permission.READ_CONTACTS)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "Crear Evento",
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
                colors = TopAppBarDefaults.topAppBarColors(containerColor = YellowPrimary)
            )
        },
        containerColor = GrayBackground
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(scrollState)
        ) {
            // 1. Categoría
            CategorySelectionRow(
                selectedCategory = uiState.selectedCategory,
                onCategorySelected = viewModel::onCategorySelected
            )
            Spacer(modifier = Modifier.height(20.dp))

            // 2. Fecha y Hora
            val calendar = Calendar.getInstance()
            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, month, dayOfMonth ->
                    viewModel.onDateSelected(LocalDate.of(year, month + 1, dayOfMonth))
                },
                calendar.get(Calendar.YEAR),
                calendar.get(Calendar.MONTH),
                calendar.get(Calendar.DAY_OF_MONTH)
            )

            ReadOnlyTextField(
                label = "Fecha",
                value = uiState.getFormattedDate(),
                onClick = { datePickerDialog.show() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    viewModel.onTimeSelected(LocalTime.of(hourOfDay, minute))
                },
                calendar.get(Calendar.HOUR_OF_DAY),
                calendar.get(Calendar.MINUTE),
                true
            )

            ReadOnlyTextField(
                label = "Hora:",
                value = uiState.getFormattedTime(),
                onClick = { timePickerDialog.show() }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 3. Descripción
            CustomLabelTextField(
                label = "Descripción:",
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChanged,
                singleLine = false,
                minLines = 3
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 4. Status
            StatusDropdown(
                selectedStatus = uiState.status,
                onStatusSelected = viewModel::onStatusSelected
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 5. Ubicación
            ReadOnlyTextField(
                label = "Ubicación",
                value = uiState.location.ifEmpty { "Toque para seleccionar en mapa" },
                onClick = { showMap = true }
            )
            Spacer(modifier = Modifier.height(16.dp))

            // 6. Contacto (AHORA DINÁMICO)
            ContactDropdown(
                selectedContact = uiState.selectedContactName,
                availableContacts = uiState.availableContacts,
                onContactSelected = viewModel::onContactSelected
            )
            Spacer(modifier = Modifier.height(30.dp))

            // 7. Botón Guardar
            Button(
                onClick = viewModel::onSaveClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = ButtonDefaults.buttonColors(containerColor = GrayUnselected),
                shape = RoundedCornerShape(8.dp)
            ) {
                Text(
                    "Guardar",
                    color = Color.Black,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }

    if (showMap) {
        Dialog(
            onDismissRequest = { showMap = false },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            LocationPickerMap(
                onLocationConfirmed = { lat, lon ->
                    viewModel.onLocationSelected(lat, lon)
                    showMap = false
                }
            )
        }
    }
}

// ... Componentes Auxiliares (CategorySelectionRow, GenericDropdown, etc) IGUALES ...

@Composable
fun CategorySelectionRow(
    selectedCategory: EventCategory,
    onCategorySelected: (EventCategory) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Text("Categoria:", fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
        Row(
            modifier = Modifier.horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EventCategory.values().forEach { category ->
                CategoryTabButton(
                    text = category.displayName,
                    isSelected = category == selectedCategory,
                    onClick = { onCategorySelected(category) }
                )
            }
        }
    }
}

@Composable
fun CategoryTabButton(text: String, isSelected: Boolean, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(if (isSelected) BlueHighlight else GrayUnselected)
            .clickable { onClick() }
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Text(
            text = text,
            color = if (isSelected) Color.White else Color.Black,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            fontSize = 12.sp
        )
    }
}

@Composable
fun CustomLabelTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    singleLine: Boolean = true,
    minLines: Int = 1
) {
    Row(verticalAlignment = Alignment.Top, modifier = Modifier.fillMaxWidth()) {
        Text(
            label,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.width(80.dp).padding(top = 16.dp)
        )
        OutlinedTextField(
            value = value,
            onValueChange = onValueChange,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedContainerColor = Color.White,
                unfocusedContainerColor = Color.White,
                focusedBorderColor = GrayUnselected,
                unfocusedBorderColor = GrayUnselected
            ),
            singleLine = singleLine,
            minLines = minLines
        )
    }
}

@Composable
fun ReadOnlyTextField(label: String, value: String, onClick: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
        Box(modifier = Modifier.weight(1f).clickable { onClick() }) {
            OutlinedTextField(
                value = value,
                onValueChange = {},
                modifier = Modifier.fillMaxWidth(),
                enabled = false,
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    disabledContainerColor = Color.White,
                    disabledBorderColor = GrayUnselected,
                    disabledTextColor = Color.Black
                ),
            )
            Box(modifier = Modifier.matchParentSize().clickable(onClick = onClick))
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GenericDropdown(
    label: String,
    selectedValue: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
        Text(label, fontWeight = FontWeight.Bold, modifier = Modifier.width(80.dp))
        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded },
            modifier = Modifier.weight(1f)
        ) {
            OutlinedTextField(
                modifier = Modifier.menuAnchor().fillMaxWidth(),
                readOnly = true,
                value = selectedValue,
                onValueChange = {},
                trailingIcon = { Icon(Icons.Filled.ArrowDropDown, null) },
                shape = RoundedCornerShape(12.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = Color.White,
                    unfocusedContainerColor = Color.White,
                    focusedBorderColor = GrayUnselected,
                    unfocusedBorderColor = GrayUnselected
                ),
                textStyle = LocalTextStyle.current.copy(fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            )
            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                // Validación para lista vacía
                if (options.isEmpty()) {
                    DropdownMenuItem(
                        text = { Text("No hay contactos") },
                        onClick = { expanded = false }
                    )
                } else {
                    options.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onOptionSelected(option)
                                expanded = false
                            },
                            contentPadding = ExposedDropdownMenuDefaults.ItemContentPadding,
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StatusDropdown(selectedStatus: EventStatus, onStatusSelected: (EventStatus) -> Unit) {
    GenericDropdown(
        label = "Status:",
        selectedValue = selectedStatus.displayName,
        options = EventStatus.values().map { it.displayName },
        onOptionSelected = { name ->
            val status = EventStatus.values().first { it.displayName == name }
            onStatusSelected(status)
        }
    )
}

@Composable
fun ContactDropdown(selectedContact: String, availableContacts: List<String>, onContactSelected: (String) -> Unit) {
    GenericDropdown(
        label = "Contacto",
        selectedValue = selectedContact,
        options = availableContacts,
        onOptionSelected = onContactSelected
    )
}