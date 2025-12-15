package com.example.proyectoagenda.ui.navigation

import android.annotation.SuppressLint
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.proyectoagenda.ui.consult.ConsultEventScreen
import com.example.proyectoagenda.ui.consult.ConsultEventViewModel
import com.example.proyectoagenda.ui.create.CreateEventViewModel
import com.tuempresa.proyectoagenda.ui.create.CreateEventScreen
import kotlinx.coroutines.launch
import com.example.proyectoagenda.ui.backup.BackupScreen
import androidx.compose.ui.platform.LocalContext
import androidx.activity.ComponentActivity


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
@SuppressLint("ContextCastToActivity")
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

                // --- Respaldar/Recuperar ---
                NavigationDrawerItem(
                    label = { Text("Respaldar / Recuperar") },
                    selected = false,
                    icon = { Icon(Icons.Default.Cloud, null) },
                    onClick = {
                        navController.navigate("backup")
                        scope.launch { drawerState.close() }
                    }
                )

                // --- Acerca de... ---
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
                startDestination = "inicio", // Ya apunta a Inicio
                modifier = Modifier.padding(padding)
            ) {

                // ------------------------------
                // HOME SCREEN (NUEVA)
                // ------------------------------
                composable("inicio") {
                    // Aquí llamamos a HomeScreen. Como eliminamos la versión antigua
                    // al final de este archivo, ahora usará la nueva versión (Dashboard)
                    // que creamos en el archivo separado HomeScreen.kt
                    HomeScreen(
                        onMenuClicked = { scope.launch { drawerState.open() } }
                    )
                }

                // ------------------------------
                // CREATE / EDIT SCREEN
                // ------------------------------
                composable(
                    route = "create?eventId={eventId}",
                    arguments = listOf(
                        navArgument("eventId") {
                            type = NavType.LongType
                            defaultValue = -1L
                        }
                    )
                ) { backStackEntry ->
                    val eventId = backStackEntry.arguments?.getLong("eventId") ?: -1L
                    val createViewModel: CreateEventViewModel = viewModel()

                    LaunchedEffect(eventId) {
                        if (eventId != -1L) {
                            createViewModel.loadEventForEdit(eventId)
                        } else {
                            createViewModel.clearForm()
                        }
                    }

                    CreateEventScreen(
                        viewModel = createViewModel,
                        onMenuClicked = { scope.launch { drawerState.open() } }
                    )
                }

                // ------------------------------
                // CONSULT SCREEN
                // ------------------------------
                composable("consult") {
                    val viewModel: ConsultEventViewModel = viewModel()

                    LaunchedEffect(Unit) {
                        viewModel.loadEvents()
                    }

                    ConsultEventScreen(
                        viewModel = viewModel,
                        onMenuClicked = { scope.launch { drawerState.open() } },
                        onEditEvent = { eventId ->
                            navController.navigate("create?eventId=$eventId")
                        }
                    )
                }

                // ------------------------------
                // OTRAS PANTALLAS
                // ------------------------------
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
                    val activity = LocalContext.current as ComponentActivity

                    ExitScreen(
                        onExit = { activity.finishAffinity() }
                    )
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
fun ExitScreen(onExit: () -> Unit) {
    Column(modifier = Modifier.padding(32.dp)) {
        Text("Gracias por usar la app.")
        Spacer(modifier = Modifier.height(16.dp))
        Button(onClick = onExit) {
            Text("Cerrar aplicación")
        }
    }
}