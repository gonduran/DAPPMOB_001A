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
import com.example.alarmavisual.alarm.CustomAlarmManager
import com.example.alarmavisual.user.InMemoryUserRepository
import com.example.alarmavisual.user.UserRepository

class MainActivity : ComponentActivity() {
    // Crear una instancia de UserRepository
    private val userRepository: UserRepository = InMemoryUserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            AlarmaVisualTheme {
                val navController = rememberNavController()
                val alarmManager = CustomAlarmManager(context = this)

                // Escuchar cambios en el Intent y navegar en consecuencia
                var navigateTo by remember { mutableStateOf(intent?.getStringExtra("navigate_to")) }

                // Verificar si el Intent cambia y actualizar la variable navigateTo
                LaunchedEffect(intent) {
                    navigateTo = intent?.getStringExtra("navigate_to")
                }

                AppNavigator(navController, navigateTo, userRepository, alarmManager)
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController, navigateTo: String?, userRepository: UserRepository, alarmManager : CustomAlarmManager) {
    // Determina si la pantalla inicial es "alarmScreen" o "splash"
    val startDestination = if (navigateTo == "alarmScreen") "activateAlarm" else "splash"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController = navController, userRepository = userRepository) }
        composable("register") { RegisterScreen(navController = navController, userRepository = userRepository) }
        composable("recoverPassword") { RecoverPasswordScreen(navController = navController, userRepository = userRepository) }

        composable("alarmListScreen") { AlarmListScreen(navController = navController, alarmManager = alarmManager) }
        composable("addAlarmScreen") { AddAlarmScreen(navController = navController, alarmManager = alarmManager) }
        // Pasar el alarmId como argumento
        composable("editAlarmScreen/{alarmId}") { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")
            if (alarmId != null) {
                EditAlarmScreen(navController = navController, alarmManager = alarmManager, alarmId = alarmId)
            }
        }

        composable("activateAlarm") { AlarmScreen(navController = navController, alarmManager = alarmManager) }
    }
}