package com.example.nutricionsemanal

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController

data class Receta(val nombre: String, val recomendaciones: String)

@Composable
fun MinutaScreen(navController: NavHostController, recetas: List<Receta>) {
    LazyColumn(
        modifier = Modifier.padding(16.dp)
    ) {
        items(recetas) { receta ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                elevation = CardDefaults.cardElevation(4.dp)  // Aquí está el cambio
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(text = receta.nombre, style = MaterialTheme.typography.headlineSmall)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(text = receta.recomendaciones)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun MinutaPreview() {
    val recetas = listOf(
        Receta("Ensalada César", "Alto en proteínas, bajo en carbohidratos"),
        Receta("Pasta al Pesto", "Fuente de carbohidratos y grasas saludables"),
        Receta("Pollo a la plancha", "Bajo en calorías y alto en proteínas"),
        Receta("Sopa de verduras", "Rico en fibra y vitaminas"),
        Receta("Batido de frutas", "Fuente de antioxidantes y energía rápida")
    )

    MaterialTheme {
       // MinutaScreen(recetas = recetas)
    }
}