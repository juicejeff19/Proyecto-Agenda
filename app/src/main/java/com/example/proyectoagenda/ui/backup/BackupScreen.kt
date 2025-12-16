package com.example.proyectoagenda.ui.backup

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.dropbox.core.android.Auth // Importante: Clase estática del SDK
import com.example.proyectoagenda.cloud.DropboxService
import com.example.proyectoagenda.model.EventRepository
import com.example.proyectoagenda.model.exportEventsAsJson
import com.example.proyectoagenda.model.importEventsFromJson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onMenuClicked: () -> Unit) {

    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val scope = rememberCoroutineScope()

    // Repositorios y Servicios
    val repo = remember { EventRepository(context) }
    val dropboxService = remember { DropboxService(context) }

    // Estados
    var isConnected by remember { mutableStateOf(dropboxService.isClientInitialized()) }
    var isLoading by remember { mutableStateOf(false) }

    // Constantes
    val APP_KEY = "17wsfmwhq502yjt"
    val CLOUD_PATH = "/agenda_backup.json"

    // --- DETECTOR DE RETORNO DE AUTH (Lifecycle Observer) ---
    // Esto se ejecuta cuando el usuario vuelve del navegador de Dropbox a la App
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                // Verificar si Dropbox nos devolvió un token
                val token = Auth.getOAuth2Token()
                if (token != null) {
                    // ¡Éxito! Guardamos token e inicializamos cliente
                    dropboxService.initClient(token)
                    isConnected = true
                    Toast.makeText(context, "Conexión exitosa", Toast.LENGTH_SHORT).show()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respaldo Nube (Dropbox)") },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, null)
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .padding(16.dp)
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Text("Gestión de Respaldos", style = MaterialTheme.typography.headlineSmall)
            Spacer(modifier = Modifier.height(24.dp))

            // --- BOTÓN CONECTAR / DESCONECTAR ---
            if (!isConnected) {
                Button(
                    onClick = {
                        // Inicia el flujo OAuth nativo
                        Auth.startOAuth2Authentication(context, APP_KEY)
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Conectar con Dropbox")
                }
            } else {
                OutlinedButton(
                    onClick = {
                        dropboxService.logout()
                        isConnected = false
                        Toast.makeText(context, "Desconectado", Toast.LENGTH_SHORT).show()
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Desconectar cuenta")
                }
            }

            Spacer(modifier = Modifier.height(32.dp))

            // --- ACCIONES DE NUBE (Solo si conectado) ---
            if (isConnected) {
                Text("Acciones en la Nube", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    // SUBIR
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val json = exportEventsAsJson(repo)
                                val success = dropboxService.uploadFile(json, CLOUD_PATH)
                                isLoading = false
                                if (success) {
                                    Toast.makeText(context, "Respaldo subido correctamente", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error al subir", Toast.LENGTH_SHORT).show()
                                }
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading
                    ) {
                        Icon(Icons.Default.CloudUpload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Subir")
                    }

                    // BAJAR
                    Button(
                        onClick = {
                            scope.launch {
                                isLoading = true
                                val json = dropboxService.downloadFile(CLOUD_PATH)
                                if (json != null) {
                                    importEventsFromJson(repo, json)
                                    Toast.makeText(context, "Datos restaurados", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(context, "Error o archivo no existe", Toast.LENGTH_SHORT).show()
                                }
                                isLoading = false
                            }
                        },
                        modifier = Modifier.weight(1f),
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Icon(Icons.Default.CloudDownload, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Bajar")
                    }
                }
            }

            if (isLoading) {
                Spacer(modifier = Modifier.height(16.dp))
                CircularProgressIndicator()
            }
        }
    }
}