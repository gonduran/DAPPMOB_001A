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

// Guarda (email, password)
val registeredUsers = mutableListOf<Pair<String, String>>()

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            NutricionSemanalTheme {
                val navController = rememberNavController()
                AppNavigator(navController)
            }
        }
    }
}

@Composable
fun AppNavigator(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {
        composable("login") { LoginScreen(navController) }
        composable("register") { RegisterScreen(navController) }
        composable("recoverPassword") { RecoverPasswordScreen(navController) }
        composable("recetasScreen") { RecetasScreen(navController) }
        composable("detalleReceta/{recetaIndex}") { backStackEntry ->
            val recetaIndex = backStackEntry.arguments?.getString("recetaIndex")?.toInt() ?: 0
            DetalleRecetaScreen(navController, recetaIndex)
        }

        /*composable("minuta") {
            val recetas = listOf(
                Receta("Ensalada César", "Alto en proteínas, bajo en carbohidratos"),
                Receta("Pasta al Pesto", "Fuente de carbohidratos y grasas saludables"),
                Receta("Pollo a la plancha", "Bajo en calorías y alto en proteínas"),
                Receta("Sopa de verduras", "Rico en fibra y vitaminas"),
                Receta("Batido de frutas", "Fuente de antioxidantes y energía rápida")
            )
            MinutaScreen(navController = navController, recetas = recetas)
        }*/
    }
}