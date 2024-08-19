package com.example.alarmavisual
/*
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import com.example.alarmavisual.views.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Guarda (email, password)
val registeredUsers = mutableListOf<Pair<String, String>>()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                AlarmaVisualTheme {
                    val navController = rememberNavController()
                    AppNavigator(navController)
                }
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "splash") {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("recoverPassword") { RecoverPasswordScreen(navController) }
        composable("clockScreen") { ClockScreen(navController = navController) }
        composable("activateAlarm") { AlarmScreen(navController = navController) }
    }
}*/

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import com.example.alarmavisual.views.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController

// Guarda (email, password)
val registeredUsers = mutableListOf<Pair<String, String>>()

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlarmaVisualTheme {
                val navController = rememberNavController()

                // Escuchar cambios en el Intent y navegar en consecuencia
                var navigateTo by remember { mutableStateOf(intent?.getStringExtra("navigate_to")) }

                // Verificar si el Intent cambia y actualizar la variable navigateTo
                LaunchedEffect(intent) {
                    navigateTo = intent?.getStringExtra("navigate_to")
                }

                AppNavigator(navController, navigateTo)
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController, navigateTo: String?) {
    // Determina si la pantalla inicial es "alarmScreen" o "splash"
    val startDestination = if (navigateTo == "alarmScreen") "activateAlarm" else "splash"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("recoverPassword") { RecoverPasswordScreen(navController) }
        composable("clockScreen") { ClockScreen(navController = navController) }
        composable("activateAlarm") { AlarmScreen(navController = navController) }
    }
}