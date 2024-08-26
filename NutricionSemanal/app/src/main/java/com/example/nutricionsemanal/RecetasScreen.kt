package com.example.nutricionsemanal

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController

data class Receta(
    val nombre: String,
    val breveDescripcion: String,
    val detalle: String
)

val recetas = listOf(
    Receta(
        "Ensalada César",
        "Fresca ensalada con pollo y aderezo César.",
        "Ingredientes: Lechuga romana, pollo, crutones, queso parmesano y aderezo César..."
    ),
    Receta(
        "Sopa de Tomate",
        "Sopa cremosa de tomate con albahaca.",
        "Ingredientes: Tomates, albahaca fresca, cebolla, ajo, caldo de verduras..."
    ),
    Receta(
        "Pasta al Pesto",
        "Pasta con salsa de pesto casera.",
        "Ingredientes: Albahaca fresca, ajo, queso parmesano, piñones, aceite de oliva..."
    ),
    Receta(
        "Pollo a la Plancha",
        "Pollo marinado con hierbas a la plancha.",
        "Ingredientes: Pechugas de pollo, hierbas aromáticas, ajo, limón y aceite de oliva..."
    ),
    Receta(
        "Arroz Frito con Verduras",
        "Arroz salteado con verduras frescas y salsa de soya.",
        "Ingredientes: Arroz cocido, zanahoria, guisantes, maíz, salsa de soya..."
    ),
    Receta(
        "Tacos de Pescado",
        "Tacos rellenos de pescado frito con salsa de yogur.",
        "Ingredientes: Filetes de pescado, tortillas de maíz, col rallada, salsa de yogur..."
    ),
    Receta(
        "Batido de Frutas",
        "Batido refrescante con una mezcla de frutas tropicales.",
        "Ingredientes: Mango, piña, leche de coco, hielo, miel..."
    )
)

@Composable
fun RecetasScreen(navController: NavHostController) {
    val context = LocalContext.current

    // Define tus colores de degradado
    val gradientColors = listOf(
        Color(0xFFFFFFFF),
        Color(0xFFCDDC39)
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(0.dp)
            .background(Brush.verticalGradient(gradientColors)),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        itemsIndexed(recetas) { index, receta ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp, horizontal = 16.dp)
                    .clickable {
                        // Navegar a la pantalla de detalles de la receta seleccionada
                        navController.navigate("detalleReceta/$index")
                    },
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = receta.nombre, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = receta.breveDescripcion, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun RecetasPreview() {
    val navController = rememberNavController()
    MaterialTheme {
        RecetasScreen(navController = navController)
    }
}