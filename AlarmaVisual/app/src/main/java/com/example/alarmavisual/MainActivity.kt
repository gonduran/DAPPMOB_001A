package com.example.alarmavisual

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
import com.example.alarmavisual.user.InMemoryUserRepository
import com.example.alarmavisual.user.UserRepository

// Guarda (email, password)
val registeredUsers = mutableListOf<Pair<String, String>>()

class MainActivity : ComponentActivity() {
    // Crear una instancia de UserRepository
    private val userRepository: UserRepository = InMemoryUserRepository()

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

                AppNavigator(navController, navigateTo, userRepository)
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController, navigateTo: String?, userRepository: UserRepository) {
    // Determina si la pantalla inicial es "alarmScreen" o "splash"
    val startDestination = if (navigateTo == "alarmScreen") "activateAlarm" else "splash"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController = navController, userRepository = userRepository) }
        composable("register") { RegisterScreen(navController = navController, userRepository = userRepository) }
        composable("recoverPassword") { RecoverPasswordScreen(navController = navController, userRepository = userRepository) }
        composable("clockScreen") { ClockScreen(navController = navController) }
        composable("activateAlarm") { AlarmScreen(navController = navController) }
    }
}