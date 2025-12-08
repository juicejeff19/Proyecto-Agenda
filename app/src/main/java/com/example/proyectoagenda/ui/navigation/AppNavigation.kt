package com.example.proyectoagenda.ui.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.proyectoagenda.ui.consult.ConsultEventScreen
import com.tuempresa.proyectoagenda.ui.create.CreateEventScreen
import kotlinx.coroutines.launch

// ---------------------------------------------------------------------
// Bottom Navigation Items
// ---------------------------------------------------------------------
sealed class BottomNavItem(val route: String, val label: String, val icon: ImageVector) {
    object Inicio : BottomNavItem("inicio", "Inicio", Icons.Default.Home)
    object Consultar : BottomNavItem("consult", "Consultar", Icons.Default.DateRange)
    object Salir : BottomNavItem("exit", "Salir", Icons.Default.ExitToApp)
}

val bottomItems = listOf(
    BottomNavItem.Inicio,
    BottomNavItem.Consultar,
    BottomNavItem.Salir
)

// ---------------------------------------------------------------------
//  NAVEGACIÓN PRINCIPAL
// ---------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
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
                Text(
                    "Agenda App",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.headlineSmall
                )
                Divider()

                // --- Añadir Eventos ---
                NavigationDrawerItem(
                    label = { Text("Añadir Eventos") },
                    selected = false,
                    icon = { Icon(Icons.Default.Add, null) },
                    onClick = {
                        navController.navigate("create")
                        scope.launch { drawerState.close() }
                    }
                )

                // --- Consultar Eventos ---
                NavigationDrawerItem(
                    label = { Text("Consultar Eventos") },
                    selected = false,
                    icon = { Icon(Icons.Default.DateRange, null) },
                    onClick = {
                        navController.navigate("consult")
                        scope.launch { drawerState.close() }
                    }
                )

                // --- NUEVO: Respaldar/Recuperar ---
                NavigationDrawerItem(
                    label = { Text("Respaldar / Recuperar") },
                    selected = false,
                    icon = { Icon(Icons.Default.Cloud, null) },
                    onClick = {
                        navController.navigate("backup")
                        scope.launch { drawerState.close() }
                    }
                )

                // --- NUEVO: Acerca de... ---
                NavigationDrawerItem(
                    label = { Text("Acerca de…") },
                    selected = false,
                    icon = { Icon(Icons.Default.Info, null) },
                    onClick = {
                        navController.navigate("about")
                        scope.launch { drawerState.close() }
                    }
                )

                // --- Salir ---
                NavigationDrawerItem(
                    label = { Text("Salir") },
                    selected = false,
                    icon = { Icon(Icons.Default.Home, null) },
                    onClick = {
                        navController.navigate("exit")
                        scope.launch { drawerState.close() }
                    }
                )
            }
        }
    ) {

        // ------------------------------
        //  Scaffold con Bottom Bar
        // ------------------------------
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController)
            }
        ) { padding ->
            NavHost(
                navController = navController,
                startDestination = "inicio",
                modifier = Modifier.padding(padding)
            ) {

                // ------------------------------
                // HOME SCREEN (Listado simple de texto)
                // ------------------------------
                composable("inicio") {
                    HomeScreen(
                        onMenuClicked = { scope.launch { drawerState.open() } }
                    )
                }

                // ------------------------------
                // DEMÁS PANTALLAS
                // ------------------------------
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

                composable("backup") {
                    BackupScreen(
                        onMenuClicked = { scope.launch { drawerState.open() } }
                    )
                }

                composable("about") {
                    AboutScreen(
                        onMenuClicked = { scope.launch { drawerState.open() } }
                    )
                }

                composable("exit") {
                    ExitScreen()
                }
            }
        }
    }
}

// ---------------------------------------------------------------------
// BOTTOM NAV BAR
// ---------------------------------------------------------------------
@Composable
fun BottomNavigationBar(navController: androidx.navigation.NavController) {

    NavigationBar {

        val currentRoute = navController.currentBackStackEntry?.destination?.route

        bottomItems.forEach { item ->

            NavigationBarItem(
                selected = currentRoute == item.route,
                onClick = {
                    navController.navigate(item.route) {
                        launchSingleTop = true
                    }
                },
                icon = { Icon(item.icon, contentDescription = item.label) },
                label = { Text(item.label) }
            )
        }
    }
}

// ---------------------------------------------------------------------
// PANTALLAS ADICIONALES
// ---------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BackupScreen(onMenuClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Respaldar / Recuperar") },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, null)
                    }
                }
            )
        },
    ) { padding ->
        Text(
            "Opciones de respaldo en Dropbox / Drive",
            modifier = Modifier.padding(padding).padding(16.dp)
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AboutScreen(onMenuClicked: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Acerca de…") },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, null)
                    }
                }
            )
        }
    ) { padding ->
        Text(
            "Agenda App — versión 1.0",
            modifier = Modifier.padding(padding).padding(16.dp)
        )
    }
}

@Composable
fun ExitScreen() {
    Text(
        "Gracias por usar la app.",
        modifier = Modifier.padding(32.dp)
    )
}

// ---------------------------------------------------------------------
// PANTALLA HOME (Listado simple de texto)
// ---------------------------------------------------------------------
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onMenuClicked: () -> Unit) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Inicio") },
                navigationIcon = {
                    IconButton(onClick = onMenuClicked) {
                        Icon(Icons.Default.Menu, contentDescription = "Menú")
                    }
                }
            )
        }
    ) { padding ->

        val eventos = listOf(
            "Evento 1 - Hoy",
            "Evento 2 - Mañana",
            "Evento 3 - Próxima semana",
            "Evento 4 - Próximo mes"
        )

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            items(eventos) { evento ->
                Text(evento)
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
