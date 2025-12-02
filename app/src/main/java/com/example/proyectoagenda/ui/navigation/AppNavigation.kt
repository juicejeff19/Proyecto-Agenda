package com.example.proyectoagenda.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectoagenda.ui.consult.ConsultEventScreen
import com.tuempresa.proyectoagenda.ui.create.CreateEventScreen
import kotlinx.coroutines.launch

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet {
                Spacer(modifier = Modifier.height(16.dp))
                Text("Agenda App", modifier = Modifier.padding(16.dp), style = MaterialTheme.typography.headlineSmall)
                Divider()

                NavigationDrawerItem(
                    label = { Text(text = "Añadir Eventos") },
                    selected = false,
                    icon = { Icon(Icons.Default.Add, null) },
                    onClick = {
                        navController.navigate("create")
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(text = "Consultar Eventos") },
                    selected = false,
                    icon = { Icon(Icons.Default.DateRange, null) },
                    onClick = {
                        navController.navigate("consult")
                        scope.launch { drawerState.close() }
                    }
                )

                NavigationDrawerItem(
                    label = { Text(text = "Salir") },
                    selected = false,
                    icon = { Icon(Icons.Default.Home, null) }, // Icono temporal
                    onClick = {
                        // Lógica de salir
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {
        // El contenido principal que cambia
        NavHost(navController = navController, startDestination = "create") {
            composable("create") {
                CreateEventScreen(
                    onMenuClicked = { scope.launch { drawerState.open() } }
                )
            }
            composable("consult") {
                ConsultEventScreen(
                    onMenuClicked = { scope.launch { drawerState.open() } }
                )
            }
        }
    }
}