package com.example.proyectoagenda.ui.create

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import org.osmdroid.config.Configuration
import org.osmdroid.events.MapEventsReceiver
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.MapEventsOverlay
import org.osmdroid.views.overlay.Marker

// ... imports ...

@Composable
fun LocationPickerMap(
    initialLatitude: Double = 19.4326,
    initialLongitude: Double = -99.1332,
    onLocationConfirmed: (Double, Double) -> Unit
) {
    val context = LocalContext.current
    var selectedPoint by remember { mutableStateOf(GeoPoint(initialLatitude, initialLongitude)) }

    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
    }

    Box(modifier = Modifier.fillMaxSize()) {
        AndroidView(
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    controller.setZoom(15.0)
                    controller.setCenter(selectedPoint)

                    // --- CORRECCIÓN AQUÍ ---
                    val mapEventsOverlay = MapEventsOverlay(object : MapEventsReceiver {
                        override fun singleTapConfirmedHelper(p: GeoPoint?): Boolean {
                            p?.let { point ->
                                selectedPoint = point

                                // 1. En lugar de borrar TODO, borramos solo los Marcadores antiguos.
                                // Esto evita que borremos el detector de eventos accidentalmente.
                                overlays.removeAll { it is Marker }

                                // 2. Creamos el nuevo marcador
                                val marker = Marker(this@apply)
                                marker.position = point
                                marker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                                marker.title = "Ubicación Seleccionada"

                                // 3. Lo agregamos
                                overlays.add(marker)

                                // 4. Refrescamos el mapa
                                invalidate()
                            }
                            return true
                        }

                        override fun longPressHelper(p: GeoPoint?): Boolean = false
                    })

                    // Agregamos el detector de eventos UNA SOLA VEZ
                    overlays.add(mapEventsOverlay)

                    // Marcador inicial (si aplica)
                    val startMarker = Marker(this)
                    startMarker.position = selectedPoint
                    startMarker.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                    overlays.add(startMarker)
                }
            },
            modifier = Modifier.fillMaxSize()
        )

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