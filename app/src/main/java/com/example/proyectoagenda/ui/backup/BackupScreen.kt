package com.example.proyectoagenda.ui.backup

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.proyectoagenda.model.EventRepository
import com.example.proyectoagenda.model.exportEventsAsJson
import com.example.proyectoagenda.model.importEventsFromJson
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onMenuClicked: () -> Unit) {   // ← AGREGADO

    val context = LocalContext.current
    val repo = remember { EventRepository(context) }
    val scope = rememberCoroutineScope()

    var importText by remember { mutableStateOf("") }

    val createBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) { uri ->
            if (uri != null) {
                val json = exportEventsAsJson(repo)
                context.contentResolver.openOutputStream(uri)?.use { out ->
                    out.write(json.toByteArray())
                }
                Toast.makeText(context, "Respaldo guardado", Toast.LENGTH_SHORT).show()
            }
        }

    val openBackupLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                context.contentResolver.openInputStream(uri)?.use { input ->
                    importText = input.bufferedReader().readText()
                }
            }
        }

    // -----------------------
    //   UI con Scaffold
    // -----------------------
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respaldar / Recuperar") },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, contentDescription = null)
                    }
                }
            )
        }
    ) { padding ->

        Column(modifier = Modifier.padding(padding).padding(16.dp)) {

            Text("Respaldo de eventos", style = MaterialTheme.typography.titleLarge)
            Spacer(modifier = Modifier.height(16.dp))

            Button(onClick = {
                createBackupLauncher.launch("agenda_backup.json")
            }) {
                Text("Exportar agenda")
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(onClick = {
                openBackupLauncher.launch(arrayOf("application/json"))
            }) {
                Text("Importar archivo JSON")
            }

            Spacer(modifier = Modifier.height(16.dp))

            if (importText.isNotEmpty()) {
                Button(onClick = {
                    scope.launch {
                        importEventsFromJson(repo, importText)
                        Toast.makeText(context, "Agenda restaurada", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Restaurar datos importados")
                }
            }
        }
    }
}