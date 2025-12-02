package com.example.proyectoagenda

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.proyectoagenda.ui.navigation.AppNavigation

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // setContent es el punto de entrada de Compose
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    // CAMBIO CLAVE: Aquí llamamos al "Router" (Navegación)
                    // en lugar de llamar a una pantalla específica como CreateEventScreen()
                    AppNavigation()
                }
            }
        }
    }
}