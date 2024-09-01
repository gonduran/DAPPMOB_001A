package com.example.nutricionsemanal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.example.nutricionsemanal.ui.theme.NutricionSemanalTheme
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.nutricionsemanal.receta.RecetaRepository
import com.example.nutricionsemanal.user.InMemoryUserRepository
import com.example.nutricionsemanal.user.UserRepository

class MainActivity : ComponentActivity() {
    // Crear una instancia de UserRepository
    private val userRepository: UserRepository = InMemoryUserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutricionSemanalTheme {
                val navController = rememberNavController()
                val recetaRepository = RecetaRepository()
                AppNavigator(navController, userRepository, recetaRepository = recetaRepository)
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController, userRepository: UserRepository, recetaRepository: RecetaRepository) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController = navController, userRepository = userRepository) }
        composable("register") { RegisterScreen(navController = navController, userRepository = userRepository) }
        composable("recoverPassword") { RecoverPasswordScreen(navController = navController, userRepository = userRepository) }
        composable("recetasScreen") { RecetasScreen(navController = navController, recetaRepository = recetaRepository) }
        composable("detalleReceta/{recetaIndex}") { backStackEntry ->
            val recetaIndex = backStackEntry.arguments?.getString("recetaIndex")?.toInt() ?: 0
            DetalleRecetaScreen(navController = navController, recetaIndex = recetaIndex, recetaRepository = recetaRepository)
        }
    }
}