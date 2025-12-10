package com.example.proyectoagenda.ui.create

import android.content.Context
import android.location.Geocoder
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker
import java.util.Locale

@OptIn(ExperimentalComposeUiApi::class, ExperimentalMaterial3Api::class)
@Composable
fun LocationPickerMap(
    initialLatitude: Double = 19.4326, // CDMX
    initialLongitude: Double = -99.1332,
    onLocationConfirmed: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val keyboardController = LocalSoftwareKeyboardController.current

    // Estado del punto seleccionado
    var selectedPoint by remember { mutableStateOf(GeoPoint(initialLatitude, initialLongitude)) }

    // Estado de la búsqueda
    var searchQuery by remember { mutableStateOf("") }

    // Referencia al mapa para poder moverlo programáticamente desde la búsqueda
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    // Función auxiliar para buscar y mover el mapa
    fun performSearch() {
        if (searchQuery.isBlank()) return

        keyboardController?.hide() // Ocultar teclado

        scope.launch(Dispatchers.IO) {
            try {
                val geocoder = Geocoder(context, Locale.getDefault())
                // Obtenemos máximo 1 resultado
                @Suppress("DEPRECATION")
                val results = geocoder.getFromLocationName(searchQuery, 1)

                if (!results.isNullOrEmpty()) {
                    val location = results[0]
                    val newPoint = GeoPoint(location.latitude, location.longitude)

                    withContext(Dispatchers.Main) {
                        selectedPoint = newPoint
                        // Movemos el mapa
                        mapViewRef?.controller?.animateTo(newPoint)
                        mapViewRef?.controller?.setZoom(16.0)

                        // Actualizamos marcadores manualmente en la referencia del mapa
                        mapViewRef?.let { map ->
                            map.overlays.removeAll { it is Marker }
                            val marker = Marker(map)
                            marker.position = newPoint
                            marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                            marker.title = location.featureName ?: "Resultado"
                            map.overlays.add(marker)
                            map.invalidate()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Lugar no encontrado", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error en búsqueda: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {

        // 1. EL MAPA
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    mapViewRef = this // Guardamos referencia
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(selectedPoint)

                    // Detector de toques
                    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { point ->
                                selectedPoint = point

                                // Actualizar marcador visual
                                overlays.removeAll { it is Marker }
                                val marker = Marker(this@apply)
                                marker.position = point
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "Ubicación Seleccionada"
                                overlays.add(marker)
                                invalidate()
                            }
                            return true
                        }
                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })
                    overlays.add(mapEventsOverlay)

                    // Marcador inicial
                    val startMarker = Marker(this)
                    startMarker.position = selectedPoint
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    overlays.add(startMarker)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

        // 2. BARRA DE BÚSQUEDA (Flotante arriba)
        Column(
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color.White, RoundedCornerShape(24.dp)),
                placeholder = { Text("Buscar lugar (ej. Coyoacán)...") },
                singleLine = true,
                shape = RoundedCornerShape(24.dp),
                trailingIcon = {
                    IconButton(onClick = { performSearch() }) {
                        Icon(Icons.Default.Search, contentDescription = "Buscar")
                    }
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                keyboardActions = KeyboardActions(onSearch = { performSearch() })
            )
        }

        // 3. BOTÓN CONFIRMAR (Flotante abajo)
        FloatingActionButton(
            onClick = { onLocationConfirmed(selectedPoint.latitude, selectedPoint.longitude) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(16.dp)
        ) {
            Icon(Icons.Default.Check, contentDescription = "Confirmar Ubicación")
        }
    }
}