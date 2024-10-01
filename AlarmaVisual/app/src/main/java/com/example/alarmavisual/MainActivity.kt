package com.example.alarmavisual

import android.app.Application
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.alarmavisual.ui.theme.AlarmaVisualTheme
import com.example.alarmavisual.views.*
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.alarmavisual.alarm.CustomAlarmManager
import com.example.alarmavisual.viewmodels.ConversationViewModel
import com.example.alarmavisual.viewmodels.ConversationViewModelFactory
import com.google.firebase.FirebaseApp
import android.speech.tts.TextToSpeech
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        FirebaseApp.initializeApp(this)  // Inicializar Firebase

        setContent {
            AlarmaVisualTheme {
                val navController = rememberNavController()
                val alarmManager = try {
                    CustomAlarmManager(context = this)
                } catch (e: Exception) {
                    Toast.makeText(this, "Error initializing alarmManager: ${e.message}", Toast.LENGTH_LONG).show()
                    null // Return null in case of error
                }

                // Escuchar cambios en el Intent y navegar en consecuencia
                var navigateTo by remember { mutableStateOf(intent?.getStringExtra("navigate_to")) }

                // Verificar si el Intent cambia y actualizar la variable navigateTo
                LaunchedEffect(intent) {
                    navigateTo = intent?.getStringExtra("navigate_to")
                }

                alarmManager?.let {
                    AppNavigator(navController, intent?.getStringExtra("navigate_to"), it)
                } ?: run {
                    Toast.makeText(this, "Failed to initialize alarm manager", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController, navigateTo: String?, alarmManager : CustomAlarmManager) {
    // Determina si la pantalla inicial es "alarmScreen" o "splash"
    val startDestination = if (navigateTo == "alarmScreen") "activateAlarm" else "splash"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("splash") { SplashScreen(navController) }
        composable("login") { LoginScreen(navController = navController) }
        composable("register") { RegisterScreen(navController = navController) }
        composable("recoverPassword") { RecoverPasswordScreen(navController = navController) }

        composable("homeMenu") { HomeMenuScreen(navController = navController, alarmManager = alarmManager) }
        composable("alarmList") { AlarmListScreen(navController = navController, alarmManager = alarmManager) }
        composable("addAlarm") { AddAlarmScreen(navController = navController, alarmManager = alarmManager) }
        // Pasar el alarmId como argumento
        composable("editAlarm/{alarmId}") { backStackEntry ->
            val alarmId = backStackEntry.arguments?.getString("alarmId")
            alarmId?.let {
                EditAlarmScreen(navController = navController, alarmManager = alarmManager, alarmId = it)
            } ?: run {
                Toast.makeText(navController.context, "Error: Alarm ID no encontrado", Toast.LENGTH_LONG).show()
            }
        }

        composable("activateAlarm") { AlarmScreen(navController = navController, alarmManager = alarmManager) }
        composable("location") { LocationScreen(navController = navController) }
        // Pantalla para la conversación inclusiva
        composable("conversation") {
            val context = LocalContext.current

            // Crear una instancia de TextToSpeech
            val textToSpeech = remember {
                TextToSpeech(context) {
                    if (it != TextToSpeech.SUCCESS) {
                        Toast.makeText(context, "Error inicializando TextToSpeech", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Crear la instancia del ViewModel usando la fábrica
            val viewModel: ConversationViewModel = viewModel(
                factory = ConversationViewModelFactory(
                    context.applicationContext as Application,
                    textToSpeech,
                    firestore = FirebaseFirestore.getInstance()
                )
            )

            // Llamar a la pantalla de conversación y pasar el ViewModel
            ConversationScreen(navController = navController, viewModel = viewModel)
        }
    }
}